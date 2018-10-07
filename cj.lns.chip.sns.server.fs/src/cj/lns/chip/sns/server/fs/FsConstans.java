package cj.lns.chip.sns.server.fs;

import cj.studio.ecm.graph.CjStatusDef;
import cj.studio.ecm.graph.IConstans;

public interface FsConstans extends IConstans{
	@CjStatusDef(message = "FS/1.0")
	String PROTOCAL = "protocol";
	@CjStatusDef(message="ok")
	String STATUS_200="200";
	@CjStatusDef(message="文件服务器发现未有设备或用户在线,请求：%s")
	String STATUS_301 = "301";
}
