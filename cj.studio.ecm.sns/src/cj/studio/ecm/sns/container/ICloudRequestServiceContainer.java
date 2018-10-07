package cj.studio.ecm.sns.container;

import cj.studio.ecm.sns.ICloudRequestService;

public interface ICloudRequestServiceContainer {

	boolean matchService(String relativePath);


	ICloudRequestService service(String key);

}
