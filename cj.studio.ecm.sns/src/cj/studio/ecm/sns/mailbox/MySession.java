package cj.studio.ecm.sns.mailbox;

import cj.studio.ecm.sns.BaseMySession;

/**
 * 会话
 * <pre>
 * 表示用户设备上的会话，即我的会话，该会话代表用户设备上收到的消息集合
 * 
 * 会话保存在用户的视窗下
 * 
 * 会话持有一个互动实体
 * 视窗的消息：inbox,outbox等均以会话标识记录
 * 
 * 会话的消息即设备的消息，与互动实体的消息不同，互动实体的消息是姆版，如果设备的消息删除，用户还可在互动实体的消息列表中找回。
 * 
 * 因此，用户视窗里，看到的是会话，并对自己的会话进行管理
 * </pre>
 * @author carocean
 *
 */
//会话是用户某个视窗下的私有实体
public class MySession extends BaseMySession{
	public static String KEY_COLL_NAME="sns.session";
	transient String sid;
	public MySession() {
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	
}
