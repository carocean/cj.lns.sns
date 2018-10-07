package cj.lns.chip.sns.server.device.cde;
/*
 * 
 * 完成推送逻辑，和消息缓冲功能
 */
public interface IReporterCenter {

	TerminusReporter take(String sid, long timeout);


	boolean notifya(String sid,TerminusMessage msg);
	void notifyExistsAll(TerminusMessage msg);
}
