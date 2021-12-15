package com.eforce21.lib.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all exceptions thrown out of the service layer. Defines
 * requirements for a uniform exception handling within services and at system
 * boundaries. Basically there's an i18n-key allowing the client to translate to
 * own messages filled with optional parameters varying by concrete
 * implementation. Note that the message string is only used for debug purpose
 * and never shown to the client.
 *
 * @author thomas.kuhlins
 */
public abstract class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Variable amount of parameters to be substituted in translated exception
     * messages. Subclasses need to make sure required parameters are set (via ctor,
     * validation, ...).
     */
    protected List<Object> params = new ArrayList<>();

    /**
     * Variable amount of details especially meant for form field validation
     * whenever you want to wrap multiple messages in one exception.
     */
    protected List<Detail> details = new ArrayList<>();


    public ServiceException(String msg, Throwable cause, Object... params) {
        super(msg, cause);
        this.params.addAll(Arrays.asList(params));
    }

    /**
     * I18n-key for exception translation. Be aware that different technologies
     * (Java properties, Json, ...) will be used to map these keys, so don't use
     * characters that might cause any problem. Recommendation is to concatenate the
     * parent key via "_" in subclasses.
     *
     * @return
     */
    public String getKey() {
        return "ex";
    }

    /**
     * HTTP status code that should be send in case you transfer this exception over HTTP.
     * No clean separation of (web-)concerns, but makes subclassing and generic error handlers so much easier.
     *
     * @return
     */
    public int getHttpCode() {
        return 500;
    }

    public List<Object> getParams() {
        return params;
    }

    public List<Detail> getDetails() {
        return details;
    }

    /**
     * Is there at least one detail?
     *
     * @return
     */
    public boolean hasDetails() {
        return details.size() > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [key=" + getKey() + ", msg=" + getMessage() + ", params=" + params + ", details=" + details + "]";
    }


}
