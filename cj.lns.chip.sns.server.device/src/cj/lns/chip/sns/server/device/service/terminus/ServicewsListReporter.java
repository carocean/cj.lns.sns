package cj.lns.chip.sns.server.device.service.terminus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.device.cache.IPeerCache;
import cj.lns.chip.sns.server.device.dao.IInboxDao;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.net.nio.NetConstans;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.Peer;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

@CjService(name = "/servicewsListReporter")
public class ServicewsListReporter implements ITerminusRequestService {
	ILogging logger;
	@CjServiceRef
	IInboxDao inboxDao;
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	public ServicewsListReporter() {
		logger = CJSystem.current().environment().logging();
	}
	
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		String sid = (String) circuit.attribute("select-id");
		String sname = (String) circuit.attribute("select-name");
		Peer peer = cache.getPeer(sname, sid);
		if(StringUtil.isEmpty(peer.getOnUser())){
			circuit.status("503");
			circuit.message("用户未上线");
			logger.info(getClass(),circuit.message());
			return ;
		}
		Map<String,Long> reporter=new HashMap<>();
		
		List<String> swsids=getUserAllServicews(peer.getOnUser(),outputCloud);
		if(swsids.isEmpty()){
			pushToPeer(peer,reporter,outputTerminus);
			return;
		}
		
		for(String swsid:swsids){
			if(StringUtil.isEmpty(swsid)){
				continue;
			}
			long count=inboxDao.totalMessage(swsid, peer.getOnUser());
			reporter.put(swsid, count);
		}
		pushToPeer(peer,reporter,outputTerminus);
	}

	private List<String> getUserAllServicews(String onUser, IPin outputCloud) throws CircuitException {
		Frame f = new Frame("getAllSwsid /serviceOS/sws/owner sos/1.0");
		f.parameter("userCode", onUser);
		f.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC,"true");
		f.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC_TIMEOUT,"20000");
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		byte[] b=c.content().readFully();
		Frame back=new Frame(b);
		if(!"200".equals(back.head("status"))){
			throw new CircuitException(back.head("status"), String.format("在远程服务器上出错，原因：%s", back.head("message"))	);
		}
		b=back.content().readFully();
		return new Gson().fromJson(new String(b), new TypeToken<List<String>>(){}.getType());
	}

	private void pushToPeer(Peer peer, 
			 Map<String, Long> reporter, IPin outputTerminus) throws CircuitException {
		// 留在客户端判断是否是当前视窗
		// if(toSwsid.equals(peer.getSwsid())){//是当前视窗
		//
		// }else{
		//
		// }
		// 是在本服务器上的，则向客户端发送消息
		
		Frame f = new Frame("push /cyberport/ peer/1.0");
		f.parameter("sender", peer.getOnUser());
		f.parameter("swsid", peer.getSwsid());
		f.parameter("type", "reportMyServicewsMsgCount");
		f.content().writeBytes(new Gson().toJson(reporter).getBytes());
		Circuit c = new Circuit("peer/1.0 200 ok");
		c.attribute("select-id", peer.getSelectId());
		outputTerminus.flow(f, c);
	}
	
	

}
