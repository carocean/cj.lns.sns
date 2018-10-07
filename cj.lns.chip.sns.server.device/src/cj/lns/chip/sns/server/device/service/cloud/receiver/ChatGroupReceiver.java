package cj.lns.chip.sns.server.device.service.cloud.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.dao.IChatGroupDao;
import cj.lns.chip.sns.server.device.dao.IInboxDao;
import cj.lns.chip.sns.server.device.service.cloud.IAppReceiver;
import cj.lns.chip.sns.server.device.service.cloud.ProcessMessageService;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IChipInfo;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.Peer;
import cj.studio.ecm.sns.mailbox.Message;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

@CjService(name = "chatGroup")
public class ChatGroupReceiver implements IAppReceiver, IServiceAfter {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	@CjServiceRef
	IChatGroupDao chatGroupDao;
	@CjServiceRef
	IInboxDao inboxDao;
	private String webpart;
	@CjServiceInvertInjection
	@CjServiceRef(refByName = "/processMessage.service")
	ProcessMessageService process;

	@Override
	public void onAfter(IServiceSite site) {

		IChipInfo info = (IChipInfo) site
				.getService(String.format("$.%s", IChipInfo.class.getName()));
		String resource = info.getResourceProp("resource");
		if (resource.startsWith("/")) {
			resource = resource.substring(1, resource.length());
		}
		String f = String.format("%s/sessionMsgOther.html", resource);
		int blen = 8092;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(blen * 2);
		int timeRead = 0;
		byte[] b = new byte[blen];

		try {
			InputStream in = this.getClass().getClassLoader()
					.getResourceAsStream(f);
			while ((timeRead = in.read(b, 0, blen)) > 0) {
				buf.write(b, 0, timeRead);
			}
			webpart = new String(buf.toByteArray());
		} catch (IOException e) {
			throw new EcmException(e);
		} finally {
		}

	}

	@Override
	public void receive(MessageStub msg, Frame frame, Circuit circuit,
			IPin outputTerminus, IPin outputCloud) throws CircuitException {
		String destUser = frame.head("dest-user");
		// 消息先入港
		String appCode = frame.parameter("app-code");
		String appid = frame.parameter("app-id");
		/*
		 * 命中对方视窗的规则：
		 * 1.如果发送者是超级视窗，则发送给对方的home网盘临时存储
		 * 2.如果发送者是基础视窗，则同时发送给对方的该基础视窗的所有视窗（如果有，字段：swsbid），如果对方在该基础视窗下一个视窗也没有，则发送给对方的home网盘临时存储
		 * 3.如果发送者是公共视窗，则查得该公共视窗所在的基础视窗，然后进行第2步操作
		 * 4.如果发送者是个人视窗，则查得该个人视窗所在的基础视窗，然后进行第2步操作
		 * 5.对于发送给对方的home网盘的消息，也叫“发送给本人”，将在信息港进行提示，并邀请对方开通相应基础、公共视窗下的个人视窗（该功能在之后版本中实现）
		 */
		List<String> swsids = null;
		// msg.getSwstid()==-1表示是超级视窗，超级视窗发给对方的主空间，其它的视窗需要计算出发送的视窗列表
		if (msg.getSwstid().equals("-1")) {// 发给对方用户主空间
			swsids = new ArrayList<>();
			swsids.add("/home");// 注意：swsid在此表示要发往用户的哪个视窗，如果是/home即表示发往用户的主空间，因此并不一定表示视窗号
		} else {
			swsids = getAnotherSwsids(destUser, msg.getSenderOnSws(),
					outputCloud);
		}
		// if(StringUtil.isEmpty(swsid)){//如果视窗为空，则在终端的信息港的任务列表中列出申请新视窗的消息，以让用户判断是否申请新视窗，该功能在未来实现
		// 任务的消息是非会话消息。
		//
		// }
		if (swsids.isEmpty() && !swsids.contains("/home")) {// 发给对方用户主空间
			swsids.add("/home");
		}
		for (String swsid : swsids) {
			MySessionStub session = chatGroupDao.openSession(destUser, appCode,
					appid, swsid);
			msg.setSid(session.getSid());
			Message m = new Message();
			msg.fillTo(m);
			inboxDao.saveMessage(destUser, swsid, m);
			String destPeerId = frame.head("dest-peerid");
			if (StringUtil.isEmpty(destPeerId)) {
				List<Peer> peers = cache.getPeers(destUser);
				for (Peer peer : peers) {
					if (!cache.getOnAddress().equals(peer.getOnAddress())) {// 不在此服务器上，则转发
						frame.content()
								.writeBytes(new Gson().toJson(msg).getBytes());
						outputCloud.flow(frame, circuit);
						continue;
					}
					pushToPeer(swsid, peer, session, m, outputTerminus,
							outputCloud);
				}
			} else {
				Peer peer = cache.getPeerById(destPeerId);
				if (peer == null) {
					return;
				}
				if (!cache.getOnAddress().equals(peer.getOnAddress())) {// 不在此服务器上，则转发
					frame.content()
							.writeBytes(new Gson().toJson(msg).getBytes());
					outputCloud.flow(frame, circuit);
					return;
				}
				pushToPeer(swsid, peer, session, m, outputTerminus,
						outputCloud);
			}
		}
	}

	private void pushToPeer(String toSwsid, Peer peer, MySessionStub session,
			Message msg, IPin outputTerminus, IPin outputCloud)
			throws CircuitException {
		// 留在客户端判断是否是当前视窗
		// if(toSwsid.equals(peer.getSwsid())){//是当前视窗
		//
		// }else{
		//
		// }
		// 是在本服务器上的，则向客户端发送消息
		Frame f = new Frame("push /session/ peer/1.0");
		f.parameter("sid", msg.getSid());
		f.parameter("sender", msg.getSender());
		f.parameter("swsid", toSwsid);
		f.parameter("msgid", msg.getId());
		f.parameter("mailbox", "inbox");
		Circuit c = new Circuit("peer/1.0 200 ok");
		c.attribute("select-id", peer.getSelectId());

		Document doc = Jsoup.parse(webpart);
		Element li = doc.body().select(">li").first();
		Map<String, Object> face = getFace(msg.getSender(), outputCloud);
		pushWebPart(session, msg, face, peer.getSelectId(), li);

		f.content().writeBytes(li.outerHtml().getBytes());
		outputTerminus.flow(f, c);
	}

	private void pushWebPart(MySessionStub session, Message msg,
			Map<String, Object> face, String selectId, Element li)
			throws CircuitException {

		SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:ss");
		String st = format.format(new Date(msg.getSendTime()));
		li.select(".time").html(st);
		String body = new String(msg.getBody());
		body = body.replace("\r\n", "<br>");
		li.select(".box-other>.tag-left>div").html(body);
		// String nick=(String)face.get("nick");
		li.select(".box-other>.person>p").attr(msg.getSender());
		li.select(".box-other>.person>p").html(msg.getSender());
		// if (StringUtil.isEmpty(nick)) {
		// li.select(".box-other>.person>p").html(msg.getSender());
		// } else {
		// li.select(".box-other>.person>p").html(nick);
		// }
		String src = String.format(
				"./resource/ud/%s?path=home://system/img/faces&u=%s",
				face.get("head"), msg.getSender());

		li.select(".box-other>.person>img").attr("src", src);

	}

	private List<String> getAnotherSwsids(String toUser, String sourceSwsid,
			IPin outputCloud) throws CircuitException {
		Frame f = new Frame("getAnotherSwsids /serviceOS/sws/instance sos/1.0");
		f.parameter("sourceSwsid", sourceSwsid);
		f.parameter("toUser", toUser);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		String json = new String(back.content().readFully());
		return new Gson().fromJson(json, new TypeToken<ArrayList<String>>() {
		}.getType());
	}

	private Map<String, Object> getFace(String userCode, IPin outputCloud)
			throws CircuitException {
		Frame f = new Frame("getUserFace /serviceOS/public/user/ sos/1.0");
		f.parameter("userCode", userCode);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		return new Gson().fromJson(new String(back.content().readFully()),
				new TypeToken<Map<String, Object>>() {
				}.getType());
	}
}
