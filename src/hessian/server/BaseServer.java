package hessian.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

public abstract class BaseServer {

	protected final Logger log = Logger.getLogger(getClass());

	@PostConstruct
	public abstract void start();

	@PreDestroy
	public abstract void stop();
}
