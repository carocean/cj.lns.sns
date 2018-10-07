package cj.studio.ecm.sns.mailbox;

import cj.studio.ecm.sns.BaseMessage;

public class Message extends BaseMessage{
	transient String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
