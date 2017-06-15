package hessian.server.hetty.processor;

import hessian.model.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceMetaDataProcessor {

	private static Map<String, ServiceMetaData> hessianServiceMetaMap = new HashMap<String, ServiceMetaData>();

	public static void initMetaDataMap() {
		Map<String, Service> serviceMap = ServiceProcessor.getServiceMap();
		Set<String> serviceNames = serviceMap.keySet();
		for (String name : serviceNames) {
			Service service = serviceMap.get(name);
			Class<?> clazz = service.getTypeClass();
			ServiceMetaData smd = new ServiceMetaData(clazz,
					service.isOverload());
			hessianServiceMetaMap.put(name, smd);
		}
	}

	public static ServiceMetaData getServiceMetaData(String sname) {
		return hessianServiceMetaMap.get(sname);
	}
}
