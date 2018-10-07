package cj.studio.ecm.sns.mailbox;
/**
 * 消息固定存储为四个信箱，实际上对应网盘中的表
 * <pre>
 *
 * </pre>
 * @author carocean
 *
 */
public enum Mailbox {
	inbox,outbox,
	/**
	 * 表示inbox和outbox类型的集合
	 */
	iobox,drafts,trash
}
