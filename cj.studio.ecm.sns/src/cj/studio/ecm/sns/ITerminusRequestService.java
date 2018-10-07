package cj.studio.ecm.sns;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;

public interface ITerminusRequestService {

	void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException;

}
