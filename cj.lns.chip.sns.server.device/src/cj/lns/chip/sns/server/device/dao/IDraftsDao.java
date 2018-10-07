package cj.lns.chip.sns.server.device.dao;

import cj.studio.ecm.sns.mailbox.Message;

public interface IDraftsDao {

	void saveMessage(String owner, String swsid, Message m);

}
