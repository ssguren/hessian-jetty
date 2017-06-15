package base.servicecenter.hessian.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import base.servicecenter.hessian.service.BaseService;
import base.servicecenter.hessian.service.ISpTimeoutService;

@Service
public class SpTimeoutService extends BaseService implements ISpTimeoutService {

	private int defaultQueryTimeout = 20000;

	private int defaultApplyTimeout = 20000;

	private Map<String, Integer> spIdQueryTimeout = new HashMap<String, Integer>();

	private Map<String, Integer> spIdApplyTimeout = new HashMap<String, Integer>();

	@Override
	public int getQueryTimeout(String spId) {
		if (spIdQueryTimeout.containsKey(spId))
			return spIdQueryTimeout.get(spId);

		return defaultQueryTimeout;
	}

	@Override
	public int getApplyOrderTimeout(String spId) {
		if (spIdApplyTimeout.containsKey(spId))
			return spIdApplyTimeout.get(spId);

		return defaultApplyTimeout;
	}

	@Override
	public int getWorkingTimeout(String spId) {
		return Math.max(getQueryTimeout(spId), getApplyOrderTimeout(spId));
	}

	public void setDefaultQueryTimeout(int defaultQueryTimeout) {
		this.defaultQueryTimeout = defaultQueryTimeout;
	}

	public void setDefaultApplyTimeout(int defaultApplyTimeout) {
		this.defaultApplyTimeout = defaultApplyTimeout;
	}

	public void setSpIdQueryTimeout(Map<String, Integer> spIdQueryTimeout) {
		this.spIdQueryTimeout = spIdQueryTimeout;
	}

	public void setSpIdApplyTimeout(Map<String, Integer> spIdApplyTimeout) {
		this.spIdApplyTimeout = spIdApplyTimeout;
	}
}
