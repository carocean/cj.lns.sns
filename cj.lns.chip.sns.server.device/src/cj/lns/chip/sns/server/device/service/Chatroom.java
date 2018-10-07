package cj.lns.chip.sns.server.device.service;

public class Chatroom {
	public static String KEY_COLL_NAME="sns.chatroom";
	public static String KEY_COLL_USERS="sns.chatroom.users";
	String creator;
	String chatTo;
	long createTime;
	transient String id;
	private String swsid;
	public Chatroom(String creator, String swsid, String chatTo) {
		this.creator=creator;
		this.chatTo=chatTo;
		this.swsid=swsid;
		this.createTime=System.currentTimeMillis();
	}
	public String getSwsid() {
		return swsid;
	}
	public void setSwsid(String swsid) {
		this.swsid = swsid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getChatTo() {
		return chatTo;
	}
	public void setChatTo(String chatTo) {
		this.chatTo = chatTo;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
}
