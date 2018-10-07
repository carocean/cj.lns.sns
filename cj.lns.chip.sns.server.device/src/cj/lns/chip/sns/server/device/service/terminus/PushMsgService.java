package cj.lns.chip.sns.server.device.service.terminus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.dao.ISnsAppDao;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IChipInfo;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.Peer;
import cj.studio.ecm.sns.mailbox.Mailbox;
import cj.studio.ecm.sns.mailbox.Message;
import cj.studio.ecm.sns.mailbox.MySession;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

/**
 * 消息推送
 * 
 * <pre>
 *
 * </pre>
 * 
 * @author carocean
 *
 */
@CjService(name = "/pushMsgService")
public class PushMsgService implements ITerminusRequestService, IServiceAfter {
	ILogging logger;
	private String webpart;
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;

	public PushMsgService() {
		logger = CJSystem.current().environment().logging();
	}

	@Override
	public void onAfter(IServiceSite site) {
		IChipInfo info = (IChipInfo) site
				.getService(String.format("$.%s", IChipInfo.class.getName()));
		String resource = info.getResourceProp("resource");
		if (resource.startsWith("/")) {
			resource = resource.substring(1, resource.length());
		}
		String f = String.format("%s/sessionMsgMe.html", resource);
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
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "push":
			push(frame, circuit, outputTerminus, outputCloud);
			break;
		default:
			throw new CircuitException("404",
					String.format("不支持的方法:%s", frame.command()));
		}
	}

	private void push(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		// System.out.println(frame + " \r\n" +
		// frame.content().readableBytes());
		// 1.回推此条消息以显示（带html代码)
		// 2.存储到我的发件箱
		// 3.向im云推送，im云推送到各设备终端
		String sid = frame.parameter("sid");
		String swsid = frame.parameter("swsid");
		String selectUser = frame.parameter("selectUser");
		String sender = frame.parameter("sender");
		Message msg = new Message();
		msg.setBody(frame.content().readFully());
		msg.setSender(sender);
		msg.setSendTime(System.currentTimeMillis());
		msg.setSid(sid);
		msg.setState((byte) 0);
		msg.setSenderOnSws(swsid);

		Document doc = Jsoup.parse(webpart);
		Element li = doc.body().select(">li").first();
//		String selectId = (String) circuit.attribute("select-id");
		Map<String, String> face = getSenderFace(sender, outputCloud);
		List<Peer> peers = cache.getPeers(sender);
		for (Peer peer : peers) {
			pushWebPart(face, msg, peer, li, frame, outputTerminus);
		}
		INetDisk disk = db.getUserDisk(sender);
		ICube swsStore = disk.cube(swsid);
		msg.setIobox(Mailbox.outbox.name());
		String msgid = swsStore.saveDoc(Mailbox.iobox.name(),
				new TupleDocument<Message>(msg));
		msg.setId(msgid);

		IQuery<MySession> q = swsStore.createQuery(
				"select {'tuple':'*'} from tuple ?(tuple) ?(clazz) where {'_id':ObjectId('?(sid)')}");
		q.setParameter("tuple", MySession.KEY_COLL_NAME);
		q.setParameter("clazz", MySession.class.getName());
		q.setParameter("sid", sid);
		IDocument<MySession> mydoc = q.getSingleResult();
		MySession my = mydoc.tuple();
		my.setSid(mydoc.docid());
		// SnsApp app=this.appdao.getApp(my.getAppCode());//到im服务器上查
		// MySessionStub ms=new MySessionStub();
		// ms.fill(my);
		// ms.setSnsApp(app);

		MessageStub st = new MessageStub();
		st.fill(msg);
		pushSessionMessage(st, my, selectUser, outputCloud);
	}

	private void pushSessionMessage(MessageStub msg, MySession session,
			String selectUser, IPin outputCloud) throws CircuitException {
		Frame f = new Frame("pushMsg /im/session.service im/1.0");
		if (!StringUtil.isEmpty(selectUser)
				&& !"undefined".equals(selectUser)) {
			f.parameter("select-user", selectUser);
		}
		f.parameter("app-id", session.getAppId());
		f.parameter("app-code", session.getAppCode());
		f.content().writeBytes(new Gson().toJson(msg).getBytes());
		Circuit c = new Circuit("im/1.0 200 ok");
		outputCloud.flow(f, c);
	}

	private Map<String, String> getSenderFace(String sender, IPin outputCloud)
			throws CircuitException {
		Frame f = new Frame("getUserFace /serviceOS/public/user/ sos/1.0");
		f.parameter("userCode", sender);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		String json = new String(back.content().readFully());
		return new Gson().fromJson(json,
				new TypeToken<HashMap<String, String>>() {
				}.getType());
	}

	private void pushWebPart(Map<String, String> face, Message msg,
			Peer peer, Element li, Frame frame, IPin outputTerminus)
			throws CircuitException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:ss");
		String st = format.format(new Date(msg.getSendTime()));
		li.select(".time").html(st);
		li.select(".box-me>.tag-right>div").html(new String(msg.getBody()));
		li.select(".box-me>.person>p").html(msg.getSender());
//		if (StringUtil.isEmpty(face.get("nick"))) {
//			li.select(".box-me>.person>p").html(msg.getSender());
//		} else {
//			li.select(".box-me>.person>p").html(face.get("nick"));
//		}
		String src =String.format("./resource/ud/%s?path=home://system/img/faces&u=%s", face.get("head"),msg.getSender());
		li.select(".box-me>.person>img").attr("src", src);

		Frame f = new Frame("put /session peer/1.0");
		f.parameter("sid", msg.getSid());
		f.parameter("sender", msg.getSender());
		f.parameter("swsid",peer.getSwsid());
		f.parameter("msgid",msg.getId());
		f.parameter("mailbox","outbox");
		Circuit c = new Circuit(f);
		c.attribute("select-id", peer.getSelectId());
		f.content().writeBytes(li.outerHtml().getBytes());
		outputTerminus.flow(f, c);
	}

}
