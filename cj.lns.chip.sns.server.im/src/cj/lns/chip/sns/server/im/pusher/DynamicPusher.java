package cj.lns.chip.sns.server.im.pusher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cj.lns.chip.sns.server.im.IDatabaseCloud;
import cj.lns.chip.sns.server.im.cache.IPeerCache;
import cj.lns.chip.sns.server.im.cloud.IAppPusher;
import cj.lns.chip.sns.server.im.cloud.SessionService;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.frame.IFlowContent;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.sns.Peer;
import cj.studio.ecm.sns.mailbox.SnsApp;
import cj.studio.ecm.sns.mailbox.viewer.MessageStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;
@CjService(name="dynamic")
public class DynamicPusher implements IAppPusher{
	@CjServiceRef(refByName = "peerCache")
	IPeerCache cache;
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceInvertInjection
	@CjServiceRef(refByName="/session.service")
	SessionService sessionService;
	ILogging logger;
	public DynamicPusher() {
		logger = CJSystem.current().environment().logging();
	}
	@Override
	public void push(SnsApp app, Frame frame, Circuit circuit,
			IPin outputTerminus, IPin outputCloud) throws CircuitException {
		String appid = frame.parameter("app-id");
		IFlowContent cnt = frame.content();
		MessageStub msg=new Gson().fromJson(new String(cnt.readFully()), MessageStub.class);
		List<String> users = getUserNames(msg.getSenderOnSws(),outputCloud);
		//同时向基础视窗推送动态，因此求出基础视窗的持有者，并添加到users中
		String basicSwsOwner=getBasicSwsOwner(msg.getSenderOnSws(),outputCloud);
		if(!StringUtil.isEmpty(basicSwsOwner)&&!users.contains(basicSwsOwner)){
			users.add(basicSwsOwner);
		}
		List<Peer> onlines = new ArrayList<>();
		List<String> offlines = new ArrayList<>();
		cache.checkUsers(users, onlines, offlines);
		// 先推送在线的，后推送不在线的
		
		String swstid=getSwstid(msg.getSenderOnSws(),outputCloud);
		msg.setSwstid(swstid);
		byte[] b=new Gson().toJson(msg).getBytes();
		String cline = "push /device/processMessage.service peer/1.0";
		
		String selectUser = frame.parameter("select-user");
		if (!StringUtil.isEmpty(selectUser)) {// 按选定的目标通讯
			List<Peer> peers = cache.getPeers(selectUser);
			if (peers.isEmpty()) {
				Frame f = new Frame(cline);
				f.content().writeBytes(b);
				f.head("dest-user", selectUser);
				f.parameter("app-id", appid);
				f.parameter("app-code", app.getCode());
				f.parameter("app-name", app.getName());
				f.parameter("app-icon", app.getIcon());
				Circuit c = new Circuit("peer/1.0 200 ok");
				try {
					outputCloud.flow(f, c);
				} catch (Exception e) {
					logger.error(getClass(), e.getMessage());
				}
			} else {
				for (Peer p : peers) {
					Frame f = new Frame(cline);
					f.head("dest-address", p.getOnAddress());
					f.head("dest-user", p.getOnUser());
					f.head("dest-peerid", p.getId());
					f.parameter("app-id", appid);
					f.parameter("app-code", app.getCode());
					f.parameter("app-name", app.getName());
					f.parameter("app-icon", app.getIcon());
					f.content().writeBytes(b);
					Circuit c = new Circuit("peer/1.0 200 ok");
					try {
						outputCloud.flow(f, c);
					} catch (Exception e) {
						logger.error(getClass(), e.getMessage());
					}
				}
			}
			return;
		}
		
		for (Peer p : onlines) {
			if(msg.getSender().equals(p.getOnUser())){
				continue;
			}
			Frame f = new Frame(cline);
			f.head("dest-address", p.getOnAddress());
			f.head("dest-user", p.getOnUser());
			f.head("dest-peerid",p.getId());
			f.parameter("app-id",appid);
			f.parameter("app-code",app.getCode());
			f.parameter("app-name",app.getName());
			f.parameter("app-icon",app.getIcon());
			f.content().writeBytes(b);
			Circuit c = new Circuit("peer/1.0 200 ok");
			try {
				outputCloud.flow(f, c);
			} catch (Exception e) {
				logger.error(getClass(), e.getMessage());
			}
		}
		for (String user : offlines) {
			if(user.equals(msg.getSender())){
				continue;
			}
			Frame f = new Frame(cline);
			f.content().writeBytes(b);
			f.head("dest-user", user);
			f.parameter("app-id",appid);
			f.parameter("app-code",app.getCode());
			f.parameter("app-name",app.getName());
			f.parameter("app-icon",app.getIcon());
			Circuit c = new Circuit("peer/1.0 200 ok");
			try {
				outputCloud.flow(f, c);
			} catch (Exception e) {
				logger.error(getClass(), e.getMessage());
			}
		}
		
	}
	private String getBasicSwsOwner(String swsid,IPin outputCloud) throws CircuitException {
		Frame f = new Frame("getbasicSws /serviceOS/sws/instance sos/1.0");
		f.parameter("swsid", swsid);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		HashMap<String,Object> map=new Gson().fromJson(new String(back.content().readFully()), new TypeToken<HashMap<String,Object>>(){}.getType());
		return (String)map.get("owner");
	}
	private List<String> getUserNames(String swsid, IPin outputCloud) throws CircuitException {
		Frame f = new Frame("getServicewsUserNames /serviceOS/sws/userGroup/ sos/1.0");
		f.parameter("swsid", swsid);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		List<String> list=new Gson().fromJson(new String(back.content().readFully()), new TypeToken<ArrayList<String>>(){}.getType());
		return list;
	}
	private String getSwstid(String swsid, IPin outputCloud) throws CircuitException {
		Frame f = new Frame("getServicewstid /serviceOS/sws/instance sos/1.0");
		f.parameter("swsid", swsid);
		Circuit c = new Circuit("sos/1.0 200 ok");
		outputCloud.flow(f, c);
		Frame back = new Frame(c.content().readFully());
		if (!"200".equals(back.head("status"))) {
			throw new CircuitException(back.head("status"),
					String.format("在远程服务器上出现错误。原因：%s", back.head("message")));
		}
		return back.head("swstid");
	}
}
