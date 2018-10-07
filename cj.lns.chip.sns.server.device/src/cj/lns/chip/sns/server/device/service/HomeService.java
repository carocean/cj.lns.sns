package cj.lns.chip.sns.server.device.service;

import java.util.ArrayList;
import java.util.List;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.studio.ecm.IChipInfo;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.Peer;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/")
public class HomeService implements ITerminusRequestService,
		ICloudRequestService, IServiceAfter {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	private ServiceCollection<ITerminusRequestService> services;

	@Override
	public void onAfter(IServiceSite site) {
		this.services = site.getServices(ITerminusRequestService.class);
		IChipInfo info = (IChipInfo) site
				.getService(String.format("$.%s", IChipInfo.class.getName()));
		String resource = info.getResourceProp("resource");
		if (resource.startsWith("/")) {
			resource = resource.substring(1, resource.length());
		}
		
	}

	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		String sname = (String) circuit.attribute("select-name");
		String sid = (String) circuit.attribute("select-id");
		Peer peer = cache.getPeer(sname, sid);
		circuit.head("peer-id", peer.getId());
		circuit.message(
				"欢迎使用cj互动系统，请在终端上保持peer-id以备使用其它功能。接下来请使用你的用户名和cjtoken登录，以便使用用户相关的服务");
		List<String> list = new ArrayList<>();
		for (ITerminusRequestService s : services) {
			CjService cj = s.getClass().getAnnotation(CjService.class);
			list.add(cj.name());
		}
		circuit.content().writeBytes(new Gson().toJson(list).getBytes());
	}


}
