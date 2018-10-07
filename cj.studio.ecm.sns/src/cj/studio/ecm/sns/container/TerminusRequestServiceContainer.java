package cj.studio.ecm.sns.container;

import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.sns.ITerminusRequestService;

public class TerminusRequestServiceContainer implements ITerminusRequestServiceContainer{

	private Map<String, ITerminusRequestService> services;

	private TerminusRequestServiceContainer() {
	}

	public static ITerminusRequestServiceContainer loadServices(
			IServiceSite site) {
		ServiceCollection<ITerminusRequestService> col = site
				.getServices(ITerminusRequestService.class);
		Map<String, ITerminusRequestService> map = new HashMap<>();
		for(ITerminusRequestService service:col){
			CjService cj=service.getClass().getAnnotation(CjService.class);
			if(cj==null){
				throw new EcmException(String.format("%s 服务缺少注解：CjService", service));
			}
			map.put(cj.name(), service);
		}
		TerminusRequestServiceContainer container = new TerminusRequestServiceContainer();
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
	public ITerminusRequestService service(String key){
		if (services.containsKey(key)) {
			return services.get(key);
		}
		if (key.endsWith("/")) {
			String str = key.substring(0, key.length() - 1);
			return services.get(str);
		} else {
			String str = String.format("%s/", key);
			return services.get(str);
		}
	}

	@Override
	public ITerminusRequestService getTerminusOnlineService() {
		return services.get(KEY_ONLINE_SERVICE);
	}

	@Override
	public ITerminusRequestService getTerminusOfflineService() {
		return services.get(KEY_OFFLINE_SERVICE);
	}

}
