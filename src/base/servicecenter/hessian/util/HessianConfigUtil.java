package base.servicecenter.hessian.util;

public class HessianConfigUtil {

	protected static String baseUrl = "https://localhost:8080/hessianDemo/hessian/";

	protected static String hessianUser = "client1";

	protected static String hessianPassword = "client1";

	protected static long connectionTimeout = 5000;

	protected static long readTimeout = 180000;

	public void setBaseUrl(String baseUrl) {
		HessianConfigUtil.baseUrl = baseUrl;
	}

	public void setHessianUser(String hessianUser) {
		HessianConfigUtil.hessianUser = hessianUser;
	}

	public void setHessianPassword(String hessianPassword) {
		HessianConfigUtil.hessianPassword = hessianPassword;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		HessianConfigUtil.connectionTimeout = connectionTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		HessianConfigUtil.readTimeout = readTimeout;
	}
}
