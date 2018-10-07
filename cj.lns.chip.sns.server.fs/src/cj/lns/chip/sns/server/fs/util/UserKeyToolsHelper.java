package cj.lns.chip.sns.server.fs.util;

import cj.lns.chip.sns.server.fs.IDatabaseCloud;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.sns.UserKeyTools;
@CjService(name="userKeyToolsHelper")
public class UserKeyToolsHelper implements IUserKeyToolsHelper{
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;
	@Override
	public UserKeyTools getUKey(String userName) {
		String cubeql="select {'tuple':'*'} from tuple ?(tuple) ?(clazz) where {'tuple.user':'?(user)'}";
		IQuery<UserKeyTools> q=db.getLnsSysHome().createQuery(cubeql);
		q.setParameter("tuple", KEY_COLLECTIONS_NAME);
		q.setParameter("clazz", UserKeyTools.class.getName());
		q.setParameter("user", userName);
		IDocument<UserKeyTools> tuple=q.getSingleResult();
		if(tuple==null){
			return null;
		}
		return tuple.tuple();
	}

}
