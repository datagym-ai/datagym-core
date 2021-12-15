package com.eforce21.lib.exception;

/**
 * Exception thrown whenever a create- or update-operation runs against unique
 * constraints. Since this exception might be thrown for either an obvious
 * unique constraint violation or somewhere deeper within an operation you need
 * to specified some details. By example: Setting WHAT=User, CRITERIA=email,
 * VALUE=hans@wurst.de will lead to this very useful String: "User with email
 * hans@wurst.de already exists.".
 *
 * @author thomas.kuhlins
 */
public final class AlreadyExistsException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public AlreadyExistsException(String what, String criteria, String value) {
		this(what, criteria, value, null);
	}

	public AlreadyExistsException(String what, String criteria, String value, Throwable cause) {
		super("Item " + what + " with " + criteria + " " + value + " already exists.", cause, what, criteria, value);
	}

	@Override
	public String getKey() {
		return super.getKey() + "_alreadyexists";
	}

	@Override
	public int getHttpCode() {
		return 412;
	}
}
