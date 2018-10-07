package cj.lns.chip.sos.csc.helloworld;

import cj.lns.chip.sos.csc.CscGraph;
import cj.studio.ecm.IServiceSetter;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
@CjService(name="cj.neuron.app",isExoteric=true)
public class HelloworldCscGraph extends CscGraph implements IServiceSetter{
	@CjServiceRef
	NeuronAppManager neuronAppManager;
	@Override
	public void setService(String serviceId, Object service) {
		// 
		if("neuron".equals(serviceId)){//神经元
			neuronAppManager.setNeuron(service);
		}
	}

}
