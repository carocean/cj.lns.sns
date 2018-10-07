package cj.lns.chip.sns.server.device.dao;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;

public interface IDynamicDao {



	MySessionStub openSession(String owner, String appCode, String appId,
			String swsid) throws CircuitException;

}
