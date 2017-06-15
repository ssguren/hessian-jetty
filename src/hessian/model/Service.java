package hessian.model;

import java.io.Serializable;

public class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Class<?> typeClass;

	protected Integer id;

	protected String name;

	private ServiceProvider provider = null;

	private boolean overload = false;

	public Service() {
	}

	public Service(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Class<?> getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(Class<?> typeClass) {
		this.typeClass = typeClass;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceProvider getProvider() {
		return provider;
	}

	public void setProvider(ServiceProvider provider) {
		this.provider = provider;
	}

	public boolean isOverload() {
		return overload;
	}

	public void setOverload(boolean overload) {
		this.overload = overload;
	}
}