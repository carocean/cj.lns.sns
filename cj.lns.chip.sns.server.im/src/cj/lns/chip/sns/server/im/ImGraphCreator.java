package cj.lns.chip.sns.server.im;

import cj.lns.chip.sns.server.im.sink.DenyInputTerminusSink;
import cj.studio.ecm.graph.AnnotationProtocolFactory;
import cj.studio.ecm.graph.IProtocolFactory;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.SnsGraphCreator;

public class ImGraphCreator extends SnsGraphCreator {
	@Override
	protected IProtocolFactory newProtocol() {
		return AnnotationProtocolFactory.factory(ImConstans.class);
	}

	@Override
	protected ISink createSink(String sink) {
		switch (sink) {
		case "inputTerminusSink":
			return new DenyInputTerminusSink();
		}
		return super.createSink(sink);
	}

}
