package cj.lns.chip.sns.server.fs.terminus;

import cj.lns.chip.sns.server.fs.cache.IPeerCache;
import cj.lns.chip.sns.server.fs.sink.IAuthFactory;
import cj.lns.chip.sns.server.fs.util.IUserKeyToolsHelper;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.ITerminusRequestService;

@CjService(name = "/loginService")
public class LoginService implements ITerminusRequestService {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	ILogging logger;
	@CjServiceRef(refByName="userKeyToolsHelper")
	IUserKeyToolsHelper keytools;
	@CjServiceRef(refByName="authFactory")
	IAuthFactory factory;
	public LoginService() {
		logger = CJSystem.current().environment().logging();
	}
	
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "login":
			login(frame, circuit);
			break;
		default:
			throw new CircuitException("404",
					String.format("不支持的方法:%s", frame.command()));
		}
	}


	private void login(Frame frame, Circuit circuit) throws CircuitException {
		String peerid=frame.parameter("peer-id");
		String uid=frame.parameter("uid");
		String cjtoken=frame.parameter("cjtoken");
		String selector=String.format("%s-%s", circuit.attribute("select-name"),circuit.attribute("select-id"));
		factory.flagSelectorPass(selector);//通过认证
		
	}
	

}
