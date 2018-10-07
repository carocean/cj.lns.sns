package cj.lns.chip.sns.server.device.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
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

@CjService(name = "chatGroupDao")
public class ChatGroupDao implements IChatGroupDao {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;

	@Override
	public List<HashMap<String, String>> getUsers(String appId) {
		ICube home = db.getLnsDataHome();
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.gid':'?(gid)'}";
		IQuery<HashMap<String, String>> q = home.createQuery(cjql);
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("colName", "sns.chatGroup.users");
		q.setParameter("gid", appId);
		List<IDocument<HashMap<String, String>>> list = q.getResultList();
		List<HashMap<String, String>> result = new ArrayList<>();
		for (IDocument<HashMap<String, String>> doc : list) {
			result.add(doc.tuple());
		}
		return result;
	}

	@Override
	public MySessionStub openSession(String owner, String appCode, String appId,
			String swsid) throws CircuitException {
		
		SnsApp app = appdao.getApp(appCode);

		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);

		MySession session = null;

		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.appId':'?(appId)','tuple.appCode':'?(code)'}";
		IQuery<MySession> lq = store.createQuery(cjql);
		lq.setParameter("colName", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("code", appCode);
		lq.setParameter("appId", appId);
		IDocument<MySession> sessionDoc = lq.getSingleResult();
		if (sessionDoc != null) {
			session = sessionDoc.tuple();
			session.setSid(sessionDoc.docid());
		} else {// 创建会话
			ICube home = db.getLnsDataHome();
			cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'_id':ObjectId('?(appId)')}";
			IQuery<Map<String, Object>> q = home.createQuery(cjql);
			q.setParameter("colName", "sns.chatGroup");
			q.setParameter("clazz", HashMap.class.getName());
			q.setParameter("appId", appId);
			IDocument<Map<String, Object>> doc = q.getSingleResult();

			session = createSession(swsid, app, owner, appId, doc.tuple());
		}
		MySessionStub ms = new MySessionStub();
		ms.fill(session);
		ms.setSnsApp(app);
		return ms;
	}

	private MySession createSession(String swsid, SnsApp app, String owner,
			String appId, Map<String, Object> map) {

		MySession session = new MySession();
		session.setAppId(appId);
		session.setAppCode(app.getCode());
		session.setCreateTime(System.currentTimeMillis());
		String src = String.format("./resource/dd/%s?path=home://%s",
				map.get("headFile"),
				String.format("/chatGroups/%s/head", appId));
		session.setIcon(src);
		session.setTitle((String) map.get("name"));
		TupleDocument<MySession> doc = new TupleDocument<MySession>(session);
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);
		String sid = store.saveDoc(MySession.KEY_COLL_NAME, doc);
		session.setSid(sid);
		return session;
	}

}
