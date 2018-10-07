package cj.lns.chip.sns.server.fs.sink;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.container.ITerminusRequestServiceContainer;

public class InputTerminusSink implements ISink {
	IAuthFactory factory;
	public InputTerminusSink(IAuthFactory factory) {
		this.factory=factory;
	}
	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		ITerminusRequestServiceContainer container = (ITerminusRequestServiceContainer) plug
				.option("container");
		IPin outputCloud = (IPin) plug.option("outputCloud");
		IPin outputTerminus = (IPin) plug.option("outputTerminus");
		if ("NET/1.1".endsWith(frame.protocol())) {
			String name=(String)circuit.attribute("select-name");
			String id=(String)circuit.attribute("select-id");
			if ("connect".equals(frame.command())) {
				factory.on(String.format("%s-%s", name,id));
				return;
			}
			if ("disconnect".equals(frame.command())) {
				factory.off(String.format("%s-%s", name,id));
				return;
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
