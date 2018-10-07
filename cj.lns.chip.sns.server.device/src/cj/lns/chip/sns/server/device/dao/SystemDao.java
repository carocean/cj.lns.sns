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
@CjService(name="systemDao")
public class SystemDao implements ISystemDao{
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName = "snsAppDao")
	ISnsAppDao appdao;
	@Override
	public MySessionStub openSession(String appId, String owner, String appCode,
			String swsid) throws CircuitException {
		SnsApp app = appdao.getApp(appCode);
		String appIcon = app.getIcon();
		String appName = app.getName();

		String itemAppCode="";
//		String itemAppId="";
		int pos=appId.indexOf(".");
		if(pos>0){
			itemAppCode=appId.substring(0,pos);
//			itemAppId=appId.substring(pos+1,appId.length());
		}
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'tuple.appId':'?(appId)','tuple.appCode':'?(code)'}";
		IQuery<MySession> lq = store.createQuery(cjql);
		lq.setParameter("colName", MySession.KEY_COLL_NAME);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("code", appCode);
		if(pos>0){
		lq.setParameter("appId", itemAppCode);
		}else{
			lq.setParameter("appId", appId);
		}
		IDocument<MySession> sessionDoc = lq.getSingleResult();
		MySession session=null;
		if (sessionDoc != null) {
			session = sessionDoc.tuple();
			session.setSid(sessionDoc.docid());
		} else {// 创建会话
//			String itemAppCode="";
//			String itemAppId="";
//			int pos=appId.indexOf(".");
			if(pos>0){
//				itemAppCode=appId.substring(0,pos);
//				itemAppId=appId.substring(pos+1,appId.length());
				SnsApp itemApp=appdao.getApp(itemAppCode);
				session = createSession(swsid, appCode, owner, itemApp.getIcon(), itemApp.getName(), 
						itemAppCode);
			}else{
				session = createSession(swsid, appCode, owner, appIcon, appName, 
						appId);
			}
		}

		SnsApp sa = new SnsApp(appCode, appName, appIcon);
		MySessionStub ms = new MySessionStub();
		ms.fill(session);
		ms.setSnsApp(sa);
		return ms;
	}
	public MySession createSession(String swsid, String appCode, String owner,
			String icon, String title, String appId) {
		MySession session = new MySession();
		session.setAppId(appId);
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
