package cj.lns.chip.sns.server.im;

import cj.studio.ecm.graph.CjStatusDef;
import cj.studio.ecm.graph.IConstans;

public interface ImConstans extends IConstans{
	@CjStatusDef(message = "IM/1.0")
	String PROTOCAL = "protocol";
	@CjStatusDef(message="ok")
	String STATUS_200="200";
}
