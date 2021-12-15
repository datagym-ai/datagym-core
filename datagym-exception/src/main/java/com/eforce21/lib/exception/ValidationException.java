package com.eforce21.lib.exception;

/**
 * Thrown when user input (request data) isn't valid either in style or in
 * content. Like a classic HTTP 400.
 *
 * @author thomas.kuhlins
 */
public class ValidationException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ValidationException() {
		this(null);
	}

	public ValidationException(Throwable cause) {
		super("Validation failed.", cause);
	}

	public ValidationException addDetail(Detail detail) {
		details.add(detail);
		return this;
	}

	@Override
	public String getKey() {
		return super.getKey() + "_validation";
	}

	@Override
	public int getHttpCode() {
		return 400;
	}

	/**
	 * Throw itself if there's at least one detail.
	 */
	public void throwOnDetails() throws ValidationException {
		if (hasDetails()) {
			throw this;
		}
	}

}
