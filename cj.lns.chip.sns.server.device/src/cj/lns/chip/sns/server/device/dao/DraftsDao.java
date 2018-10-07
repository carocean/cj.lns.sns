package cj.lns.chip.sns.server.device.dao;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.lns.chip.sos.disk.INetDisk;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.sns.mailbox.Mailbox;
import cj.studio.ecm.sns.mailbox.Message;

@CjService(name="draftsDao")
public class DraftsDao implements IDraftsDao{
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;

	@Override
	public void saveMessage(String owner,String swsid,Message m) {
		m.setArriveTime(System.currentTimeMillis());
		INetDisk userdisk = db.getUserDisk(owner);
		ICube store = userdisk.cube(swsid);
		String msgid=store.saveDoc(Mailbox.drafts.name(), new TupleDocument<Message>(m));
		m.setId(msgid);
	}

}
