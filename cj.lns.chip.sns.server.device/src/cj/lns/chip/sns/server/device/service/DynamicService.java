package cj.lns.chip.sns.server.device.service;

import cj.lns.chip.sns.server.device.dao.IDynamicDao;
import cj.lns.chip.sns.server.device.dao.ISessionDao;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

@CjService(name = "/session/dynamicSession")
public class DynamicService
		implements ITerminusRequestService, ICloudRequestService {
	@CjServiceRef
	ISessionDao sessionDao;
	@CjServiceRef
	IDynamicDao dynamicDao;

	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "open":
			doOpen(frame, circuit);
			break;
		default:
			throw new CircuitException("503",
					String.format("不支持的指令：%s", frame.command()));
		}
	}


	public void doOpen(Frame frame, Circuit circuit) throws CircuitException {
		String owner = frame.parameter("owner");
		if (StringUtil.isEmpty(owner)) {
			throw new CircuitException("503", "侦的参数：owner为空");
		}
		String appCode = frame.parameter("app-code");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：app-code为空");
		}
		String appId = frame.parameter("app-id");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：app-id为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		
		MySessionStub stub = this.dynamicDao.openSession(owner, appCode,appId,
				swsid);
		Gson g = new Gson();
		String json = g.toJson(stub);
		circuit.content().writeBytes(json.getBytes());
	}

}
