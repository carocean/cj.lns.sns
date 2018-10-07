package cj.lns.chip.sns.server.im.cloud;

import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.mailbox.SnsApp;
/**
 * 必须声明为服务，且服务名必须是应用代码：appCode
 * <pre>
 *
 * </pre>
 * @author carocean
 *
 */
public interface IAppPusher {

	void push(SnsApp app, Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud)throws CircuitException;

}
