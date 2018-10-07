package cj.studio.ecm.sns.mailbox;

public class SnsApp {
	String code;
	String name;
	String icon;

	public SnsApp(String appCode, String appName, String icon) {
		this.code = appCode;
		this.name = appName;
		this.icon = icon;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}
}
