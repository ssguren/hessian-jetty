package base.task.executor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import org.apache.log4j.Logger;

public class PurgeAndAbortPolicy extends AbortPolicy {

	private static Logger log = Logger.getLogger(PurgeAndAbortPolicy.class);

	private final String executorName;

	public PurgeAndAbortPolicy(String executorName) {
		this.executorName = executorName;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		log.error(executorName + " threadpool full, current pool size is: "
				+ e.getPoolSize() + ", active count: " + e.getActiveCount());
		e.purge();// Tries to remove from the work queue all Future tasks that
					// have been cancelled.
		log.info(executorName + " purge completed, current pool size is: "
				+ e.getPoolSize() + ", active count: " + e.getActiveCount());
		super.rejectedExecution(r, e);
	}
}
