package cj.studio.ecm.sns;

import cj.studio.ecm.sns.mailbox.MyProfile;

public abstract class BaseMySession {
	String title;//会话的标题
	String icon;//会话的图标
	String appId;//指向互动实例，互动实例是应用的某个实体，如聊天室的用户a-b的聊天室
	String appCode;
	long createTime;
	MyProfile profile;//会话窗口的用户使用偏好设置
	public BaseMySession() {
	}
	public MyProfile getProfile() {
		return profile;
	}
	public void setProfile(MyProfile profile) {
		this.profile = profile;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
}
