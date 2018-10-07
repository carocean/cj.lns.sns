package cj.lns.chip.sns.server.device.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sns.server.device.service.Chatroom;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.MySession;
import cj.studio.ecm.sns.mailbox.SnsApp;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;

@CjService(name = "chatroomDao")
public class ChatroomDao implements IChatroomDao {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;

	@Override
	public List<HashMap<String, String>> getUsers(String crid) {
		ICube home = db.getLnsDataHome();
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.crid':'?(crid)'}";
		IQuery<HashMap<String, String>> q = home.createQuery(cjql);
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("colName", Chatroom.KEY_COLL_USERS);
		q.setParameter("crid", crid);
		List<IDocument<HashMap<String, String>>> list = q.getResultList();
		q = home.createQuery(
				"select {'tuple':'*'} from tuple sns.chatroom java.util.HashMap where {'_id':ObjectId('?(crid)')}");
		q.setParameter("crid", crid);
		IDocument<HashMap<String, String>> creator = q.getSingleResult();
		
		List<HashMap<String, String>> result = new ArrayList<>();
		HashMap<String,String> cuser=new HashMap<>();
		cuser.put("user", creator.tuple().get("creator"));
		cuser.put("crid", crid);
		result.add(cuser);
		for (IDocument<HashMap<String, String>> doc : list) {
			result.add(doc.tuple());
		}
		return result;
	}
	@Override
	public MySessionStub openSessionByCrid(String crid,String owner,
			String appCode, String swsid, String icon, String title)
			throws CircuitException {
		SnsApp app = appdao.getApp(appCode);
		String appIcon = app.getIcon();
		String appName = app.getName();

		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.appId':'?(appId)','tuple.appCode':'?(code)'}";
		IQuery<MySession> lq = store.createQuery(cjql);
		lq.setParameter("colName", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("code", appCode);
		lq.setParameter("appId", crid);
		IDocument<MySession> sessionDoc = lq.getSingleResult();
		MySession session=null;
		if (sessionDoc != null) {
			session = sessionDoc.tuple();
			session.setSid(sessionDoc.docid());
		} else {// 创建会话
			session = createSession(swsid, appCode, owner, icon, title, 
					crid);
		}

		SnsApp sa = new SnsApp(appCode, appName, appIcon);
		MySessionStub ms = new MySessionStub();
		ms.fill(session);
		ms.setSnsApp(sa);
		return ms;

	}

	@Override
	public MySessionStub openSession(String chatTo, String sender,
			String appCode, String swsid, String icon, String title)
			throws CircuitException {
		// String memoName = frame.parameter("memo-name");
		// 判断主对话人是否存在于im.chatroom表中如果不存在则创建聊天室和主对话人
		ICube home = db.getLnsDataHome();
		SnsApp app = appdao.getApp(appCode);
		String appIcon = app.getIcon();
		String appName = app.getName();

		INetDisk userdisk = db.getUserDisk(sender);
		ICube store = userdisk.cube(swsid);
//		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {$or:[{'tuple.creator':'?(creator)','tuple.chatTo':'?(chatTo)'},{'tuple.creator':'?(chatTo)','tuple.chatTo':'?(creator)'}],'tuple.swsid':'?(swsid)'}";
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {$or:[{'tuple.creator':'?(creator)','tuple.chatTo':'?(chatTo)'},{'tuple.creator':'?(chatTo)','tuple.chatTo':'?(creator)'}]}";
		IQuery<Map<String, String>> q = home.createQuery(cjql);
		q.setParameter("colName", Chatroom.KEY_COLL_NAME);
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("chatTo", chatTo);
		q.setParameter("creator", sender);
		q.setParameter("swsid", swsid);
		IDocument<Map<String, String>> chatroom = q.getSingleResult();
		MySession session = null;
		if (chatroom != null) {// 如果有聊天室，则到持有者的视窗内查看有没有该聊天室的会话，没有则创建会话有则打开。
			cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.appId':'?(appId)','tuple.appCode':'?(code)'}";
			IQuery<MySession> lq = store.createQuery(cjql);
			lq.setParameter("colName", MySession.KEY_COLL_NAME);
			lq.setParameter("clazz", MySession.class.getName());
			lq.setParameter("code", appCode);
			lq.setParameter("appId", chatroom.docid());
			IDocument<MySession> sessionDoc = lq.getSingleResult();
			if (sessionDoc != null) {
				session = sessionDoc.tuple();
				session.setSid(sessionDoc.docid());
			} else {// 创建会话
				session = createSession(swsid, appCode, sender, icon, title,
						 chatroom.docid());
			}

		} else {// 聊天室不存在会话也不存在

			Chatroom cr = createChatroom(swsid, sender, chatTo, home);
			session = createSession(swsid, appCode, sender, icon, title, 
					cr.getId());
		}
		SnsApp sa = new SnsApp(appCode, appName, appIcon);
		MySessionStub ms = new MySessionStub();
		ms.fill(session);
		ms.setSnsApp(sa);
		return ms;
	}

	public Chatroom createChatroom(String swsid, String owner, String chatTo,
			ICube home) {
		// 先创建聊天室chatroom,聊天室是用户之间共享的，因此存储在系统磁盘中

		Chatroom cr = new Chatroom(owner, swsid, chatTo);
		String crid = home.saveDoc(Chatroom.KEY_COLL_NAME,
				new TupleDocument<Chatroom>(cr));
		cr.setId(crid);
		// 肯定没有联系人表，因此创建
		Map<String, String> clc = new HashMap<>();
		clc.put("crid", crid);
		clc.put("user", chatTo);
		TupleDocument<Map<String, String>> clcDoc = new TupleDocument<Map<String, String>>(
				clc);
		home.saveDoc(Chatroom.KEY_COLL_USERS, clcDoc);

		return cr;
	}

	public MySession createSession(String swsid, String appCode, String owner,
			String icon, String title, String crid) {
		MySession session = new MySession();
		session.setAppId(crid);
		session.setAppCode(appCode);
		session.setCreateTime(System.currentTimeMillis());
		session.setIcon(icon);
		session.setTitle(title);
		TupleDocument<MySession> doc = new TupleDocument<MySession>(session);
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);
		String sid = store.saveDoc(MySession.KEY_COLL_NAME, doc);
		session.setSid(sid);
		return session;
	}
}
