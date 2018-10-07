package cj.lns.chip.sns.server.device.service.mailbox;

import cj.studio.ecm.sns.ICloudRequestService;
import cj.studio.ecm.sns.ITerminusRequestService;

/**
 * 收件箱，对应入港
 * <pre>
 *	－写入接收消息到收件表
 *	－查询（排序）
 * </pre>
 * @author carocean
 *
 */
public interface IInboxService extends ITerminusRequestService,ICloudRequestService{
	
}
