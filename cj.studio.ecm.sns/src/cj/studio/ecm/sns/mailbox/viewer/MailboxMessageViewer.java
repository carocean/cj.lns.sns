package cj.studio.ecm.sns.mailbox.viewer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MailboxMessageViewer {
	Map<String,MessageStub> messages;
	Map<String, MySessionStub> sessions;//key=sessionid
	public MailboxMessageViewer() {
		messages=new LinkedHashMap<>();
		sessions=new HashMap<>();
	}
	public Map<String, MySessionStub> getSessions() {
		return sessions;
	}
	public Map<String, MessageStub> getMessages() {
		return messages;
	}
}
