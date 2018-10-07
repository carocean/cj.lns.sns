package cj.lns.chip.sns.server.fs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cj.lns.chip.sns.server.fs.cache.IPeerCache;
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
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/")
public class HomeService implements ITerminusRequestService,
		ICloudRequestService, IServiceAfter {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	private ServiceCollection<ITerminusRequestService> services;
	private String jsengine;

	@Override
	public void onAfter(IServiceSite site) {
		this.services = site.getServices(ITerminusRequestService.class);
		IChipInfo info = (IChipInfo) site
				.getService(String.format("$.%s", IChipInfo.class.getName()));
		String resource = info.getResourceProp("resource");
		if (resource.startsWith("/")) {
			resource = resource.substring(1, resource.length());
		}
		jsengine = String.format("%s/socket.html", resource);
		
	}

	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		if ("HTTP/1.1".equals(frame.protocol())) {
			doHttpRequest(frame, circuit);
			return;
		}
		List<String> list = new ArrayList<>();
		for (ITerminusRequestService s : services) {
			CjService cj = s.getClass().getAnnotation(CjService.class);
			list.add(cj.name());
		}
		circuit.content().writeBytes(new Gson().toJson(list).getBytes());
	}

	private void doHttpRequest(Frame frame, Circuit circuit)
			throws CircuitException {
		int blen = 2048;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(blen * 2);
		int timeRead = 0;
		byte[] b = new byte[blen];
		
		try {
			InputStream jsengine = this.getClass().getClassLoader()
					.getResourceAsStream(this.jsengine);
			while ((timeRead = jsengine.read(b, 0, blen)) > 0) {
				buf.write(b, 0, timeRead);
			}
			circuit.piggybacking(true);
			circuit.content().writeBytes(buf.toByteArray());
		} catch (IOException e) {
			throw new CircuitException("503", e);
		} finally {
		}
		

	}

}
