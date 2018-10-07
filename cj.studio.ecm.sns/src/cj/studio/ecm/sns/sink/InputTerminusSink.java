package cj.studio.ecm.sns.sink;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.container.ITerminusRequestServiceContainer;

public class InputTerminusSink implements ISink {

	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		ITerminusRequestServiceContainer container = (ITerminusRequestServiceContainer) plug
				.option("container");
		IPin outputCloud = (IPin) plug.option("outputCloud");
		IPin outputTerminus = (IPin) plug.option("outputTerminus");
		if ("NET/1.1".endsWith(frame.protocol())) {
			if ("connect".equals(frame.command())) {
				ITerminusRequestService service = container
						.getTerminusOnlineService();
				if (service == null) {
					throw new CircuitException("404", "找不到上线服务");
				}
				service.doService(frame, circuit, outputTerminus, outputCloud);
			}
			if ("disconnect".equals(frame.command())) {
				ITerminusRequestService service = container
						.getTerminusOfflineService();
				if (service == null) {
					throw new CircuitException("404", "找不到下线服务");
				}
				service.doService(frame, circuit, outputTerminus, outputCloud);
			}
			return;
		}

		String relativePath = frame.relativePath();
		if (!container.matchService(relativePath)) {
			throw new CircuitException("404",
					String.format("找不到服务地址：%s", relativePath));
		}

		ITerminusRequestService service = container.service(relativePath);
		service.doService(frame, circuit, outputTerminus, outputCloud);
	}

}
