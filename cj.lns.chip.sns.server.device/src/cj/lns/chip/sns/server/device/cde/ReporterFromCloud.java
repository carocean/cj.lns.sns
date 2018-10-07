package cj.lns.chip.sns.server.device.cde;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.ultimate.IDisposable;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

/**
 * 长轮询方式
 * 
 * <pre>
 * 客户端会话请求堵塞，云报数和触发。
 * 会话
 * 用于发现是否有以下消息及新消息数
 * 平台新到的主题数
 * 通知新到的动态数（动态的类型：像动态／回复／评论／赞）
 * 消息新到的消息数
 * </pre>
 * 
 * @author carocean
 *
 */
@CjService(name = "/cde/cloud/reporter")
public class ReporterFromCloud implements ICloudRequestService,IServiceAfter,IDisposable {
	@CjServiceRef
	IReporterCenter reporterCenter;
	private ExecutorService pool;
	@CjServiceRef
	IDatabaseCloud databaseCloud;
	@Override
	public void dispose() {
		pool.shutdown();
	}
	@Override
	public void onAfter(IServiceSite site) {
		int workPoolSizeV=10;
		String workThreadCount = site.getProperty("cde.reporter.workThreadCount");
		if (!StringUtil.isEmpty(workThreadCount)) {
			workPoolSizeV = Integer.valueOf(workThreadCount);
		} else {
			CJSystem.current().environment().logging().warn(this.getClass()
					+ " 未指定环境变量：cde.reporter.workThreadCount，采用默认值：" + workPoolSizeV);
		}
		this.pool=Executors.newFixedThreadPool(workPoolSizeV);
	}
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		String sid = frame.parameter("cjtoken");// 发送者所在的会话
		String module = frame.parameter("module");
		String sender = frame.parameter("sender");
		String message = frame.parameter("message");// 模块主题、活动、消息中的如文章号／活动号／消息号等
		if (StringUtil.isEmpty(sid)) {
			String err = "缺少cjtoken";
			CJSystem.current().environment().logging().debug(getClass(), err);
			throw new CircuitException("500", err);
		}
		if (StringUtil.isEmpty(module)) {
			String err = "缺少module";
			CJSystem.current().environment().logging().debug(getClass(), err);
			throw new CircuitException("500", err);
		}
		if (StringUtil.isEmpty(sender)) {
			String err = "缺少sender";
			CJSystem.current().environment().logging().debug(getClass(), err);
			throw new CircuitException("500", err);
		}
		if (StringUtil.isEmpty(message)) {
			String err = "缺少message";
			CJSystem.current().environment().logging().debug(getClass(), err);
			throw new CircuitException("500", err);
		}
		Object data = null;
		if (frame.content().readableBytes() > 0) {// 附带数据
			data = new Gson().fromJson(new String(frame.content().readFully()),
					new TypeToken<HashMap<String, Object>>() {
					}.getType());
		}
		// 放入消息，而后解除对会话的阻塞以使客户端收到消息。
		// 收到指令转义为终端消息并按模块的消息推送要求进行终端的推送
		TerminusMessage msg = new TerminusMessage();
		msg.data = data;
		msg.message = message;
		msg.module = module;
		msg.sender = sender;
		
		pool.submit(new PushReporter(databaseCloud,reporterCenter,sid,msg));

	}


}
