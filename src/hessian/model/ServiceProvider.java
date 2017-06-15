package hessian.model;

import java.io.Serializable;

import com.esotericsoftware.reflectasm.MethodAccess;

public class ServiceProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	private Class<?> processorClass;

	private String beanName;

	private MethodAccess methodAccess;

	public ServiceProvider() {
	}

	public ServiceProvider(Class<?> processorClass, String beanName) {
		this.processorClass = processorClass;
		this.beanName = beanName;
		this.methodAccess = MethodAccess.get(processorClass);
	}

	public Class<?> getProcessorClass() {
		return processorClass;
	}

	public void setProcessorClass(Class<?> processorClass) {
		this.processorClass = processorClass;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public MethodAccess getMethodAccess() {
		return methodAccess;
	}
}