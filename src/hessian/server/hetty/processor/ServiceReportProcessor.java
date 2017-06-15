package hessian.server.hetty.processor;

import hessian.model.RequestWrapper;

import org.apache.log4j.Logger;

final class ServiceReportProcessor {
	private static final Logger log = Logger
			.getLogger(ServiceReportProcessor.class);

	/**
	 * Report action before action invoking when the common request coming
	 */
	protected static final boolean reportBeforeInvoke(RequestWrapper request) {
		doReport(request);
		return true;
	}

	private static final void doReport(RequestWrapper request) {
		StringBuilder tip = new StringBuilder(
				"\n------------------ Hetty Request Report ------------------\n");
		// tip.append("user    : ").append(request.getUser()).append("\n");
		// tip.append("password: ").append(request.getPassword()).append("\n");
		tip.append("clientIP: ").append(request.getClientIP()).append("\n");
		tip.append("spId    : ").append(request.getSpId()).append("\n");
		tip.append("service : ").append(request.getServiceName()).append("\n");
		tip.append("method  : ").append(request.getMethodName()).append("\n");
		tip.append("----------------------------------------------------------\n");
		log.info(tip.toString());
	}
}
