package base.task.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import base.util.MiscUtil;

public class ThreadPoolExecutorHelper {

	private static Logger log = Logger
			.getLogger(ThreadPoolExecutorHelper.class);

	private static ExecutorService excutor = Executors.newFixedThreadPool(4);

	public static ThreadPoolExecutor applyAppointmentThreadPool = null;

	public static ThreadPoolExecutor queryScheduleThreadPool = null;

	public static ThreadPoolExecutor workingThreadPool = null;

	private static int awaitTermination = 120000;

	static {
		applyAppointmentThreadPool = new ThreadPoolExecutor(0,
				Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(true), new NamedThreadFactory(
						"ApplyOrder"), new PurgeAndAbortPolicy("ApplyOrder"));

		queryScheduleThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
				60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true),
				new NamedThreadFactory("QuerySchedule"),
				new PurgeAndAbortPolicy("QuerySchedule"));

		workingThreadPool = new ThreadPoolExecutor(0, 1024, 60L,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true),
				new NamedThreadFactory("MainWork"), new PurgeAndAbortPolicy(
						"MainWork"));
	}

	public static Future<?> shutdownGracefully() {
		Future<?> future = excutor.submit(new Runnable() {
			@Override
			public void run() {
				Future<?> f1 = shutdownGracefully(applyAppointmentThreadPool,
						"applyAppointmentThreadPool", awaitTermination);
				Future<?> f2 = shutdownGracefully(queryScheduleThreadPool,
						"queryScheduleThreadPool", awaitTermination);
				Future<?> f3 = shutdownGracefully(workingThreadPool,
						"workingThreadPool", awaitTermination);

				while (!f1.isDone() || !f2.isDone() || !f3.isDone()) {
					if (!f1.isDone())
						log.warn("applyAppointmentThreadPool are not shutdown complete, wait...");
					if (!f2.isDone())
						log.warn("queryScheduleThreadPool are not shutdown complete, wait...");
					if (!f3.isDone())
						log.warn("workingThreadPool are not shutdown complete, wait...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.error(MiscUtil.traceInfo(e));
					}
				}
			}
		});

		return future;
	}

	private static Future<?> shutdownGracefully(final ExecutorService service,
			final String serviceName, final long timeout) {
		Future<?> future = excutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					service.shutdown();
					service.awaitTermination(timeout, TimeUnit.MILLISECONDS);
					log.info(serviceName + " shutdown gracefully... ...");
				} catch (InterruptedException e) {
					log.error(serviceName + " can't shutdown gracefully... ...");
					log.error(MiscUtil.traceInfo(e));
				}
			}
		});

		return future;
	}

	public static String reportWorkingThreadPoolStatus() {
		synchronized (workingThreadPool) {
			StringBuffer sb = new StringBuffer();
			sb.append("CompletedTaskCount: "
					+ workingThreadPool.getCompletedTaskCount() + "\n");
			sb.append("ActiveCount: " + workingThreadPool.getActiveCount()
					+ "\n");

			return sb.toString();
		}
	}

	public static void setAwaitTermination(int awaitTermination) {
		ThreadPoolExecutorHelper.awaitTermination = awaitTermination;
	}
}
