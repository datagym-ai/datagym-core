package com.eforce21.lib.exception;

/**
 * Thrown when you need to be authorized for the operation you're about to
 * perform but you're not. There might be different causes like: No auth,
 * invalid auth, expired auth, and more .... If you're an UI, think about
 * promting your user for credentials or redirect to an login form. Like HTTP
 * 401.
 *
 * @author thomas.kuhlins
 */
public class UnauthorizedException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Unauthorized.", null);
    }

    public UnauthorizedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public String getKey() {
        return super.getKey() + "_unauthorized";
    }

    @Override
    public int getHttpCode() {
        return 401;
    }

}
