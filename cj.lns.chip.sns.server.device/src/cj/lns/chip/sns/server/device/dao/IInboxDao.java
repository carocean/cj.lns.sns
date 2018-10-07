package cj.lns.chip.sns.server.device.dao;

import java.util.List;

import cj.studio.ecm.sns.mailbox.Message;

public interface IInboxDao {

	void saveMessage(String owner,String swsid,Message m);

	long totalMessage(String swsid, String onUser);

	long totalMessage(String swsid, List<String> sessionIds, String owner);


}
