package cj.studio.ecm.sns;

/**
 * 表示用户设备所在的结点，它等同于代表一个用户
 * 
 * <pre>
 *
 * </pre>
 * 
 * @author carocean
 *
 */
public class Peer {
	transient String id;
	String selectId;
	String selectName;
	String onAddress;
	String onUser;
	String swsid;//当前登录者所在视窗号
	long peerOnlineTime;
	long userOnlineTime;
	public String getSwsid() {
		return swsid;
	}
	public void setSwsid(String swsid) {
		this.swsid = swsid;
	}
	public String getSelectId() {
		return selectId;
	}

	public String getSelectName() {
		return selectName;
	}

	public void setSelectId(String selectId) {
		this.selectId = selectId;
	}

	public void setSelectName(String selectName) {
		this.selectName = selectName;
	}

	public String getOnUser() {
		return onUser;
	}

	public void setOnUser(String onUser) {
		this.onUser = onUser;
	}

	public long getPeerOnlineTime() {
		return peerOnlineTime;
	}

	public long getUserOnlineTime() {
		return userOnlineTime;
	}

	public void setPeerOnlineTime(long peerOnlineTime) {
		this.peerOnlineTime = peerOnlineTime;
	}

	public void setUserOnlineTime(long userOnlineTime) {
		this.userOnlineTime = userOnlineTime;
	}

	public String getId() {
		return id;
	}

	public String getOnAddress() {
		return onAddress;
	}

	public void setId(String sid) {
		this.id = sid;
	}

	public void setOnAddress(String onAddress) {
		this.onAddress = onAddress;
	}

	public static String indexKey(String onAddress, String sname, String sid) {
		return String.format("%s#%s#%s", onAddress,sname,sid);
	}

}
