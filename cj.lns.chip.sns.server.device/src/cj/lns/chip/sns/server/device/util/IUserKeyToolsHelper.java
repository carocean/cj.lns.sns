package cj.lns.chip.sns.server.device.util;

import cj.studio.ecm.sns.UserKeyTools;

public interface IUserKeyToolsHelper {
	String KEY_COLLECTIONS_NAME="userKeyTools";
	UserKeyTools getUKey(String userName);

}
