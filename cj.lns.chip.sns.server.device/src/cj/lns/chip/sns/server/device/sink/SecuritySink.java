package cj.lns.chip.sns.server.device.sink;

import cj.lns.chip.sns.server.device.DeviceIConstans;
import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.AnnotationProtocolFactory;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPlug;
import cj.studio.ecm.graph.ISink;
import cj.studio.ecm.sns.Peer;
import cj.ultimate.util.StringUtil;

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
		if ("/".equals(relurl) ||relurl.startsWith("/cde/")|| "/loginService".equals(relurl)
				|| "/loginService/".equals(relurl) || relurl.endsWith(".js")) {
			plug.flow(frame, circuit);
			return;
		}
		String sid = (String) circuit.attribute("select-id");
		String sname = (String) circuit.attribute("select-name");
		Peer peer = cache.getPeer(sname, sid);
		if (StringUtil.isEmpty(peer.getOnUser())) {
			throw new CircuitException(DeviceIConstans.STATUS_301,
					String.format(AnnotationProtocolFactory
							.factory(DeviceIConstans.class)
							.get(DeviceIConstans.STATUS_301), frame));
		}
		plug.flow(frame, circuit);
	}

}
