package cj.studio.ecm.sns.container;

import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.sns.ICloudRequestService;

public class CloudRequestServiceContainer
		implements ICloudRequestServiceContainer {
	private Map<String, ICloudRequestService> services;
	private CloudRequestServiceContainer() {
	}

	public static ICloudRequestServiceContainer loadServices(
			IServiceSite site) {
		ServiceCollection<ICloudRequestService> col = site
				.getServices(ICloudRequestService.class);
		Map<String, ICloudRequestService> map = new HashMap<>();
		for(ICloudRequestService service:col){
			CjService cj=service.getClass().getAnnotation(CjService.class);
			if(cj==null){
				throw new EcmException(String.format("%s 服务缺少注解：CjService", service));
			}
			map.put(cj.name(), service);
		}
		CloudRequestServiceContainer container = new CloudRequestServiceContainer();
		container.services = map;
		return container;
	}

	@Override
	public boolean matchService(String relativePath) {
		if (services.containsKey(relativePath)) {
			return true;
		}
		if (relativePath.endsWith("/")) {
			String str = relativePath.substring(0, relativePath.length() - 1);
			return services.containsKey(str);
		} else {
			String str = String.format("%s/", relativePath);
			return services.containsKey(str);
		}
	}
	@Override
	public ICloudRequestService service(String key){
		return services.get(key);
	}

}
