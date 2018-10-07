package cj.lns.chip.sns.server.device.cde;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.ultimate.IDisposable;
import cj.ultimate.util.StringUtil;

@CjService(name = "reporterCenter")
public class ReporterCenter
		implements IReporterCenter, IServiceAfter, IDisposable {
	Semaphore pool;
	ReentrantLock reen;
	ConcurrentHashMap<String, Condition> locks;// 会话锁,超时可改，默认5分钟
	ConcurrentHashMap<String, TerminusReporter> sessions;// 会话数据,消息中心按会话CjToken作为信道

	@Override
	public void onAfter(IServiceSite site) {
		int capacity = 8192;
		String cap = site.getProperty("cde.reporter.capacity");
		if (!StringUtil.isEmpty(cap)) {
			capacity = Integer.valueOf(cap);
		} else {
			CJSystem.current().environment().logging().warn(this.getClass()
					+ " 未指定环境变量：cde.reporter.capacity，采用默认值：" + capacity);
		}
		init(capacity);
	}

	protected void init(int capacity) {
		sessions = new ConcurrentHashMap<>();
		locks = new ConcurrentHashMap<>();
		pool = new Semaphore(capacity);
		reen = new ReentrantLock();
	}

	@SuppressWarnings("static-access")
	public static void main(String... reen) throws InterruptedException {
		ReporterCenter center = new ReporterCenter();
		center.init(8192);
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < 10; i++) {
					TerminusReporter reporter = center.take("aa", 3600);
					if(reporter==null){
						System.out.println("take it:" + reporter);
					}else{
						System.out.println("take it:" + reporter.count()+" "+reporter);
					}
				}
			}
		}).start();
		Thread.currentThread().sleep(1000);
		center.notifya("aa", new TerminusMessage());
		for (int i = 0; i < 10; i++) {//多次放一只要有一次take就可以
			center.notifya("aa", new TerminusMessage());
//			Thread.currentThread().sleep(1000);
		}
		System.out.println(center);
	}

	// 取消息报告，如果会话id为空，则添加会话（此时为空数据）并等待直到超时；如果会话堵塞被通知，则由notifya方法通知并放入数据，则此时take返回消息报告
	@Override
	public TerminusReporter take(String sid, long timeout) {
		TerminusReporter reporter = sessions.get(sid);
		if (reporter != null && reporter.count() > 0) {//有的话就直接返回
			sessions.remove(sid);
			Condition locker=locks.get(sid);
			if(locker!=null){
				try {
					reen.lock();
				locker.signalAll();
				} finally {
					reen.unlock();
				}
			}
			locks.remove(sid);// 只要方法能执行过，则说明锁用完就必须释放。
			pool.release();
			return reporter;
		}
		Condition lock = locks.get(sid);
		if (lock == null && assignLocker(sid)) {
			lock = locks.get(sid);
//			System.out.println("take assign");
		}
		try {
			reen.lock();
			if (timeout < 0) {
				lock.await();
			} else {
				// 当该方法返回false时为超时。可是在黄金点上受到通知时，它并不总是在超时时退出，有时正确的通知到了它也返回false，但不影响结果正确。
				// 估计是通知时与超时时的判断逻辑不正确，故而无法用超时。
				// 之前版本是使用:Object.wait(222);
				// lock.await(m,TimeUnit.MILLISECONDS);
				if (!lock.await(timeout, TimeUnit.MILLISECONDS)) {
					// throw new
					// TimeOutException(String.format("%s,设定的超时毫秒：%s",
					// key,m));
					// CJSystem.current()
					// .environment()
					// .logging()
					// .debug(String.format(
					// "确认侦等待超时.id：%s,设定的超时毫秒：%s", key, m));
				}
			}
//			System.out.println("take lock:"+lock);
			reporter = sessions.get(sid);
			return reporter;
		} catch (InterruptedException e) {
			// locks.remove(key);// 如果超时了，则移除，并同步池数
			// extra.remove(key);
			// pool.release();
			// System.out.println(String.format("线程中断或因超时：%s",key));
			return null;
		} finally {
			sessions.remove(sid);
			locks.remove(sid);// 只要方法能执行过，则说明锁用完就必须释放。
			reen.unlock();
			pool.release();
		}
	}

	@Override
	public boolean notifya(String sid, TerminusMessage msg) {
		Condition lock = locks.get(sid);
		if (lock == null && assignLocker(sid)) {
			lock = locks.get(sid);
//			System.out.println("notify assign");
		}
		try {
			reen.lock();
			// System.out.println("notifya :" + key + " " + data);
			TerminusReporter reporter = sessions.get(sid);
			if (reporter == null) {
				reporter = new TerminusReporter(sid);
				sessions.put(sid, reporter);
			}
			reporter.add(msg);
//			System.out.println("notify lock:"+lock);
			lock.signalAll();
			return true;
			// locks.remove(key);//注掉原因：如果通知时将锁移除，则在take时得不到锁，如果take线程还未收到notify通知，而将此锁移除了，因为在take方法锁非空才进入方法，因此为导致take返回为空值。
			// pool.release();//注掉原因：因为在take一个时释放一个容量最为合理，否则在此处释放一个，take处的locks&extra还未腾出一个池元素。
		} catch (Exception e) {
			CJSystem.current().environment().logging().error(this.getClass(),
					e.getMessage());
			return false;
		} finally {
			reen.unlock();
		}
	}
	@Override
	public void notifyExistsAll(TerminusMessage msg) {
		for(String sid:locks.keySet()){
			notifya(sid, msg);
		}
		
	}
	protected boolean assignLocker(String sid) {
		try {
			reen.lock();
			if (!locks.containsKey(sid)) {// 如果已存在则是替换，不存在则视为添加，因此需要等等
				pool.acquire();
			}
			locks.put(sid, reen.newCondition());
			return true;
		} catch (InterruptedException e) {
			return false;
		} finally {
			reen.unlock();
		}

	}

	@Override
	public void dispose() {
		pool.release(pool.availablePermits());
		locks.clear();
		sessions.clear();
	}
}
