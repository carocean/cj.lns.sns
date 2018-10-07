package cj.lns.chip.sns.server.device.service.mailbox;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sns.server.device.dao.ISessionDao;
import cj.lns.chip.sns.server.device.dao.ISnsAppDao;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.MyJsonWriter;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.net.nio.NetConstans;
import cj.studio.ecm.sns.mailbox.Mailbox;
import cj.studio.ecm.sns.mailbox.Message;
import cj.studio.ecm.sns.mailbox.MySession;
import cj.studio.ecm.sns.mailbox.SnsApp;
import cj.studio.ecm.sns.mailbox.viewer.InboxSessionViewer;
import cj.studio.ecm.sns.mailbox.viewer.MailboxMessageViewer;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;
import cj.studio.ecm.sns.mailbox.viewer.SnsAppStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

@CjService(name = "/mailbox/inbox")
public class InboxService implements IInboxService {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;
	@CjServiceRef
	ISessionDao sessionDao;
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "getCyberport":
			getSessions(frame, circuit);
			// test();
			break;
		case "getMessageTotal":
			getMessageTotal(frame, circuit);
			break;
		case "getAllMessage":
			getAllMessage(frame, circuit);
			break;
		case "getUnreadedMessages":
			getUnreadedMessages(frame, circuit);
			break;
		case "flagMessageReaded":
			flagMessageReaded(frame, circuit);
			break;
		case "flagMessageReadedByAppCode":
			flagMessageReadedByAppCode(frame, circuit);
			break;
		case "flagMessageOneReaded":
			flagMessageOneReaded(frame, circuit, outputCloud);
			break;
		default:
			throw new CircuitException("503",
					String.format("不支持的指令：%s", frame.command()));
		}
	}

	

	private void flagMessageOneReaded(Frame frame, Circuit circuit,
			IPin outputCloud) throws CircuitException {

		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String mid = frame.parameter("msgid");
		if (StringUtil.isEmpty(mid)) {
			throw new CircuitException("503", "侦的参数：mid为空");
		}
		String owner = getServicewsOwner(swsid, outputCloud);
		INetDisk disk = db.getUserDisk(owner);
		ICube cube = disk.cube(swsid);
		Bson filter = Document
				.parse(String.format("{'_id':ObjectId('%s')}", mid));
		Bson update = Document.parse(
				String.format("{'$set':{'tuple.state':1,'tuple.readTime':%s}}",
						System.currentTimeMillis()));
		cube.updateDocs(Mailbox.iobox.name(), filter, update);
	}

	private String getServicewsOwner(String swsid, IPin outputCloud)
			throws CircuitException {
		Frame f = new Frame(
				"getServicewsOwner /serviceOS/sws/instance sos/1.0");
		f.parameter("swsid", swsid);
		Circuit c = new Circuit("sos/1.0 200 OK");
		f.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC_TIMEOUT, "15000");//
		// 等待15秒
		try {
			outputCloud.flow(f, c);
			if ("frame/bin".equals(c.contentType())) {
				Frame back = new Frame(c.content().readFully());
				if (!"200".equals(back.head("status"))) {
					throw new CircuitException(back.head("status"), String
							.format("在远程服务器上出现错误。原因：%s", back.head("message")));
				}
				return back.head("owner");
			}
			return "";
		} catch (CircuitException e1) {
			throw e1;
		}
	}
	private void flagMessageReadedByAppCode(Frame frame, Circuit circuit) throws CircuitException {
		String owner = frame.parameter("owner");
		if (StringUtil.isEmpty(owner)) {
			throw new CircuitException("503", "侦的参数：owner为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String appCode = frame.parameter("appCode");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：appCode为空");
		}
		List<String> sids=sessionDao.getAllAppSessions(owner, swsid, appCode);
		INetDisk disk = db.getUserDisk(owner);
		ICube cube = disk.cube(swsid);
		Bson filter = Document.parse(String.format("{'tuple.sid':{$in:%s}}", sids));
		Bson update = Document.parse(
				String.format("{'$set':{'tuple.state':1,'tuple.readTime':%s}}",
						System.currentTimeMillis()));
		cube.updateDocs(Mailbox.iobox.name(), filter, update);
	}
	private void flagMessageReaded(Frame frame, Circuit circuit)
			throws CircuitException {
		String owner = frame.parameter("owner");
		if (StringUtil.isEmpty(owner)) {
			throw new CircuitException("503", "侦的参数：owner为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String sid = frame.parameter("sid");
		if (StringUtil.isEmpty(sid)) {
			throw new CircuitException("503", "侦的参数：sid为空");
		}
		INetDisk disk = db.getUserDisk(owner);
		ICube cube = disk.cube(swsid);
		Bson filter = Document.parse(String.format("{'tuple.sid':'%s'}", sid));
		Bson update = Document.parse(
				String.format("{'$set':{'tuple.state':1,'tuple.readTime':%s}}",
						System.currentTimeMillis()));
		cube.updateDocs(Mailbox.iobox.name(), filter, update);
	}

	private void getUnreadedMessages(Frame frame, Circuit circuit)
			throws CircuitException {
		String user = frame.parameter("uid");
		if (StringUtil.isEmpty(user)) {
			throw new CircuitException("503", "侦的参数：uid为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String sid = frame.parameter("sid");
		if (StringUtil.isEmpty(sid)) {
			throw new CircuitException("503", "侦的参数：sid为空");
		}
		String skip = frame.parameter("skip");
		if (StringUtil.isEmpty(skip)) {
			throw new CircuitException("503", "侦的参数：skip为空");
		}
		String limit = frame.parameter("limit");
		if (StringUtil.isEmpty(limit)) {
			throw new CircuitException("503", "侦的参数：limit为空");
		}
		String sort = frame.parameter("sort");
		if (StringUtil.isEmpty(sid)) {
			sort = "-1";
		}
		INetDisk disk = db.getUserDisk(user);
		ICube cube = disk.cube(swsid);
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.arriveTime':%s,'tuple.state':1}).skip(%s).limit(%s) from tuple ?(colName) ?(clazz) where {'tuple.state':0,'tuple.iobox':'inbox','tuple.sid':'?(sid)'}",
				sort, skip, limit);
		IQuery<Message> qm = cube.createQuery(cjql);
		qm.setParameter("colName", Mailbox.iobox.name());
		qm.setParameter("clazz", Message.class.getName());
		qm.setParameter("sid", sid);
		List<IDocument<Message>> msgs = qm.getResultList();
		List<MessageStub> result = new ArrayList<>();
		for (IDocument<Message> doc : msgs) {
			Message msg = doc.tuple();
			msg.setId(doc.docid());
			MessageStub m = new MessageStub();
			m.setArriveTime(msg.getArriveTime());
			m.setBody(msg.getBody());
			m.setId(msg.getId());
			m.setReadTime(msg.getReadTime());
			m.setReplyid(msg.getReplyid());
			m.setSender(msg.getSender());
			m.setSendTime(msg.getSendTime());
			m.setSid(msg.getSid());
			m.setState(msg.getState());
			result.add(m);
		}
		circuit.content().writeBytes(new Gson().toJson(result).getBytes());
	}

	private void getAllMessage(Frame frame, Circuit circuit)
			throws CircuitException {
		String user = frame.parameter("user");
		if (StringUtil.isEmpty(user)) {
			throw new CircuitException("503", "侦的参数：user为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String skip = frame.parameter("skip");
		if (StringUtil.isEmpty(skip)) {
			throw new CircuitException("503", "侦的参数：skip为空");
		}
		String limit = frame.parameter("limit");
		if (StringUtil.isEmpty(limit)) {
			throw new CircuitException("503", "侦的参数：limit为空");
		}
		String mailbox = frame.parameter("mailbox");
		if (StringUtil.isEmpty(mailbox)) {
			throw new CircuitException("503", "侦的参数：mailbox为空");
		}
		MailboxMessageViewer view = new MailboxMessageViewer();

		INetDisk disk = db.getUserDisk(user);
		ICube cube = disk.cube(swsid);
		String cjql = "";
		cjql = String.format(
					"select {'tuple':'*'}.sort({'tuple.sendTime':-1,'tuple.state':1}).skip(%s).limit(%s) from tuple ?(colName) ?(clazz) where {'tuple.iobox':'%s'}",
					skip, limit,mailbox);

		IQuery<Message> qm = cube.createQuery(cjql);
		qm.setParameter("colName", Mailbox.iobox.name());
		qm.setParameter("clazz", Message.class.getName());
		List<IDocument<Message>> msgs = qm.getResultList();
		for (IDocument<Message> doc : msgs) {
			Message msg = doc.tuple();
			msg.setId(doc.docid());
			MessageStub m = new MessageStub();
			m.setArriveTime(msg.getArriveTime());
			m.setBody(msg.getBody());
			m.setId(msg.getId());
			m.setReadTime(msg.getReadTime());
			m.setReplyid(msg.getReplyid());
			m.setSender(msg.getSender());
			m.setSendTime(msg.getSendTime());
			m.setSid(msg.getSid());
			m.setState(msg.getState());
			view.getMessages().put(msg.getId(), m);
		}
		// 求会话列表
		cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {}";
		IQuery<MySession> lq = cube.createQuery(cjql);
		lq.setParameter("colName", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		List<IDocument<MySession>> docs = lq.getResultList();
		Map<String, SnsApp> apps = appdao.getAll();
		for (IDocument<MySession> doc : docs) {
			MySession session = doc.tuple();
			session.setSid(doc.docid());
			MySessionStub stub = new MySessionStub();
			stub.setAppId(session.getAppId());
			stub.setCreateTime(session.getCreateTime());
			stub.setIcon(session.getIcon());
			stub.setNewestMsg(null);
			stub.setProfile(session.getProfile());
			stub.setSid(session.getSid());
			SnsApp app = apps.get(session.getAppCode());
			stub.setSnsApp(app);
			stub.setTitle(session.getTitle());
			stub.setTotalMsgs(0);
			view.getSessions().put(doc.docid(), stub);
		}
		circuit.content().writeBytes(new Gson().toJson(view).getBytes());
	}

	private void getMessageTotal(Frame frame, Circuit circuit)
			throws CircuitException {
		String user = frame.parameter("user");
		if (StringUtil.isEmpty(user)) {
			throw new CircuitException("503", "侦的参数：user为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		Map<String, Long> report = new HashMap<>();

		INetDisk disk = db.getUserDisk(user);
		ICube cube = disk.cube(swsid);

		String cjql = "select {'tuple':'*'}.count() from tuple ?(colName) ?(clazz) where {'tuple.iobox':'inbox'}";
		IQuery<Long> ql = cube.createQuery(cjql);
		ql.setParameter("colName", Mailbox.iobox.name());
		ql.setParameter("clazz", Long.class.getName());
		report.put(Mailbox.inbox.name(), ql.count());

		cjql = "select {'tuple':'*'}.count() from tuple ?(colName) ?(clazz) where {'tuple.iobox':'outbox'}";
		ql = cube.createQuery(cjql);
		ql.setParameter("colName", Mailbox.iobox.name());
		ql.setParameter("clazz", Long.class.getName());
		report.put(Mailbox.outbox.name(), ql.count());

		cjql = "select {'tuple':'*'}.count() from tuple ?(colName) ?(clazz) where {}";
		ql = cube.createQuery(cjql);
		ql.setParameter("colName", Mailbox.drafts.name());
		ql.setParameter("clazz", Long.class.getName());
		report.put(Mailbox.drafts.name(), ql.count());

		cjql = "select {'tuple':'*'}.count() from tuple ?(colName) ?(clazz) where {}";
		ql = cube.createQuery(cjql);
		ql.setParameter("colName", Mailbox.trash.name());
		ql.setParameter("clazz", Long.class.getName());
		report.put(Mailbox.trash.name(), ql.count());

		circuit.content().writeBytes(new Gson().toJson(report).getBytes());
	}

	private void getSessions(Frame frame, Circuit circuit)
			throws CircuitException {
		String user = frame.parameter("user");
		if (StringUtil.isEmpty(user)) {
			throw new CircuitException("503", "侦的参数：user为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}

		INetDisk disk = db.getUserDisk(user);
		ICube cube = disk.cube(swsid);
		/* 
		  {$match:{'tuple.state':0}}
		  {$sort:{'tuple.arriveTime':-1}}
		  {$group:{_id:'$tuple.sid',newestMsg:{$first:'$tuple'},countMsgs:{'$sum':1}}}
		*/
		List<Document> pipelines = new ArrayList<>();
		pipelines.add(Document.parse("{$match:{'tuple.state':0,'tuple.iobox':'inbox'}}"));
		pipelines.add(Document.parse("{$sort:{'tuple.arriveTime':-1}}"));
		pipelines.add(Document.parse(
				"{$group:{_id:'$tuple.sid',newestMsg:{$first:'$tuple'},countMsg:{'$sum':1}}}"));
		AggregateIterable<Document> result = cube
				.aggregate(Mailbox.iobox.name(), pipelines);
		MongoCursor<Document> it = result.iterator();
		Map<String, Document> sessmap = new LinkedHashMap<>();
		List<String> sidlist = new ArrayList<>();
		while (it.hasNext()) {
			Document doc = it.next();
			String sid = doc.getString("_id");
			sessmap.put(sid, doc);
			sidlist.add(String.format("ObjectId('%s')", sid));
		}

		// 求有我消息的由我创建的所有会话
		String cjql = "select {'tuple':'*'}.sort({'tuple.createTime':-1}) from tuple ?(session) ?(clazz) where {'_id':{$in:?(ids)}}";
		IQuery<MySession> lq = cube.createQuery(cjql);
		lq.setParameter("session", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("ids", sidlist);
		List<IDocument<MySession>> sessionDocs = lq.getResultList();
		// 求无我消息的由我创建的所有会话
		cjql = "select {'tuple':'*'}.sort({'tuple.createTime':-1}) from tuple ?(session) ?(clazz) where {'_id':{$nin:?(ids)}}";
		lq = cube.createQuery(cjql);
		lq.setParameter("session", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("ids", sidlist);
		List<IDocument<MySession>> noMsgSessionDocs = lq.getResultList();
		// 将不在的追加在有消息的会话列表后面
		for (IDocument<MySession> doc : noMsgSessionDocs) {
			sessionDocs.add(doc);
		}

		// 以下组装视图
		Map<String, SnsAppStub> apps = new LinkedHashMap<>();
		Map<String, SnsApp> map = appdao.getAll();
		for (IDocument<MySession> doc : sessionDocs) {
			MySession session = doc.tuple();
			session.setSid(doc.docid());
			SnsApp app = map.get(session.getAppCode());
			SnsAppStub info = null;
			if (!apps.containsKey(app.getCode())) {
				info = new SnsAppStub(app.getCode(), app.getName(),
						app.getIcon());
				apps.put(info.getCode(), info);
			} else {
				info = apps.get(app.getCode());
			}

			List<MySessionStub> mylist = info.getSessions();
			MySessionStub si = new MySessionStub();
			mylist.add(si);
			si.setAppId(session.getAppId());
			si.setCreateTime(session.getCreateTime());
			si.setIcon(session.getIcon());
			si.setProfile(session.getProfile());
			si.setSid(session.getSid());
			si.setSnsApp(app);
			si.setTitle(session.getTitle());

			if (sessmap.containsKey(doc.docid())) {// 如果会话有消息,则为会话装入最新消息提醒
				Document tuple = sessmap.get(doc.docid());
				Document newestDoc = (Document) tuple.get("newestMsg");
				MyJsonWriter writer = new MyJsonWriter(new StringWriter(),
						new JsonWriterSettings());
				new DocumentCodec().encode(writer, newestDoc, EncoderContext
						.builder().isEncodingCollectibleDocument(true).build());
				String json = writer.getWriter().toString();
				Message newestMsg = new Gson().fromJson(json, Message.class);
				si.setNewestMsg(newestMsg);
				si.setTotalMsgs(tuple.getInteger("countMsg"));

			}
		}
		List<SnsAppStub> ret = new ArrayList<>();
		Iterator<Entry<String, SnsAppStub>> itapp = apps.entrySet().iterator();
		while (itapp.hasNext()) {
			Entry<String, SnsAppStub> en = itapp.next();
			ret.add(en.getValue());
		}
		InboxSessionViewer viewer = new InboxSessionViewer(ret);
		String json = new Gson().toJson(viewer);
		circuit.content().writeBytes(json.getBytes());
	}

}
