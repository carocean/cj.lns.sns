package cj.studio.ecm.sns;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseMessage {
	String sid;
	String replyid;//表示回执
	byte[] body;
	byte state;//当是inbox时，1已读0未读；当是outbox时1已发0未发
	String sender;
	String senderOnSws;
	String swstid;//视窗模板标识，类似于windows的窗口句柄，事件只发生在该标识之内。
	long sendTime;//发出时间
	long arriveTime;//到站时间
	long readTime;//读取时间
	Map<String,Object> source;//来源
	String iobox;
	public String getIobox() {
		return iobox;
	}
	public void setIobox(String iobox) {
		this.iobox = iobox;
	}
	public BaseMessage() {
		source=new HashMap<>();
	}
	public Map<String, Object> getSource() {
		return source;
	}
	public String getSwstid() {
		return swstid;
	}
	public void setSwstid(String swstid) {
		this.swstid = swstid;
	}
	public String getSenderOnSws() {
		return senderOnSws;
	}
	public void setSenderOnSws(String senderOnSws) {
		this.senderOnSws = senderOnSws;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getReplyid() {
		return replyid;
	}
	public void setReplyid(String replyid) {
		this.replyid = replyid;
	}
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}
	public byte getState() {
		return state;
	}
	public void setState(byte state) {
		this.state = state;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public long getSendTime() {
		return sendTime;
	}
	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}
	public long getArriveTime() {
		return arriveTime;
	}
	public void setArriveTime(long arriveTime) {
		this.arriveTime = arriveTime;
	}
	public long getReadTime() {
		return readTime;
	}
	public void setReadTime(long readTime) {
		this.readTime = readTime;
	}
	
}
