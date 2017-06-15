package base.task.executor;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import base.util.MiscUtil;

public class SpForkJoinPool {

	private static Logger log = Logger.getLogger(SpForkJoinPool.class);

	private static int awaitTermination = 20000;

	public static MyForkJoinPool forkJoinPool = new MyForkJoinPool(Runtime
			.getRuntime().availableProcessors(),
			new MyDefaultForkJoinWorkerThreadFactory(), null, false);

	public static String reportMyForkJoinPoolStatus() {
		synchronized (forkJoinPool) {
			StringBuffer sb = new StringBuffer();
			sb.append("ActiveThreadCount: "
					+ forkJoinPool.getActiveThreadCount() + "\n");
			sb.append("Parallelism: " + forkJoinPool.getParallelism() + "\n");
			sb.append("PoolSize: " + forkJoinPool.getPoolSize() + "\n");
			sb.append("QueuedSubmissionCount: "
					+ forkJoinPool.getQueuedSubmissionCount() + "\n");
			sb.append("QueuedTaskCount: " + forkJoinPool.getQueuedTaskCount()
					+ "\n");
			sb.append("RunningThreadCount: "
					+ forkJoinPool.getRunningThreadCount() + "\n");
			sb.append("StealCount: " + forkJoinPool.getStealCount() + "\n");
			sb.append("AsyncMode: " + forkJoinPool.getAsyncMode() + "\n");

			return sb.toString();
		}
	}

	public static Future<?> shutdownGracefully() {
		ExecutorService excutor = Executors.newFixedThreadPool(1);
		Future<?> future = excutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					forkJoinPool.shutdown();
					forkJoinPool.awaitTermination(awaitTermination,
							TimeUnit.MILLISECONDS);
					log.info("forkJoinPool shutdown gracefully... ...");
				} catch (InterruptedException e) {
					log.error("forkJoinPool can't shutdown gracefully... ...");
					log.error(MiscUtil.traceInfo(e));
				}
			}
		});

		return future;
	}

	public static class MyForkJoinPool extends ForkJoinPool {
		public MyForkJoinPool(int parallelism,
				ForkJoinWorkerThreadFactory factory,
				UncaughtExceptionHandler handler, boolean asyncMode) {
			super(parallelism, factory, handler, asyncMode);
		}

		public final int removeAllTask() {
			log.warn("begin to remove all tasks...");
			List<ForkJoinTask<?>> removed = new ArrayList<ForkJoinTask<?>>();
			int count = super.drainTasksTo(removed);
			log.warn("total remove " + count + " tasks...");

			return count;
		}
	}

	static class MyDefaultForkJoinWorkerThreadFactory implements
			ForkJoinWorkerThreadFactory {
		@Override
		public MyForkJoinWorkerThread newThread(ForkJoinPool pool) {
			MyForkJoinWorkerThread worker = new MyForkJoinWorkerThread(pool);

			return worker;
		}
	}

	static class MyForkJoinWorkerThread extends ForkJoinWorkerThread {
		protected MyForkJoinWorkerThread(ForkJoinPool pool) {
			super(pool);
		}
	}

	public static void setAwaitTermination(int awaitTermination) {
		SpForkJoinPool.awaitTermination = awaitTermination;
	}
}
