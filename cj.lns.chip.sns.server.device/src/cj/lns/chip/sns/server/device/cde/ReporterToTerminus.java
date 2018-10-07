package cj.lns.chip.sns.server.device.cde;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
/**
 * 长轮询方式
 * <pre>
 * 客户端会话请求堵塞，云报数和触发。
 * 会话
 * 用于发现是否有以下消息及新消息数
 * 平台新到的主题数
 * 通知新到的动态数（动态的类型：像动态／回复／评论／赞）
 * 消息新到的消息数
 * </pre>
 * @author carocean
 *
 */
@CjService(name="/cde/terminus/reporter")
public class ReporterToTerminus implements ITerminusRequestService{
	@CjServiceRef
	IReporterCenter reporterCenter;
	public ReporterToTerminus() {
	}
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		//放入消息，而后解除对会话的阻塞以使客户端收到消息。
		String sid=frame.parameter("cjtoken");
		if(StringUtil.isEmpty(sid)){
			String err="缺少cjtoken";
			CJSystem.current().environment().logging().debug(getClass(),err);
			throw new CircuitException("500", err);
		}
		TerminusReporter reporter=reporterCenter.take(sid,120000);
		//写回客户端
		if(reporter==null||reporter.count()<1){
			if(frame.containsParameter("callbackparam")){
				String onmessage=String.format("%s(%s)",frame.parameter("callbackparam"), "{}");
				circuit.content().writeBytes(onmessage.getBytes());
			}else{
				circuit.content().writeBytes("{}".getBytes());
			}
			return;
		}
		String json=new Gson().toJson(reporter);
		if(frame.containsParameter("callbackparam")){
			String onmessage=String.format("%s(%s)",frame.parameter("callbackparam"), json);
			circuit.content().writeBytes(onmessage.getBytes());
		}else{
			circuit.content().writeBytes(json.getBytes());
		}
	}
	
	
}
