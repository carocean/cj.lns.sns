package cj.lns.chip.sns.server.device;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.sink.SecuritySink;
import cj.studio.ecm.graph.AnnotationProtocolFactory;
import cj.studio.ecm.graph.IProtocolFactory;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.SnsGraphCreator;

public class DeviceGraphCreator extends SnsGraphCreator{
	@Override
	protected IProtocolFactory newProtocol() {
		return AnnotationProtocolFactory.factory(DeviceIConstans.class);
	}
	@Override
	protected ISink createSink(String sink) {
		switch(sink){
		case "authSink":
			IPeerCache cache=(IPeerCache)site().getService("peerCache");
			return new SecuritySink(cache);
		}
		return super.createSink(sink);
	}

}
