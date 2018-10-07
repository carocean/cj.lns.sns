package cj.lns.chip.sns.server.device.dao;

import java.util.List;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.sns.mailbox.Mailbox;
import cj.studio.ecm.sns.mailbox.Message;
@CjService(name="inboxDao")
public class InboxDao implements IInboxDao {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;

	@Override
	public void saveMessage(String owner,String swsid,Message m) {
		m.setArriveTime(System.currentTimeMillis());
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = null;
		if(swsid.equals("/home")){
			store=userdisk.home();
		}else{
			store=userdisk.cube(swsid);
		}
		m.setIobox(Mailbox.inbox.name());
		String msgid=store.saveDoc(Mailbox.iobox.name(), new TupleDocument<Message>(m));
		m.setId(msgid);
	}
	@Override
	public long totalMessage(String swsid, String owner) {
		String cjql="select {'tuple':'*'}.count() from tuple ?(colName) java.lang.Long where {'tuple.state':0,'tuple.iobox':'inbox'}";
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = null;
		if(swsid.equals("/home")){
			store=userdisk.home();
		}else{
			store=userdisk.cube(swsid);
		}
		IQuery<Long> q=store.createQuery(cjql);
		q.setParameter("colName", Mailbox.iobox.name());
		return q.count();
	}
	@Override
	public long totalMessage(String swsid,List<String> sessionIds, String owner) {
		String cjql="select {'tuple':'*'}.count() from tuple ?(colName) java.lang.Long where {'tuple.state':0,'tuple.sid':{$in:?(sid)},'tuple.iobox':'inbox'}";
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = null;
		if(swsid.equals("/home")){
			store=userdisk.home();
		}else{
			store=userdisk.cube(swsid);
		}
		IQuery<Long> q=store.createQuery(cjql);
		q.setParameter("colName", Mailbox.iobox.name());
		q.setParameter("sid", sessionIds);
		return q.count();
	}
}
