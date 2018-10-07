package cj.lns.chip.sns.server.device;

import cj.studio.ecm.graph.CjStatusDef;
import cj.studio.ecm.graph.IConstans;

public interface DeviceIConstans extends IConstans{
	@CjStatusDef(message = "PEER/1.0")
	String PROTOCAL = "protocol";
	@CjStatusDef(message="ok")
	String STATUS_200="200";
	@CjStatusDef(message="设备上未有登录用户，请终端登录后使用其它服务,请求：%s")
	String STATUS_301 = "301";
}
