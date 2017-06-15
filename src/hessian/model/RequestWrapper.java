package hessian.model;

import java.io.Serializable;

/**
 * 
 * @author
 * 
 */
public class RequestWrapper implements Serializable {

	private static final long serialVersionUID = -6017954186180888313L;

	private String user = null;

	private String password = null;

	private String spId = null;

	private String clientIP = null;

	private String serviceName;

	private String methodName;

	private Object[] args = null;

	private Class<?>[] argsTypes = null;

	public RequestWrapper(String user, String password, String spId,
			String clientIP, String serviceName) {
		this.user = user;
		this.password = password;
		this.spId = spId;
		this.clientIP = clientIP;
		this.serviceName = serviceName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Class<?>[] getArgsTypes() {
		return argsTypes;
	}

	public void setArgsTypes(Class<?>[] argsTypes) {
		this.argsTypes = argsTypes;
	}
}
