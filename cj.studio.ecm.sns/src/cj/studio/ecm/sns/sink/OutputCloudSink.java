package cj.studio.ecm.sns.sink;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.net.nio.NetConstans;

public class OutputCloudSink implements ISink {

	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		if("SOS/1.0".equals(frame.protocol())){
			if(!frame.containsParameter("cjtoken")){
				frame.parameter("cjtoken","xxxx");
			}
			if(!frame.containsHead(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC)){
				frame.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC,"true");
				frame.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC_TIMEOUT,"10000");
			}
		}
		plug.flow(frame, circuit);
	}

}
