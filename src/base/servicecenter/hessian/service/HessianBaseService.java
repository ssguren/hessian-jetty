package base.servicecenter.hessian.service;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.caucho.hessian.server.HessianServlet;

public abstract class HessianBaseService extends HessianServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final Logger log = Logger.getLogger(getClass());

	// private ISpUnifiedService spUnifiedService = null;

	@PostConstruct
	public void postConstruct() {
		log.info(getClass() + " is inited!");
	}

	// public ISpUnifiedService getSpUnifiedService() {
	// if (spUnifiedService == null)
	// spUnifiedService = ServerStartup.getBean("spUnifiedService",
	// SpUnifiedService.class);
	//
	// return spUnifiedService;
	// }
}
