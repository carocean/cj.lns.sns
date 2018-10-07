package cj.lns.chip.sns.server.device.dao;

import java.util.HashMap;
import java.util.List;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;

public interface IChatroomDao {

	List<HashMap<String, String>> getUsers(String crid);

	MySessionStub openSession(String uid, String owner, String appCode, String swsid,
			String icon, String title) throws CircuitException;

	MySessionStub openSessionByCrid(String crid, String owner, String appCode,
			String swsid, String icon, String title) throws CircuitException;

}
