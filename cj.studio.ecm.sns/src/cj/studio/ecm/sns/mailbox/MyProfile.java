package cj.studio.ecm.sns.mailbox;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话窗口的用户使用偏好设置
 * <pre>
 *
 * </pre>
 * @author carocean
 *
 */
public class MyProfile {
	Map<String,String> settings;
	public MyProfile() {
		settings=new HashMap<String, String>();
	}
	public Map<String, String> getSettings() {
		return settings;
	}
}
