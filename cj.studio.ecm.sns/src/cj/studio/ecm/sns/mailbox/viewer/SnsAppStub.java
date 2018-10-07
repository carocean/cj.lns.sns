package cj.studio.ecm.sns.mailbox.viewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cj.studio.ecm.sns.mailbox.SnsApp;

public class SnsAppStub extends SnsApp {

	public SnsAppStub(String appCode, String appName, String icon) {
		super(appCode, appName, icon);
		this.sessions = new ArrayList<>();
	}

	List<MySessionStub> sessions;

	public List<MySessionStub> getSessions() {
		return sessions;
	}

	public long totalMessages() {
		int v = 0;
		for (MySessionStub s : sessions) {
			v += s.totalMsgs;
		}
		return v;
	}

	public String getTime() {
		if (sessions.isEmpty())
			return "";
		MySessionStub s = sessions.get(0);
		if (s.getNewestMsg() == null)
			return "";
		Date d = new Date(s.getNewestMsg().getArriveTime());
		SimpleDateFormat f = new SimpleDateFormat("MM/dd HH:mm:ss");
		return f.format(d);
	}

	public String getSender() {
		if (sessions.isEmpty())
			return "";
		MySessionStub s = sessions.get(0);
		if (s.getNewestMsg() == null)
			return "";
		return s.getNewestMsg().getSender();
	}

	public String getText() {
		if (sessions.isEmpty())
			return "";
		MySessionStub s = sessions.get(0);
		if (s.getNewestMsg() == null)
			return "";
		if (s.getNewestMsg().getBody() == null)
			return "";
		return new String(s.getNewestMsg().getBody());
	}
}
