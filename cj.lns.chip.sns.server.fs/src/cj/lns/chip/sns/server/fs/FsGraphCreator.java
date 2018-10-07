package cj.lns.chip.sns.server.fs;

import cj.lns.chip.sns.server.fs.cache.IPeerCache;
import cj.lns.chip.sns.server.fs.sink.IAuthFactory;
import cj.lns.chip.sns.server.fs.sink.InputTerminusSink;
import cj.lns.chip.sns.server.fs.sink.SecuritySink;
import cj.studio.ecm.graph.AnnotationProtocolFactory;
import cj.studio.ecm.graph.IProtocolFactory;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.SnsGraphCreator;

public class FsGraphCreator extends SnsGraphCreator{
	@Override
	protected IProtocolFactory newProtocol() {
		return AnnotationProtocolFactory.factory(FsConstans.class);
	}
	@Override
	protected ISink createSink(String sink) {
		switch(sink){
		case "authSink":
			IPeerCache cache=(IPeerCache)site().getService("peerCache");
			return new SecuritySink(cache);
		case "inputTerminusSink":
			IAuthFactory factory=(IAuthFactory)site().getService("authFactory");
			return new InputTerminusSink(factory);
		}
		return super.createSink(sink);
	}

}
