package cj.lns.chip.sns.server.device.dao;

import java.util.HashMap;
import java.util.List;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;

public interface IChatGroupDao {

	List<HashMap<String, String>> getUsers(String gid);


	MySessionStub openSession(String owner, String appCode, String appId,
			String swsid) throws CircuitException;

}
