package cj.studio.ecm.sns.mailbox.viewer;

import java.util.ArrayList;
import java.util.List;

public class InboxSessionViewer {
	List<SnsAppStub> apps;
	public InboxSessionViewer() {
		apps=new ArrayList<>();
	}
	public InboxSessionViewer(List<SnsAppStub> apps) {
		this.apps=apps;
	}
	public List<SnsAppStub> getApps() {
		return apps;
	}
	
}
