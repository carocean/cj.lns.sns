package cj.lns.chip.sns.server.device.service.terminus;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.Peer;
import cj.studio.ecm.sns.container.ITerminusRequestServiceContainer;

@CjService(name = ITerminusRequestServiceContainer.KEY_ONLINE_SERVICE)
public class OnlineService implements ITerminusRequestService {
	@CjServiceRef(refByName="peerCache")
	IPeerCache cache;
	ILogging logger;
	public OnlineService() {
		logger=CJSystem.current().environment().logging();
	}
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) {
		String sid = (String) circuit.attribute("select-id");
		String sname = (String) circuit.attribute("select-name");
		Peer peer=cache.createPeer(sid,sname,System.currentTimeMillis());
		logger.debug(getClass(),String.format("设备上线，peerId:%s", peer.getId()));
	}

}
