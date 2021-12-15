package com.eforce21.lib.exception;

/**
 * Exception thrown whenever an item cannot be found. Since this exception might
 * be thrown for either an obvious primary entity (e.g. getUserById()) or
 * somewhere deeper within an operation (e.g. in permitUserForObjects(userid,
 * objects) where one object doesn't exist) you need to specified some details.
 * By example: Setting WHAT=User, CRITERIA=id, VALUE=5 will lead to this very useful
 * String: "User with id 5 not found.".
 *
 * @author thomas.kuhlins
 */
public final class NotFoundException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String what, String criteria, String value) {
		this(what, criteria, value, null);
	}

	public NotFoundException(String what, String criteria, String value, Throwable cause) {
		super("Item " + what + " with " + criteria + " " + value + " not found.", cause, what, criteria, value);
	}

	@Override
	public String getKey() {
		return super.getKey() + "_notfound";
	}

	@Override
	public int getHttpCode() {
		return 404;
	}

}
