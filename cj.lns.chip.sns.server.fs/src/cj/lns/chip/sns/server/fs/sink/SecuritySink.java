package cj.lns.chip.sns.server.fs.sink;

import cj.lns.chip.sns.server.fs.cache.IPeerCache;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;

/**
 * 终点站请求进行安全检查，如果请求不对则断开
 * 
 * <pre>
 *
 * </pre>
 * 
 * @author carocean
 *
 */
public class SecuritySink implements ISink {
	IPeerCache cache;
	
	public SecuritySink(IPeerCache cache) {
		this.cache = cache;
	}

	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug)
			throws CircuitException {
		String relurl = frame.relativePath();
		// 如果不是主目录和登录这两个开放的服务，将限制访问，但并不关闭peer，因为还可以使用这两个开放服务。
		if ("/".equals(relurl) || "/loginService".equals(relurl)
				|| "/loginService/".equals(relurl) || relurl.endsWith(".js")) {
			plug.flow(frame, circuit);
			return;
		}
		
		plug.flow(frame, circuit);
	}

}
