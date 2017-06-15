package hessian.server;

import hessian.conf.HettyConfig;

import org.apache.log4j.Logger;

import base.util.StringUtil;

public class TimeOutJobHandler {
	private static final Logger log = Logger.getLogger(TimeOutJobHandler.class);

	private static final HettyConfig hettyConfig = HettyConfig.getInstance();
	// private static ISomethingCacheRedis somethingCacheRedis;
	private static long limitCount = Long.valueOf(hettyConfig.getProperty(
			"spId.job.timeout.count", "50"));
	private static int jobTimeoutExp = Integer.valueOf(hettyConfig.getProperty(
			"spId.job.timeout.count.expirySec", "600"));

	public static boolean isJobPass(String spId) {
		if (!StringUtil.isEmptyStr(spId)) {
			// log.info("isJobPass check spId : " + spId);
			// if (somethingCacheRedis != null) {
			// long count = somethingCacheRedis.getSpIdTimeoutCount(spId);
			// if (count < limitCount) {
			// // somethingCacheRedis.incrSpIdTimeoutCount(spId,
			// // jobTimeoutExp);
			// return true;
			// }
			// log.warn("request abandon with spId-" + spId
			// + " is time out with count:" + count
			// + " default count is " + limitCount);
			// return false;
			// }

			log.warn("redis is null , job default auth is pass!");
			return true;
		}

		return true;
	}

	public static void incrJobTimeoutCount(String spId) {
		if (!StringUtil.isEmptyStr(spId)) {
			log.warn("job time out and incr count with spId:" + spId);

			// if (somethingCacheRedis != null) {
			// somethingCacheRedis.incrSpIdTimeoutCount(spId, jobTimeoutExp);
			// } else {
			// log.warn("redis is null , job time out count default is pass!");
			// }
			log.warn("redis is null , job time out count default is pass!");
		}
	}

	public static void clearSpIdTimeoutCount(String spId) {
		if (!StringUtil.isEmptyStr(spId)) {
			log.info("clear job time out count with spId:" + spId);

			// if (somethingCacheRedis != null) {
			// somethingCacheRedis.resetSpIdTimeoutCount(spId);
			// } else {
			// log.warn("redis is null, clear job time out count default is pass!");
			// }
			log.warn("redis is null, clear job time out count default is pass!");
		}
	}

	// public void setSomethingCacheRedis(ISomethingCacheRedis
	// somethingCacheRedis) {
	// TimeOutJobHandler.somethingCacheRedis = somethingCacheRedis;
	// }
}
