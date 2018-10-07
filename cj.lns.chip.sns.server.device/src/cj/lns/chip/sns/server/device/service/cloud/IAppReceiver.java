package cj.lns.chip.sns.server.device.service.cloud;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
/**
 * 服务名必须对应应用代码:appCode,而后反射注入到服务
 * <pre>
 *
 * </pre>
 * @author carocean
 *
 */
public interface IAppReceiver {

	void receive(MessageStub msg, Frame frame, Circuit circuit,
			IPin outputTerminus, IPin outputCloud)throws CircuitException;

}
