package com.eforce21.lib.exception;

/**
 * Thrown whenever something goes terribly wrong and the client/user shouldn't
 * know the reason. Exceptions of this type (and subclassed) must be logged on
 * system boundaries (including stack) and propagated to the client as general
 * "System error. Contact admin." while hiding details.
 * <p>
 * Use this exception to wrap technical problems that cannot be handeled within
 * the service layer like IOException, connection- and transaction- problems or
 * databases and backends not available.
 *
 * @author thomas.kuhlins
 */
public class SystemException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public SystemException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public String getKey() {
        return super.getKey() + "_system";
    }
}
