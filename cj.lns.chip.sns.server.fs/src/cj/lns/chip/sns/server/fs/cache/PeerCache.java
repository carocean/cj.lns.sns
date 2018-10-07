package cj.lns.chip.sns.server.fs.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.fs.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.sns.Peer;
import cj.studio.ecm.sns.util.LRULinkedHashMap;

@CjService(name = "peerCache")
public class PeerCache implements IPeerCache {

	Map<String, Peer> caches;// key=peerid
	Map<String, String> index;// key=indexKey,value=peerId
	String onAddress;
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	int lruCapacity;

	public PeerCache() {
		lruCapacity = 1024 * 10;
		caches = new LRULinkedHashMap<>(lruCapacity);
		index = new LRULinkedHashMap<>(lruCapacity);
	}

	@Override
	public String getOnAddress() {
		// TODO Auto-generated method stub
		return onAddress;
	}

	@Override
	public void setOnAddress(String onAddress) {
		// TODO Auto-generated method stub
		this.onAddress = onAddress;
	}

	@Override
	public void flush(Peer peer) {
		// 永远不会是新建，如果非托管则覆盖缓存而后以新建方式存入，其它的视为更新

		db.getLnsSysHome().updateDoc(KEY_COLLECTIONS_NAME, peer.getId(),
				new TupleDocument<Peer>(peer));
		if (caches.containsKey(peer.getId())) {
			Peer store = caches.get(peer.getId());
			if (!store.equals(peer)) {
				caches.put(peer.getId(), peer);
			}
		}
	}

	@Override
	public Peer createPeer(String sid, String sname, long peerOnlineTime) {
		Peer peer = new Peer();
		peer.setOnAddress(onAddress);
		peer.setPeerOnlineTime(peerOnlineTime);
		peer.setSelectId(sid);
		peer.setSelectName(sname);
		TupleDocument<Peer> doc = new TupleDocument<Peer>(peer);
		String id = db.getLnsSysHome().saveDoc(KEY_COLLECTIONS_NAME, doc);
		peer.setId(id);
		caches.put(id, peer);
		String indexKey = Peer.indexKey(onAddress, sname, sid);
		index.put(indexKey, id);
		return peer;
	}

	@Override
	public void removePeer(String sname, String sid) {
		String bson = String.format(
				"{'tuple.selectName':'%s','tuple.selectId':'%s','tuple.onAddress':'%s'}",
				sname, sid, onAddress);
		db.getLnsSysHome().deleteDocs(KEY_COLLECTIONS_NAME, bson);
		String indexKey = Peer.indexKey(onAddress, sname, sid);
		if (index.containsKey(indexKey)) {
			caches.remove(index.get(indexKey));
			index.remove(indexKey);
		}
	}

	@Override
	public List<Peer> getPeers(String user) {
		// 到数据库中取
		String cubeql = "select {'tuple':'*'} from tuple ?(tupleName) ?(clazz) where {'tuple.onUser':'?(user)'}";
		IQuery<Peer> q = db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tupleName", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", Peer.class.getName());
		q.setParameter("user", user);
		List<IDocument<Peer>> list = q.getResultList();
		List<Peer> result = new ArrayList<Peer>();
		for (IDocument<Peer> doc : list) {
			Peer peer = doc.tuple();
			peer.setId(doc.docid());
			caches.put(peer.getId(), peer);
			result.add(peer);
		}
		return result;
	}

	@Override
	public Peer getPeer(String user) {
		// 到数据库中取
		String cubeql = "select {'tuple':'*'} from tuple ?(tupleName) ?(clazz) where {'tuple.onUser':'?(user)'}";
		IQuery<Peer> q = db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tupleName", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", Peer.class.getName());
		q.setParameter("user", user);
		IDocument<Peer> tuple = q.getSingleResult();
		if (tuple == null)
			return null;

		Peer peer = tuple.tuple();
		peer.setId(tuple.docid());
		caches.put(peer.getId(), peer);
		return peer;
	}
	@Override
	public Peer getPeerById(String pid) {
		if(caches.containsKey(pid)){
			return caches.get(pid);
		}
		// 到数据库中取
		String cubeql = "select {'tuple':'*'} from tuple ?(tupleName) ?(clazz) where {'_id':ObjectId('?(peerid)')}";
		IQuery<Peer> q = db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tupleName", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", Peer.class.getName());
		q.setParameter("peerid", pid);
		IDocument<Peer> tuple = q.getSingleResult();
		if (tuple == null)
			return null;

		Peer peer = tuple.tuple();
		peer.setId(tuple.docid());
		caches.put(peer.getId(), peer);
		return peer;
	}
	@Override
	public Peer getPeer(String sname, String sid) {

		Peer peer = caches.get(index.get(Peer.indexKey(onAddress, sname, sid)));
		if (peer != null) {
			return peer;
		}
		// 到数据库中取
		String cubeql = String.format(
				"select {'tuple':'*'} from tuple ?(tupleName) ?(clazz) where {'tuple.selectName':'?(sname)','tuple.selectId':'?(sid)','tuple.onAddress':'?(address)'}",
				sname, sid, onAddress);
		IQuery<Peer> q = db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tupleName", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", Peer.class.getName());
		q.setParameter("sname", sname);
		q.setParameter("sid", sid);
		q.setParameter("address", onAddress);
		IDocument<Peer> tuple = q.getSingleResult();
		if (tuple == null) {
			return null;
		}
		peer = tuple.tuple();
		peer.setId(tuple.docid());
		caches.put(peer.getId(), peer);
		String indexKey = Peer.indexKey(onAddress, sname, sid);
		index.put(indexKey, peer.getId());
		return peer;
	}

	@Override
	public boolean containsPeer(String sname, String sid) {
		String indexKey = index.get(Peer.indexKey(onAddress, sname, sid));
		if (!index.containsKey(indexKey)) {
			return false;
		}
		if (caches.containsKey(index.get(indexKey))) {
			return true;
		}
		// 到加中查
		String cubeql = String.format(
				"select {'tuple':'*'}.count() from tuple ?(tupleName) ?(clazz) where {'tuple.selectName':'?(sname)','tuple.selectId':'?(sid)','tuple.onAddress':'?(address)'}",
				sname, sid, onAddress);
		IQuery<Peer> q = db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tupleName", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", Peer.class.getName());
		q.setParameter("sname", sname);
		q.setParameter("sid", sid);
		q.setParameter("address", onAddress);
		return q.count() > 0;
	}

	@Override
	public void close() {
		caches.clear();
		index.clear();
		db.getLnsSysHome().deleteDocs(KEY_COLLECTIONS_NAME,
				String.format("{'tuple.onAddress':'%s'}", onAddress));
	}
}
