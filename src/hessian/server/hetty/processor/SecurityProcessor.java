package hessian.server.hetty.processor;

import hessian.model.Application;

import java.util.HashMap;
import java.util.Map;

import base.util.StringUtil;

public class SecurityProcessor {

	private static final Map<String, Application> applicationMap = new HashMap<String, Application>();

	public static void addToApplicationMap(Application app) {
		applicationMap.put(app.getUser(), app);
	}

	/**
	 * check permission
	 * 
	 * @param user
	 * @param password
	 */
	public static boolean checkPermission(String user, String password) {
		if (StringUtil.isEmptyStr(user) || StringUtil.isEmptyStr(password)) {
			return false;
		}

		if (applicationMap.containsKey(user)
				&& applicationMap.get(user).getPassword().equals(password)) {
			return true;
		} else {
			return false;
		}
	}
}
