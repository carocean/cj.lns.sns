package cj.lns.chip.sns.server.device.service;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sns.server.device.dao.ISnsAppDao;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;
import cj.studio.ecm.sns.mailbox.MySession;
import cj.studio.ecm.sns.mailbox.SnsApp;
import cj.studio.ecm.sns.mailbox.viewer.MySessionStub;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

@CjService(name = "/session.service")
public class SessionService
		implements ITerminusRequestService, ICloudRequestService {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@CjServiceRef(refByName="snsAppDao")
	ISnsAppDao appdao;
	@Override
	public void doService(Frame frame, Circuit circuit, IPin outputTerminus,
			IPin outputCloud) throws CircuitException {
		switch (frame.command()) {
		case "open":
			doOpen(frame, circuit);
			break;
		default:
			throw new CircuitException("503",
					String.format("不支持的指令：%s", frame.command()));
		}
	}

	private void doOpen(Frame frame, Circuit circuit) throws CircuitException {
		String swsid = frame.parameter("swsid");
		if (StringUtil.isEmpty(swsid)) {
			throw new CircuitException("503", "侦的参数：swsid为空");
		}
		String uid = frame.parameter("uid");
		if (StringUtil.isEmpty(uid)) {
			throw new CircuitException("503", "侦的参数：uid为空");
		}
		String sid = frame.parameter("sid");
		if (StringUtil.isEmpty(sid)) {
			throw new CircuitException("503", "侦的参数：sid为空");
		}

		ICube cube = db.getUserDisk(uid).cube(swsid);
		String cjql = "select {'tuple':'*'} from tuple ?(colName) ?(clazz) where {'_id':ObjectId('?(sid)')}";
		IQuery<MySession> lq = cube.createQuery(cjql);
		lq.setParameter("clazz", MySession.class.getName());
		lq.setParameter("colName", MySession.KEY_COLL_NAME);
		lq.setParameter("sid", sid);
		IDocument<MySession> sessiondoc = lq.getSingleResult();
		MySession session=sessiondoc.tuple();
		session.setSid(sessiondoc.docid());
		MySessionStub s=new MySessionStub();
		s.setAppId(session.getAppId());
		s.setCreateTime(session.getCreateTime());
		s.setIcon(session.getIcon());
		s.setNewestMsg(null);
		s.setProfile(session.getProfile());
		s.setSid(session.getSid());
		SnsApp app=appdao.getApp(session.getAppCode());
		s.setSnsApp(app);
		s.setTitle(session.getTitle());
		circuit.content().writeBytes(new Gson().toJson(s).getBytes());
	}

	

}
