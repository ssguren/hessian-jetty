package base.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class SemaphoreUtil {

	private static final Logger log = Logger.getLogger(SemaphoreUtil.class);

	private static Map<String, Semaphore> spSemaphore = new HashMap<String, Semaphore>();

	private static long timeout = 1000;

	private static int defaultPermit = 24;

	public static final boolean acquire(final String spId) {
		Semaphore sem = getSemaphore(spId);
		if (sem != null) {
			try {
				boolean res = sem.tryAcquire(timeout, TimeUnit.MILLISECONDS);
				if (!res)
					log.error("spSemaphore try to acquire within " + timeout
							+ " mills failed, spId=" + spId);
				return res;
			} catch (InterruptedException e) {
				log.error(MiscUtil.traceInfo(e));
				return false;
			}
		} else {
			log.error("Can't get or create spSemaphore for spId=" + spId);
			return false;
		}
	}

	private static Semaphore getSemaphore(String spId) {
		Semaphore semaphore = spSemaphore.get(spId);

		if (semaphore == null) {
			synchronized (spSemaphore) {
				semaphore = new Semaphore(defaultPermit);
				spSemaphore.put(spId, semaphore);
				log.info("Create spSemaphore with " + defaultPermit
						+ " permits for spId=" + spId);
			}
		}

		return semaphore;
	}

	public static final void release(final String spId) {
		Semaphore sem = spSemaphore.get(spId);
		if (sem != null) {
			sem.release();
		}
	}

	public void setTimeout(long timeout) {
		SemaphoreUtil.timeout = timeout;
	}

	public void setDefaultPermit(int defaultPermit) {
		SemaphoreUtil.defaultPermit = defaultPermit;
	}

	public void setSpSemaphore(Map<String, Semaphore> spSemaphore) {
		SemaphoreUtil.spSemaphore = spSemaphore;
		Iterator<Entry<String, Semaphore>> iter = spSemaphore.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Semaphore> en = iter.next();
			log.info("spId=" + en.getKey() + ", permits="
					+ en.getValue().availablePermits());
		}
	}
}
