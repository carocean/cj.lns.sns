package cj.lns.chip.sos.csc.helloworld;

import cj.studio.ecm.annotation.CjService;
/**
 * 由jss服务通过site调用该类
 * <pre>
 * 这样省去了使用java还要反射或代理的麻烦。
 * </pre>
 * @author carocean
 *
 */
@CjService(name="neuronAppManager")
public class NeuronAppManager {
	Object neuron;
	public void setNeuron(Object service) {
		this.neuron=service;
	}
	public Object getNeuron() {
		return neuron;
	}
}
