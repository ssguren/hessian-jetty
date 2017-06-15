package hessian.plugin;

import hessian.conf.HettyConfig;
import hessian.conf.XmlConfig;
import hessian.model.Application;
import hessian.model.Service;
import hessian.server.hetty.processor.SecurityProcessor;
import hessian.server.hetty.processor.ServiceProcessor;

import java.util.List;

public class XmlConfigPlugin implements IPlugin {

	@Override
	public boolean start() {
		String configFile = HettyConfig.getInstance().getPropertiesFile();
		String[] fileArr = configFile.split(",");

		for (String file : fileArr) {
			XmlConfig config = new XmlConfig(file);

			List<Application> appList = config.parseApplication();
			for (Application app : appList) {
				SecurityProcessor.addToApplicationMap(app);
			}

			List<Service> serviceList = config.parseService();
			for (Service service : serviceList) {
				ServiceProcessor.addToServiceMap(service);
			}
		}

		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}
}
