package cj.lns.chip.sns.server.device.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.sns.mailbox.Mailbox;
import cj.studio.ecm.sns.mailbox.MySession;
@CjService(name="sessionDao")
public class SessionDao implements ISessionDao {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;

	@Override
	public void deleteSession(String uid, String swsid, String sid) {
		INetDisk disk = db.getUserDisk(uid);
		ICube store = null;
		if(swsid.equals("/home")){
			store=disk.home();
		}else{
			store=disk.cube(swsid);
		}
		String bson = String.format("{'tuple.sid':'%s'}", sid);
		store.deleteDocs(Mailbox.iobox.name(), bson);
		store.deleteDocs(Mailbox.drafts.name(), bson);
		store.deleteDocs(Mailbox.trash.name(), bson);
		store.deleteDoc(MySession.KEY_COLL_NAME, sid);
	}
	@Override
	public List<String> getAllAppSessions(String uid, String swsid,String appCode) {
		INetDisk disk = db.getUserDisk(uid);
		ICube store = null;
		if(swsid.equals("/home")){
			store=disk.home();
		}else{
			store=disk.cube(swsid);
		}
		String cjql="select {'tuple.appId':1} from tuple sns.session java.util.HashMap where {'tuple.appCode':'?(appCode)'}";
		IQuery<Map<String,Object>> q=store.createQuery(cjql);
		q.setParameter("appCode", appCode);
		List<IDocument<Map<String, Object>>> result=q.getResultList();
		List<String> ids=new ArrayList<>();
		for(IDocument<Map<String, Object>> doc:result){
			ids.add(String.format("'%s'", doc.docid()));
		}
		return ids;
	}
}
