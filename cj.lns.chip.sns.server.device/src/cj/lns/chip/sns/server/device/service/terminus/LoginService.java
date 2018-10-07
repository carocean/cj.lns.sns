package cj.lns.chip.sns.server.device.service.terminus;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.dao.IInboxDao;
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
import cj.studio.ecm.sns.UserKeyTools;
import cj.ultimate.util.StringUtil;

@CjService(name = "/loginService")
public class LoginService implements ITerminusRequestService {
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	ILogging logger;
	@CjServiceRef(refByName="userKeyToolsHelper")
	IUserKeyToolsHelper keytools;
	@CjServiceRef
	IInboxDao inboxDao;
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


	private Peer login(Frame frame, Circuit circuit) throws CircuitException {
		
		String userName = frame.parameter("uid");
		if (StringUtil.isEmpty(userName)) {
			throw new CircuitException("503", "用户参数为空:uid");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "视窗参数为空:swsid");
		}
		String cjtoken = frame.parameter("cjtoken");
		if (StringUtil.isEmpty(cjtoken)) {
			throw new CircuitException("503", "令牌参数为空:cjtoken");
		}
		UserKeyTools ukey = this.keytools.getUKey(userName);
		if (ukey == null) {
			throw new CircuitException("404",
					String.format("用户:%s 没有ukey", userName));
		}
		try {
			if(!ukey.verifySign(cjtoken)){
				throw new CircuitException("302", "验证失败");
			}
		} catch (Exception e) {
			throw new CircuitException("302", e);
		}
		String sid = (String) circuit.attribute("select-id");
		String sname = (String) circuit.attribute("select-name");
		Peer peer = cache.getPeer(sname, sid);
		if(!StringUtil.isEmpty(peer.getOnUser())){
			circuit.status("201");
			circuit.message("该用户已登录过");
			return peer;
		}
		peer.setSwsid(swsid);
		peer.setUserOnlineTime(System.currentTimeMillis());
		peer.setOnUser(userName);
		cache.flush(peer);
		circuit.head("peer-id",peer.getId());
		return peer;
	}
	

}
