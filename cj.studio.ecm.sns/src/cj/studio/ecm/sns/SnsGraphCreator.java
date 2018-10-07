package cj.studio.ecm.sns;

import cj.studio.ecm.graph.GraphCreator;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.sink.InputCloudSink;
import cj.studio.ecm.sns.sink.InputTerminusSink;
import cj.studio.ecm.sns.sink.OutputCloudSink;
import cj.studio.ecm.sns.sink.OutputTerminusSink;

public class SnsGraphCreator extends GraphCreator{

	@Override
	protected ISink createSink(String sink) {
		switch(sink){
		case "inputTerminusSink":
			return new InputTerminusSink();
		case "outputTerminusSink":
			return new OutputTerminusSink();
		case "inputCloudSink":
			return new InputCloudSink();
		case "outputCloudSink":
			return new OutputCloudSink();
		}
		return null;
	}

}
