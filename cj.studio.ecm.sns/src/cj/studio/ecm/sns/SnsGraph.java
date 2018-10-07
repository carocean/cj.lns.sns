package cj.studio.ecm.sns;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.graph.Access;
import cj.studio.ecm.graph.Graph;
import cj.studio.ecm.graph.GraphCreator;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.sns.container.CloudRequestServiceContainer;
import cj.studio.ecm.sns.container.ICloudRequestServiceContainer;
import cj.studio.ecm.sns.container.ITerminusRequestServiceContainer;
import cj.studio.ecm.sns.container.TerminusRequestServiceContainer;

@CjService(name = "cj.neuron.app", isExoteric = true)
public class SnsGraph extends Graph {
	@Override
	protected String defineAcceptProptocol() {
		return null;
	}

	@Override
	protected GraphCreator newCreator() {
		return new SnsGraphCreator();
	}
	public IPin inputTerminus(){
		return in("inputTerminus");
	}
	public IPin inputCloud(){
		return in("inputCloud");
	}
	public IPin outputTerminus(){
		return in("outputTerminus");
	}
	public IPin outputCloud(){
		return in("outputCloud");
	}
	@Override
	protected void build(GraphCreator c) {
		/*
		 * 经分析，互动系统有两类服务：一种是处理来自云端请求叫云服务，一种是来自终端的请求叫终端服务，这些服务均可能使用两个输出端子到对端服务器
		 */

		IPin inputTerminus = c.newWirePin("inputTerminus", Access.input);
		IPlug dispatcherTerminusReq = inputTerminus.plugLast("inputTerminusSink",
				c.newSink("inputTerminusSink"));
		IPin inputCloud = c.newWirePin("inputCloud", Access.input);
		IPlug dispatcherCloudReq = inputCloud.plugLast("inputCloudSink",
				c.newSink("inputCloudSink"));

		IPin outputTerminus = c.newWirePin("outputTerminus", Access.output);
		outputTerminus.plugLast("outputTerminusSink",
				c.newSink("outputTerminusSink"));
		
		IPin outputCloud = c.newWirePin("outputCloud", Access.output);
		outputCloud.plugLast("outputCloudSink", c.newSink("outputCloudSink"));
		
		
		dispatcherTerminusReq.option("outputTerminus",outputTerminus);
		dispatcherTerminusReq.option("outputCloud",outputCloud);
		
		ITerminusRequestServiceContainer trsc=TerminusRequestServiceContainer.loadServices(c.site());
		dispatcherTerminusReq.option("container",trsc);
		
		dispatcherCloudReq.option("outputTerminus",outputTerminus);
		dispatcherCloudReq.option("outputCloud",outputCloud);
		
		ICloudRequestServiceContainer crsc=CloudRequestServiceContainer.loadServices(c.site());
		dispatcherCloudReq.option("container",crsc);
		
	}

}
