package base.servicecenter.hessian.service.impl;

import org.springframework.stereotype.Service;

import base.servicecenter.hessian.service.HessianBaseService;
import base.servicecenter.hessian.service.IHessianTestService;

@Service
public class HessianTestService extends HessianBaseService implements
		IHessianTestService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String testJetty() {
		log.info("testJetty coming ...");
		return "success";
	}

}
