package cj.lns.chip.sos.csc.helloworld.widget;

import cj.lns.chip.sos.csc.CscWidget;
import cj.lns.chip.sos.csc.CscWidgetContext;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.frame.Circuit;
import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.IPlug;
@CjService(name="/test")
public class Test extends CscWidget{
	@Override
	public void flow(Frame frame, Circuit circuit, IPlug plug,
			CscWidgetContext ctx) {
		System.out.println("java----test");
	}
}
