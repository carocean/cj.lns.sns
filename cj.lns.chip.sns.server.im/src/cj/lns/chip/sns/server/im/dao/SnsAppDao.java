package cj.lns.chip.sns.server.im.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cj.lns.chip.sns.server.im.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.SnsApp;
@CjService(name="snsAppDao")
public class SnsAppDao implements ISnsAppDao{
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@Override
	public SnsApp getApp(String appCode) throws CircuitException {
		ICube home = db.getLnsDataHome();
		IQuery<SnsApp> q=home.createQuery("select {'tuple':'*'} from tuple sns.app ?(clazz) where {'tuple.code':'?(code)'}");
		q.setParameter("code", appCode);
		q.setParameter("clazz", SnsApp.class.getName());
		IDocument<SnsApp> apptuple=q.getSingleResult();
		if(apptuple==null){
			throw new CircuitException("404", String.format("应用代码不存在：%s",appCode));
		}
		SnsApp app=apptuple.tuple();
		return app;
	}

	@Override
	public Map<String, SnsApp> getAll() {
		ICube home = db.getLnsDataHome();
		IQuery<SnsApp> q=home.createQuery("select {'tuple':'*'} from tuple sns.app ?(clazz) where {}");
		q.setParameter("clazz", SnsApp.class.getName());
		List<IDocument<SnsApp>> apps=q.getResultList();
		Map<String,SnsApp> map=new HashMap<>();
		for(IDocument<SnsApp> doc:apps){
			SnsApp a=doc.tuple();
			map.put(a.getCode(), a);
		}
		return map;
	}

}
