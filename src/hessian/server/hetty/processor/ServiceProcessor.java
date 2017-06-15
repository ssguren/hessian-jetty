package hessian.server.hetty.processor;

import hessian.model.RequestWrapper;
import hessian.model.Service;
import hessian.model.ServiceProvider;
import hessian.startup.ServerStartup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.esotericsoftware.reflectasm.MethodAccess;

//import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * @author
 */
public class ServiceProcessor {

	private static Logger logger = Logger.getLogger(ServiceProcessor.class);

	private static final Map<String, Service> serviceMap = new HashMap<String, Service>();

	private static boolean devMod = false;

	private static Map<String, Object> serviceBeanMap = new ConcurrentHashMap<String, Object>();

	/**
	 * 1.if service has a non default version,add 2.put service to map
	 * 
	 * @param service
	 */
	public static void addToServiceMap(Service service) {
		serviceMap.put(service.getName(), service);
	}

	/**
	 * check whether service is exits according service name
	 * 
	 * @param serviceName
	 * @return true exits false not exits
	 */
	public static boolean isServiceExits(String serviceName) {
		return serviceMap.containsKey(serviceName);
	}

	/**
	 * according the request to invoke the method and return the invoke result
	 * 1.get serviceName,methodName,user,password,version 2.get version 3.get
	 * provider 4.invoke
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object invokeMethod(RequestWrapper rw) throws Exception {
		Object result = null;
		if (devMod) {
			ServiceReportProcessor.reportBeforeInvoke(rw);
		}

		String serviceName = rw.getServiceName();
		String methodName = rw.getMethodName();

		Service service = serviceMap.get(serviceName);
		if (service == null) {
			throw new RuntimeException("we cannot find service[" + serviceName
					+ "].");
		}

		ServiceProvider serviceProvider = service.getProvider();
		Object processor = getProcessor(serviceProvider);
		Object[] args = rw.getArgs();
		MethodAccess method = serviceProvider.getMethodAccess();
		// int methodIndex = method.getIndex(methodName, rw.getArgsTypes());
		// result = method.invoke(processor, methodIndex, args);
		result = method.invoke(processor, methodName, rw.getArgsTypes(), args);

		return result;
	}

	private static Object getProcessor(ServiceProvider serviceProvider)
			throws InstantiationException, IllegalAccessException {
		String beanName = serviceProvider.getBeanName();
		Object processor = serviceBeanMap.get(beanName);

		if (processor == null) {
			Class<?> processorClass = serviceProvider.getProcessorClass();
			processor = ServerStartup.getBean(serviceProvider.getBeanName(),
					processorClass);
			if (processor == null) {
				if (devMod) {
					logger.warn("can't get bean="
							+ serviceProvider.getBeanName()
							+ " from app context, use newInstance() instead.");
				}
				processor = processorClass.newInstance();
			}

			if (processor != null) {
				serviceBeanMap.put(beanName, processor);
				logger.info("add bean=" + beanName + " with instance="
						+ processor + " to serviceBeanMap.");
			}
		}

		return processor;
	}

	public static Map<String, Service> getServiceMap() {
		return Collections.unmodifiableMap(serviceMap);
	}

	public static void setDevMod(boolean devMod) {
		ServiceProcessor.devMod = devMod;
	}
}
