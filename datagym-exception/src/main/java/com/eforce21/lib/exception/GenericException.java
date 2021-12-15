package com.eforce21.lib.exception;

/**
 * Generic ServiceException where exception key can be freely specified. Use if
 * you don't want to force params via ctor in a specialized subclass but
 * remember to keep params in sync with your error messages.
 *
 * @author thomas.kuhlins
 */
public final class GenericException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private String key;

    public GenericException(String key, String msg, Throwable cause, Object... params) {
        super(msg == null ? ("GenericException with key: " + key) : msg, cause, params);
        this.key = key;
    }

    @Override
    public String getKey() {
        return super.getKey() + "_gen_" + key;
    }

}
