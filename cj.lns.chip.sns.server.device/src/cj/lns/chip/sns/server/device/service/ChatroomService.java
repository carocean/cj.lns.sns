package cj.lns.chip.sns.server.device.service;

import java.util.HashMap;
import java.util.List;

import cj.lns.chip.sns.server.device.dao.IChatroomDao;
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

@CjService(name = "/session/chatroom")
public class ChatroomService
		implements ITerminusRequestService, ICloudRequestService {
	@CjServiceRef
	ISessionDao sessionDao;
	@CjServiceRef
	IChatroomDao chatroomDao;

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
		String crid = frame.parameter("crid");
		if (StringUtil.isEmpty(crid)) {
			throw new CircuitException("503", "侦的参数：crid为空");
		}
		List<HashMap<String, String>> users = this.chatroomDao.getUsers(crid);
		String json = new Gson().toJson(users);
		circuit.content().writeBytes(json.getBytes());
	}

	public void doOpen(Frame frame, Circuit circuit) throws CircuitException {
		String uid = frame.parameter("uid");
		if (StringUtil.isEmpty(uid)) {
			throw new CircuitException("503", "侦的参数：user为空");
		}
		String owner = frame.parameter("owner");
		if (StringUtil.isEmpty(owner)) {
			throw new CircuitException("503", "侦的参数：owner为空");
		}
		String appCode = frame.parameter("app-code");
		if (StringUtil.isEmpty(appCode)) {
			throw new CircuitException("503", "侦的参数：app-code为空");
		}
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String icon = frame.parameter("icon");
		if (StringUtil.isEmpty(icon)) {
			throw new CircuitException("503", "侦的参数：icon为空");
		}
		String title = frame.parameter("title");
		if (StringUtil.isEmpty(title)) {
			throw new CircuitException("503", "侦的参数：title为空");
		}
		
		icon =String.format("./resource/ud/%s?path=home://system/img/faces&u=%s",icon,uid);
		MySessionStub stub = this.chatroomDao.openSession(uid, owner, appCode,
				swsid, icon, title);
		Gson g = new Gson();
		String json = g.toJson(stub);
		circuit.content().writeBytes(json.getBytes());
	}

}
