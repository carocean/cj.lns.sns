package cj.studio.ecm.sns.sink;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.container.ICloudRequestServiceContainer;

public class InputCloudSink implements ISink {

	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		if("NET/1.1".endsWith(frame.protocol())){
			return;
		}
		ICloudRequestServiceContainer container = (ICloudRequestServiceContainer) plug
				.option("container");
		String relativePath = frame.relativePath();
		if (!container.matchService(relativePath)) {
			throw new CircuitException("404", String.format("找不到服务地址：%s", relativePath));
		}
		IPin outputCloud = (IPin) plug.option("outputCloud");
		IPin outputTerminus = (IPin) plug.option("outputTerminus");
		ICloudRequestService service=container.service(relativePath);
		service.doService(frame, circuit, outputTerminus, outputCloud);
	}

}
