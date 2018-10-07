package cj.lns.chip.sns.server.device.service.cloud;

import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.IServiceSetter;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/processMessage.service")
public class ProcessMessageService implements ICloudRequestService,IServiceSetter {
	
	Map<String,IAppReceiver> receivers;
	public ProcessMessageService() {
		receivers=new HashMap<>();
	}
	@Override
	public void setService(String serviceId, Object service) {
		receivers.put(serviceId, (IAppReceiver)service);
	}
	
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		String appCode = frame.parameter("app-code");
		MessageStub msg = new Gson().fromJson(
				new String(frame.content().readFully()), MessageStub.class);
		if(!receivers.containsKey(appCode)){
			throw new CircuitException("503", String.format("不支持应用：%s", appCode));
		}
		IAppReceiver receiver=receivers.get(appCode);
		receiver.receive(msg,frame,circuit,outputTerminus,outputCloud);
	}

	

}
