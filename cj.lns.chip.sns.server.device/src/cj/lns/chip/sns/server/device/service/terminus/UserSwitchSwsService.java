package cj.lns.chip.sns.server.device.service.terminus;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.util.IUserKeyToolsHelper;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.Peer;
import cj.ultimate.util.StringUtil;

@CjService(name = "/servicewsService")
public class UserSwitchSwsService implements ITerminusRequestService {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	ILogging logger;
	@CjServiceRef(refByName="userKeyToolsHelper")
	IUserKeyToolsHelper keytools;

	public UserSwitchSwsService() {
		logger = CJSystem.current().environment().logging();
	}

	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "switch":
			login(frame, circuit);
			break;
		default:
			throw new CircuitException("404",
					String.format("不支持的方法:%s", frame.command()));
		}
	}

	private void login(Frame frame, Circuit circuit) throws CircuitException {
		String swsid = frame.parameter("servicews-id");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "视窗参数为空:servicews-id");
		}
		String sid = (String) circuit.attribute("select-id");
		String sname = (String) circuit.attribute("select-name");
		Peer peer = cache.getPeer(sname, sid);
		peer.setSwsid(swsid);
		cache.flush(peer);
	}

}
