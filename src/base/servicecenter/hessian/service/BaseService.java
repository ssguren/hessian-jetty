package base.servicecenter.hessian.service;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

public abstract class BaseService {

	protected final Logger log = Logger.getLogger(getClass());

	@PostConstruct
	public void postConstruct() {

		log.info(getClass() + " is inited!");
	}
}
