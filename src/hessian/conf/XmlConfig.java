package hessian.conf;

import hessian.model.Application;
import hessian.model.Service;
import hessian.model.ServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import base.util.MiscUtil;
import base.util.StringUtil;

public class XmlConfig {

	private final static Logger logger = Logger.getLogger(XmlConfig.class);

	private String configFile = null;
	private Document document;
	private Element root = null;

	public XmlConfig(String configFile) {
		this.configFile = configFile;
		root = getRoot();
	}

	/**
	 * parse application
	 */
	@SuppressWarnings("unchecked")
	public List<Application> parseApplication() {
		List<Application> appList = new LinkedList<Application>();
		Element aroot = getRoot();
		Node root = aroot.selectSingleNode("//applications");
		List<Element> elementList = root.selectNodes("application");
		for (Element e : elementList) {
			String user = e.attributeValue("user");
			String password = e.attributeValue("password");
			Application app = new Application();
			app.setUser(user);
			app.setPassword(password);
			appList.add(app);
		}

		return appList;
	}

	/**
	 * analyse service configure and return a list,the list is a LocalService
	 * and each localService corresponding a service
	 */
	@SuppressWarnings("unchecked")
	public List<Service> parseService() {
		List<Service> slist = new ArrayList<Service>();

		Node serviceRoot = root.selectSingleNode("//services");
		List<Element> serviceList = serviceRoot.selectNodes("//service");

		int i = 0;
		for (Element serviceNode : serviceList) {
			String name = serviceNode.attributeValue("name");// service name
			String interfaceStr = serviceNode.attributeValue("interface");
			String overloadStr = serviceNode.attributeValue("overload");

			if (StringUtil.isEmptyStr(name)) {
				logger.warn("you have a wrong config in " + configFile
						+ ": a service's name is empty.");
				continue;
			}
			if (StringUtil.isEmptyStr(interfaceStr)) {
				logger.warn("you have a wrong config in " + configFile
						+ ": service " + name
						+ "  has an empty interface configure.");
				continue;
			}
			Class<?> type = null;
			try {
				type = Class.forName(interfaceStr);
			} catch (ClassNotFoundException e) {
				logger.warn("you have a wrong config in " + configFile
						+ ": can't find service Interface: " + interfaceStr);
				continue;
			}

			Service service = new Service(i, name);
			service.setTypeClass(type);

			if (!StringUtil.isEmptyStr(overloadStr)
					&& "true".equals(overloadStr.trim())) {
				service.setOverload(true);
			}

			List<Element> versionList = serviceNode.selectNodes("provider");
			for (Element element : versionList) {
				String processor = element.attributeValue("class");
				String beanName = element.attributeValue("beanname");

				Class<?> providerClass = null;
				try {
					providerClass = Class.forName(processor);
				} catch (ClassNotFoundException e) {
					logger.warn("you have a wrong config in " + configFile
							+ ": can't find service provider: " + processor);
					continue;
				}

				ServiceProvider provider = new ServiceProvider(providerClass,
						beanName);

				service.setProvider(provider);
			}

			if (service.getProvider() != null) {
				slist.add(service);
				logger.info("init Service "
						+ service.getName()
						+ " for interface "
						+ service.getTypeClass().getName()
						+ " with provider="
						+ service.getProvider().getProcessorClass()
								.getSimpleName());
			}

			i++;
		}

		return slist;
	}

	@SuppressWarnings("unchecked")
	private Element getRoot() {
		try {
			Document doc = getDocument();
			List<Element> list = doc.selectNodes("//deployment");
			if (list.size() > 0) {
				Element aroot = list.get(0);
				return aroot;
			}
		} catch (DocumentException e) {
			logger.error(MiscUtil.traceInfo(e));
		} catch (IOException e1) {
			logger.error(MiscUtil.traceInfo(e1));
		}

		return null;
	}

	private Document getDocument() throws DocumentException, IOException {
		InputStream is = getFileStream();
		try {
			if (document == null) {
				SAXReader reader = new SAXReader();
				reader.setValidation(false);
				if (is == null) {
					throw new RuntimeException(
							"we can not find the service config file:"
									+ configFile);
				}
				document = reader.read(is);
			}
		} catch (Exception e) {
			logger.error(MiscUtil.traceInfo(e));
			throw new RuntimeException("get xml Document failed.");
		} finally {
			is.close();
		}

		return document;
	}

	private InputStream getFileStream() {
		return getFileStream(configFile);
	}

	private InputStream getFileStream(String file) {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(file);
		return is;
	}
}