package cj.lns.chip.sns.server.device.dao;

import java.util.List;

public interface ISessionDao {

	void deleteSession(String uid, String swsid, String sid);


	List<String> getAllAppSessions(String uid, String swsid, String appCode);

}
