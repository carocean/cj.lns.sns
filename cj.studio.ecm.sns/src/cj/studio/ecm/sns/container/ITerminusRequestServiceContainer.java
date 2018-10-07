package cj.studio.ecm.sns.container;

import cj.studio.ecm.sns.ITerminusRequestService;

public interface ITerminusRequestServiceContainer {
	String KEY_ONLINE_SERVICE="/online_service";
	String KEY_OFFLINE_SERVICE="/offline_service";
	boolean matchService(String relativePath);


	ITerminusRequestService service(String key);


	ITerminusRequestService getTerminusOnlineService();


	ITerminusRequestService getTerminusOfflineService();

}
