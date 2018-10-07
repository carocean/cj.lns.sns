package cj.lns.chip.sns.server.im.cache;

import java.util.List;

import cj.studio.ecm.sns.Peer;
import cj.ultimate.IClosable;

public interface IPeerCache extends IClosable {
	public static String KEY_COLLECTIONS_NAME = "peers";
	void flush(Peer peer);
	
	Peer createPeer(String sid, String sname,  long peerOnlineTime);

	void removePeer(String sname, String sid);

	Peer getPeer( String sname, String sid);
	boolean containsPeer(String sname, String sid);
	void close();

	void setOnAddress(String onAddress);
	String getOnAddress();

	void checkUsers(List<String> users, List<Peer> onlines,
			List<String> offlines);

	List<Peer> getPeers(String user);
}
