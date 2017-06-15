package hessian.startup;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import base.task.executor.SpForkJoinPool;
import base.task.executor.ThreadPoolExecutorHelper;
import base.util.MiscUtil;

public abstract class ServerStartup {

	private static Logger log = Logger.getLogger(ServerStartup.class);

	private static ApplicationContext applicationContext;

	public static final Object startup = new Object();

	public final static void init() {
		applicationContext = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		((AbstractApplicationContext) applicationContext)
				.registerShutdownHook();

		notifyAll2WakeUp();
	}

	private static void notifyAll2WakeUp() {
		synchronized (startup) {
			startup.notifyAll();
		}
	}

	public final static void close() {
		((AbstractApplicationContext) applicationContext).close();

		while (((AbstractApplicationContext) applicationContext).isActive()) {
			log.info("applicationContext is not shutdown complete, wait...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		Future<?> fs = SpForkJoinPool.shutdownGracefully();
		Future<?> ft = ThreadPoolExecutorHelper.shutdownGracefully();
		while (!fs.isDone() || !ft.isDone()) {
			log.info("ExecutorService is not shutdown complete, wait...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		log.info("ExecutorService stop operation complete...!!");

		log.info("applicationContext closed.");
		System.exit(0);
	}

	public final static Object getBean(String name) {
		return applicationContext.getBean(name);
	}

	public final static <T> T getBean(String name, Class<T> clazz) {
		return applicationContext.getBean(name, clazz);
	}
}
