package base.servicecenter.hessian.service;

public interface ISpTimeoutService {

	public int getQueryTimeout(String spId);

	public int getApplyOrderTimeout(String spId);

	public int getWorkingTimeout(String spId);
}
