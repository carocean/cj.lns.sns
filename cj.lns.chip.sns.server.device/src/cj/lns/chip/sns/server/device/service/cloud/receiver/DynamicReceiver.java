package cj.lns.chip.sns.server.device.service.cloud.receiver;

import java.util.ArrayList;
import java.util.List;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.dao.IDynamicDao;
import cj.lns.chip.sns.server.device.dao.IInboxDao;
import cj.lns.chip.sns.server.device.dao.ISessionDao;
import cj.lns.chip.sns.server.device.service.cloud.IAppReceiver;
import cj.lns.chip.sns.server.device.service.cloud.ProcessMessageService;
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

@CjService(name = "dynamic")
public class DynamicReceiver implements IAppReceiver {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	@CjServiceRef
	IDynamicDao dynamicDao;
	@CjServiceRef
	IInboxDao inboxDao;
	@CjServiceRef
	ISessionDao sessionDao;
	@CjServiceInvertInjection
	@CjServiceRef(refByName = "/processMessage.service")
	ProcessMessageService process;

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
		if(msg.getSwstid().equals("-1")){//发给对方用户主空间
			swsids=new ArrayList<>();
			swsids.add("/home");//注意：swsid在此表示要发往用户的哪个视窗，如果是/home即表示发往用户的主空间，因此并不一定表示视窗号
		}else{
			swsids = getAnotherSwsids(destUser, msg.getSenderOnSws(),
				outputCloud);
		}
		// if(StringUtil.isEmpty(swsid)){//如果视窗为空，则在终端的信息港的任务列表中列出申请新视窗的消息，以让用户判断是否申请新视窗，该功能在未来实现
		// 任务的消息是非会话消息。
		//
		// }
		if(swsids.isEmpty()&&!swsids.contains("/home")){//发给对方用户主空间
			swsids.add("/home");
		}
		for (String swsid : swsids) {
			MySessionStub session = dynamicDao.openSession(destUser, appCode,
					appid, swsid);
			msg.setSid(session.getSid());
			Message m = new Message();
			msg.fillTo(m);
			inboxDao.saveMessage(destUser, swsid, m);

			List<String> sessions = sessionDao.getAllAppSessions(destUser,
					swsid, "dynamic");
			long unreadmsgCount = inboxDao.totalMessage(swsid, sessions,
					destUser);

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
					pushToPeer(swsid, unreadmsgCount, peer, session,
							m, outputTerminus, outputCloud);
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
				pushToPeer(swsid, unreadmsgCount, peer, session, m,
						outputTerminus, outputCloud);
			}
		}
	}

	private void pushToPeer(String toSwsid, long unreadmsgCount, Peer peer,
			MySessionStub session, Message msg, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		// 是在本服务器上的，则向客户端发送消息
		Frame f = new Frame("push /dynamic/ peer/1.0");
		f.parameter("sid", msg.getSid());
		f.parameter("appId", session.getAppId());
		f.parameter("sender", msg.getSender());
		f.parameter("swsid", toSwsid);//也可能不是发往视窗号，或者是/home表示发往用户的主空间
		f.parameter("msgid", msg.getId());
		f.parameter("mailbox", "inbox");
		f.parameter("unreadMsgCount", String.valueOf(unreadmsgCount));
		Circuit c = new Circuit("peer/1.0 200 ok");
		c.attribute("select-id", peer.getSelectId());

		// Map<String,Object> face=getFace(msg.getSender(),outputCloud);

		// f.content().writeBytes(li.outerHtml().getBytes());
		outputTerminus.flow(f, c);
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
	// private Map<String, Object> getFace(String userCode, IPin outputCloud)
	// throws CircuitException {
	// Frame f = new Frame("getUserFace /serviceOS/public/user/ sos/1.0");
	// f.parameter("userCode", userCode);
	// Circuit c = new Circuit("sos/1.0 200 ok");
	// outputCloud.flow(f, c);
	// Frame back = new Frame(c.content().readFully());
	// if (!"200".equals(back.head("status"))) {
	// throw new CircuitException(back.head("status"),
	// String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
	// }
	// return new Gson().fromJson(new String(back.content().readFully()),
	// new TypeToken<Map<String, Object>>() {
	// }.getType());
	// }
}
