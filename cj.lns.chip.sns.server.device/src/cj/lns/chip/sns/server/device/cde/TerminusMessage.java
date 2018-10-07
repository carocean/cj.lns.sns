package cj.lns.chip.sns.server.device.cde;

public class TerminusMessage {
	String module;//产生消息的模块
	String sender;//发送者是谁
	String message;//具体指哪个消息，如：imessager的thread号,
	Object data;//附带的数据
	public TerminusMessage() {
		// TODO Auto-generated constructor stub
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
