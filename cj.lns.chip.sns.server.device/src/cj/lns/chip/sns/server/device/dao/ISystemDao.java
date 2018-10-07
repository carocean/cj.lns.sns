package cj.lns.chip.sns.server.device.dao;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;

public interface ISystemDao {

	MySessionStub openSession(String appId, String owner, String appCode,
			String swsid) throws CircuitException;

}
