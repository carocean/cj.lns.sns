package cj.lns.chip.sns.server.fs.sink;

import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.annotation.CjService;

@CjService(name="authFactory")
public class AuthFactory implements IAuthFactory{
	Map<String,Boolean> passMap;
	public AuthFactory() {
		passMap=new HashMap<>();
	}
	@Override
	public void off(String selector) {
		// TODO Auto-generated method stub
		passMap.remove(selector);
	}

	@Override
	public void on(String selector) {
		passMap.put(selector, false);
	}

	@Override
	public void flagSelectorPass(String selector) {
		passMap.put(selector, true);
	}

}
