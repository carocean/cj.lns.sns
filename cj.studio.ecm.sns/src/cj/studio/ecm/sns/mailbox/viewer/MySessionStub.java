package cj.studio.ecm.sns.mailbox.viewer;

import cj.studio.ecm.sns.BaseMySession;
import cj.studio.ecm.sns.mailbox.Message;
import cj.studio.ecm.sns.mailbox.MySession;
import cj.studio.ecm.sns.mailbox.SnsApp;

public class MySessionStub extends BaseMySession{
	long totalMsgs;
	Message newestMsg;
	String sid;
	SnsApp snsApp;//chatroom,chatGroup,地圈等，该字段不能为空
	public SnsApp getSnsApp() {
		return snsApp;
	}
	public void setSnsApp(SnsApp snsApp) {
		this.snsApp = snsApp;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public Message getNewestMsg() {
		return newestMsg;
	}
	public void setNewestMsg(Message newestMsg) {
		this.newestMsg = newestMsg;
	}
	public long getTotalMsgs() {
		return totalMsgs;
	}
	public void setTotalMsgs(long totalMsgs) {
		this.totalMsgs = totalMsgs;
	}
	public void fill(MySession my) {
		setAppId(my.getAppId());
		setCreateTime(my.getCreateTime());
		setIcon(my.getIcon());
		setProfile(my.getProfile());
		setSid(my.getSid());
		setTitle(my.getTitle());
		setAppCode(my.getAppCode());
	}
	
}
