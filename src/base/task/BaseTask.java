package base.task;

import org.apache.log4j.Logger;

import base.util.MiscUtil;

public abstract class BaseTask implements Runnable {

	protected final Logger log = Logger.getLogger(getClass());

	@Override
	public final void run() {
		try {
			work();
		} catch (Exception e) {
			log.error(MiscUtil.traceInfo(e));
		}
	}

	protected abstract void work() throws Exception;
}
