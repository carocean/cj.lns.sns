package cj.lns.chip.sns.server.im.sink;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;

public class DenyInputTerminusSink implements ISink {

	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		throw new CircuitException("301", "即时推送服务器拒绝终端调用");
	}

}
