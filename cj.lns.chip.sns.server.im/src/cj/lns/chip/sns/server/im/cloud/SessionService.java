package cj.lns.chip.sns.server.im.cloud;

import java.util.HashMap;
import java.util.Map;

import cj.lns.chip.sns.server.im.dao.ISnsAppDao;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceSetter;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.mailbox.SnsApp;
/**
 * 用法：
 * <pre>
 * 每种应用的推送器pusher均要返射注入进该服务
 * </pre>
 * @author carocean
 *
 */
@CjService(name = "/session.service")
public class SessionService
		implements ITerminusRequestService, ICloudRequestService ,IServiceSetter{
	ILogging logger;
	@CjServiceRef(refByName="snsAppDao")
	ISnsAppDao appdao;
	Map<String,IAppPusher> pushers;
	public SessionService() {
		logger = CJSystem.current().environment().logging();
		this.pushers=new HashMap<>();
	}
	@Override
	public void setService(String serviceId, Object service) {
		pushers.put(serviceId,(IAppPusher) service);
	}
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		
		String appcode = frame.parameter("app-code");
		SnsApp app=appdao.getApp(appcode);
		if(!pushers.containsKey(appcode)){
			throw new CircuitException("503",
					String.format("不支持的应用:%s", appcode));
		}
		IAppPusher pusher=pushers.get(appcode);
		pusher.push(app,frame,circuit,outputTerminus,outputCloud);
		
	}

	

}
