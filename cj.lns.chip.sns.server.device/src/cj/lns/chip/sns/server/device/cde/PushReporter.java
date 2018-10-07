package cj.lns.chip.sns.server.device.cde;

import java.util.HashMap;
import java.util.List;

import cj.lns.chip.sns.server.device.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.studio.ecm.graph.CircuitException;
import cj.ultimate.util.StringUtil;

public class PushReporter implements Runnable {
	private String sid;
	private TerminusMessage msg;
	private IReporterCenter reporterCenter;
	private ICube home;

	public PushReporter(IDatabaseCloud databaseCloud,
			IReporterCenter reporterCenter, String sid, TerminusMessage msg) {
		this.sid = sid;
		this.msg = msg;
		this.reporterCenter = reporterCenter;
		home = databaseCloud.getLnsDataHome();
	}

	@Override
	public void run() {
		try {
			pushMessage(sid, msg);
		} catch (CircuitException e) {
			throw new RuntimeException(e);
		} finally {
			this.sid = null;
			this.msg = null;
			this.reporterCenter = null;
			this.home = null;
		}
	}

	private void pushMessage(String sid, TerminusMessage msg)
			throws CircuitException {
		switch (msg.module) {
		case "platform":// 平台推向所有正在等待的会话目标
			reporterCenter.notifyExistsAll(msg);
			break;
		case "activity":// 活动推向所有粉丝,求得粉丝，并根据登录表中的用户名与会话对照求得会话标识，然后推向标识
//			List<IDocument<HashMap<String, Object>>> followers = follower(
//					msg.sender);
//			// 从登录日志中获取用户对应的会话标识
//			for (IDocument<HashMap<String, Object>> follow : followers) {
//				String tosid = getLogger(
//						(String) follow.tuple().get("follower"));// 将来得改为采用in一次性取出，将性能计算放到推送芯片上，不能都压给mongodb
//				if (StringUtil.isEmpty(tosid))
//					continue;
//				
//				reporterCenter.notifya(tosid, msg);
//			}
			HashMap<String, Object> map=(HashMap<String, Object>)msg.data;
			String tosid = getLogger(
					(String) map.get("reciever"));// 将来得改为采用in一次性取出，将性能计算放到推送芯片上，不能都压给mongodb
			if (!StringUtil.isEmpty(tosid)){
				reporterCenter.notifya(tosid, msg);
			}
			break;
		case "imessager":// 消息推向指定thread内的所有用户，除了发送者本人,根据登录表中的用户名与会话对照求得会话标识，然后推向标识
			String creator = getThreadCreator(msg.message);
			if (!msg.sender.equals(creator)) {
				inbox(creator, msg);
				 tosid = getLogger(creator);// 将来得改为采用in一次性取出，将性能计算放到推送芯片上，不能都压给mongodb
				if (!StringUtil.isEmpty(tosid)) {
					reporterCenter.notifya(tosid, msg);
				}
			}
			List<IDocument<HashMap<String, Object>>> users = getThreadUsers(
					msg.message);// message为threadid
			// 从登录日志中获取用户对应的会话标识
			for (IDocument<HashMap<String, Object>> user : users) {
				String name = (String) user.tuple().get("recipient");
				if (msg.sender.equals(name)) {
					continue;
				}
				inbox(name, msg);
				 tosid = getLogger(name);// 将来得改为采用in一次性取出，将性能计算放到推送芯片上，不能都压给mongodb
				if (StringUtil.isEmpty(tosid))
					continue;
				
				reporterCenter.notifya(tosid, msg);
			}
			break;
		default:
			throw new CircuitException("500", "消息中心不支持的模块：" + msg.module);
		}

	}

	//收件箱，直到读取thread中的记录后移除
	private void inbox(String recipients, TerminusMessage msg) {
		HashMap<String,Object> map=new HashMap<>();
		map.put("recipients", recipients);
		map.put("message", msg);
		TupleDocument<HashMap<String,Object>> doc=new TupleDocument<>(map);
		home.saveDoc("sns.cde.inbox", doc);
	}

	private String getThreadCreator(String thread) {
		// cde.imessager.threads
		String cjql = String.format(
				"select {'tuple':'*'} from tuple ?(colname) ?(clazz) where {'_id':ObjectId('?(thread)')}");
		IQuery<HashMap<String, Object>> q = home.createQuery(cjql);
		q.setParameter("colname", "cde.imessager.threads");
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("thread", thread);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null)
			return "";
		return (String) doc.tuple().get("creator");
	}

	private List<IDocument<HashMap<String, Object>>> getThreadUsers(
			String thread) {
		// cde.imessager.recipients
		String cjql = String.format(
				"select {'tuple':'*'} from tuple ?(colname) ?(clazz) where {'tuple.thread':'?(thread)'}");
		IQuery<HashMap<String, Object>> q = home.createQuery(cjql);
		q.setParameter("colname", "cde.imessager.recipients");
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("thread", thread);
		List<IDocument<HashMap<String, Object>>> docs = q.getResultList();
		return docs;
	}

	private String getLogger(String user) {
		// cde.logger.logins
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.ltime':-1}).limit(1) from tuple ?(colname) ?(clazz) where {'tuple.user':'?(user)'}");
		IQuery<HashMap<String, Object>> q = home.createQuery(cjql);
		q.setParameter("colname", "cde.logger.logins");
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("user", user);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null)
			return "";
		return (String) doc.tuple().get("session");
	}

	private List<IDocument<HashMap<String, Object>>> follower(String user) {
		String cjql = String.format(
				"select {'tuple':'*'} from tuple ?(colname) ?(clazz) where {'tuple.following':'?(following)'}");
		IQuery<HashMap<String, Object>> q = home.createQuery(cjql);
		q.setParameter("colname", "cde.contacts");
		q.setParameter("clazz", HashMap.class.getName());
		q.setParameter("following", user);
		List<IDocument<HashMap<String, Object>>> docs = q.getResultList();
		return docs;
	}
}
