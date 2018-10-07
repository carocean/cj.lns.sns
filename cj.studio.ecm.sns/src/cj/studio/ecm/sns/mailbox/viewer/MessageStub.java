package cj.studio.ecm.sns.mailbox.viewer;

import cj.studio.ecm.sns.BaseMessage;
import cj.studio.ecm.sns.mailbox.Message;

public class MessageStub extends BaseMessage{
	 String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void fill(Message msg) {
		setArriveTime(msg.getArriveTime());
		setBody(msg.getBody());
		setId(msg.getId());
		setSenderOnSws(msg.getSenderOnSws());
		setSwstid(msg.getSwstid());
		setReadTime(msg.getReadTime());
		setReplyid(msg.getReplyid());
		setSender(msg.getSender());
		setSendTime(msg.getSendTime());
		setSid(msg.getSid());
		setState(msg.getState());
		getSource().putAll(msg.getSource());
	}
	public void fillTo(Message m) {
		m.setArriveTime(this.getArriveTime());
		m.setBody(getBody());
		m.setId(getId());
		m.setReadTime(getReadTime());
		m.setReplyid(getReplyid());
		m.setSender(getSender());
		m.setSenderOnSws(getSenderOnSws());
		m.setSendTime(getSendTime());
		m.setSid(getSid());
		m.setState(getState());
		m.setSwstid(getSwstid());
		m.getSource().putAll(getSource());
	}
}
