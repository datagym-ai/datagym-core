package com.eforce21.lib.exception;

/**
 * Thrown when you're about to perform an operation or access data you're not
 * permitted. Like HTTP 403.
 *
 * @author thomas.kuhlins
 */
public class ForbiddenException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super("Forbidden.", null);
	}

	public ForbiddenException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	public String getKey() {
		return super.getKey() + "_forbidden";
	}

	@Override
	public int getHttpCode() {
		return 403;
	}

}
