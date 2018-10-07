package cj.lns.chip.sns.server.device.dao;

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

@CjService(name = "dynamicDao")
public class DynamicDao implements IDynamicDao {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;

	@Override
	public MySessionStub openSession(String owner, String appCode, String appId,
			String swsid) throws CircuitException {

		SnsApp app = appdao.getApp(appCode);

		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = null;
		if(swsid.equals("/home")){
			store=userdisk.home();
		}else{
			store=userdisk.cube(swsid);
		}
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
			session = createSession(swsid, app, owner, appId,store);
		}
		MySessionStub ms = new MySessionStub();
		ms.fill(session);
		ms.setSnsApp(app);
		return ms;
	}

	private MySession createSession(String swsid, SnsApp app, String owner,
			String appId,ICube store) throws CircuitException {
		//如果会话为空得为应用创建会话，注意：在推送端(pushDynamicMsg.jss.js)也会创建应用的会话
		MySession session = new MySession();
		session.setAppId(appId);
		session.setAppCode(app.getCode());
		session.setCreateTime(System.currentTimeMillis());
		switch(appId){
		case "myarticle":
			session.setIcon("./swssite/develop/img/article.svg");
			session.setTitle("博客");
			break;
		case "myproduct":
			session.setIcon("./swssite/develop/img/store.svg");
			session.setTitle("产品");
			break;
		case "myspace":
			session.setIcon("./swssite/develop/img/file.svg");
			session.setTitle("视窗空间");
			break;
		case "homespace":
			session.setIcon("./swssite/develop/img/file.svg");
			session.setTitle("主空间");
			break;
		case "shuoshuo"://与我相关
			session.setIcon("./swssite/develop/img/chat.svg");
			session.setTitle("说说");
			break;
		case "mine"://与我相关
			session.setIcon("./swssite/develop/img/mine.svg");
			session.setTitle("@与我相关");
			break;
		default:
			throw new CircuitException("503","未定定义会话应用："+appId);
			
		}
		TupleDocument<MySession> doc = new TupleDocument<MySession>(session);
//		INetDisk userdisk = db.getUserDisk(owner);
//		ICube store = userdisk.cube(swsid);
		String sid = store.saveDoc(MySession.KEY_COLL_NAME, doc);
		session.setSid(sid);
		return session;
	}

}
