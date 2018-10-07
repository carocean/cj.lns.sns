package cj.lns.chip.sns.server.device.service;

import java.util.HashMap;
import java.util.List;

import cj.lns.chip.sns.server.device.dao.IChatGroupDao;
import cj.lns.chip.sns.server.device.dao.ISessionDao;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

@CjService(name = "/session/chatGroup")
public class ChatGroupService
		implements ITerminusRequestService, ICloudRequestService {
	@CjServiceRef
	ISessionDao sessionDao;
	@CjServiceRef
	IChatGroupDao chatGroupDao;

	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "open":
			doOpen(frame, circuit);
			break;
		case "getUsers":
			getUsers(frame, circuit);
			break;
		case "delete":
			deleteSession(frame, circuit);
			break;
		default:
			throw new CircuitException("503",
					String.format("不支持的指令：%s", frame.command()));
		}
	}

	private void deleteSession(Frame frame, Circuit circuit)
			throws CircuitException {
		String uid = frame.parameter("uid");
		if (StringUtil.isEmpty(uid)) {
			throw new CircuitException("503", "侦的参数：uid为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String sid = frame.parameter("sid");
		if (StringUtil.isEmpty(sid)) {
			throw new CircuitException("503", "侦的参数：sid为空");
		}

		this.sessionDao.deleteSession(uid, swsid, sid);

	}

	private void getUsers(Frame frame, Circuit circuit)
			throws CircuitException {
		// 查询会话室的联系人表
		String gid = frame.parameter("gid");
		if (StringUtil.isEmpty(gid)) {
			throw new CircuitException("503", "侦的参数：gid为空");
		}
		List<HashMap<String, String>> users = this.chatGroupDao.getUsers(gid);
		String json = new Gson().toJson(users);
		circuit.content().writeBytes(json.getBytes());
	}

	public void doOpen(Frame frame, Circuit circuit) throws CircuitException {
		String owner = frame.parameter("owner");
		if (StringUtil.isEmpty(owner)) {
			throw new CircuitException("503", "侦的参数：owner为空");
		}
		String appCode = frame.parameter("app-code");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：app-code为空");
		}
		String appId = frame.parameter("app-id");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：app-id为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		
		MySessionStub stub = this.chatGroupDao.openSession(owner, appCode,appId,
				swsid);
		Gson g = new Gson();
		String json = g.toJson(stub);
		circuit.content().writeBytes(json.getBytes());
	}

}
