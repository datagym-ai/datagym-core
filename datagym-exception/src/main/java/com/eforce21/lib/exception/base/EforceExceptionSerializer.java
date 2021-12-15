package com.eforce21.lib.exception.base;

import com.eforce21.lib.exception.Detail;
import com.eforce21.lib.exception.ServiceException;
import com.eforce21.lib.exception.SystemException;
import com.eforce21.lib.exception.to.DetailTO;
import com.eforce21.lib.exception.to.ErrorTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Serialize {@link Throwable}s to serializable {@link ErrorTO}s so they can be
 * transfered to the client or used as model in error views. Overwrite to
 * implement a message translation i.e. based on java properties, spring
 * messagesources, ...
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionSerializer {

    private List<EforceExceptionConverter> converters = new ArrayList<>();

    public void setConverters(List<EforceExceptionConverter> converters) {
        this.converters = converters;
    }

    /**
     * @param t                       Throwable to serialize
     * @param serviceExceptionHandler Optional additional handler for converted
     *                                exception logging.
     * @return
     */
    public ErrorTO serialize(Throwable t, Consumer<ServiceException> serviceExceptionHandler) {
        ServiceException sex = null;

        if (t instanceof ServiceException) {
            sex = (ServiceException) t;
        } else {
            sex = converters.stream().map(c -> c.convert(t)).filter(Objects::nonNull).findFirst().orElse(null);
            if (sex == null) {
                sex = new SystemException(
                        "Unexpected exception occurred. " + t.getClass().getCanonicalName() + ": " + t.getMessage(), t);
            }
        }

        if (serviceExceptionHandler != null) {
            serviceExceptionHandler.accept(sex);
        }

        return map(sex);
    }

    /**
     * Map ServiceException to ErrorTO. Includes message translation. This is what
     * you wanna sent to your client via (network)interface.
     *
     * @param ex ServiceException to map.
     * @return ErrorTO. Never null.
     */
    protected ErrorTO map(ServiceException ex) {
        ErrorTO result = new ErrorTO();
        result.setKey(ex.getKey());
        result.setMsg(translate(ex));
        result.setCode(ex.getHttpCode());

        for (Object o : ex.getParams()) {
            result.addParam(Objects.toString(o));
        }

        for (Detail d : ex.getDetails()) {
            result.addDetail(map(d));
        }

        return result;
    }

    /**
     * Map Detail to DetailTO.
     *
     * @param source
     * @return DetailTO. Never null.
     */
    protected DetailTO map(Detail source) {
        DetailTO result = new DetailTO();
        result.setName(source.getName());
        result.setKey(source.getKey());

        for (Object o : source.getParams()) {
            result.addParam(Objects.toString(o));
        }

        return result;
    }

    /**
     * Translate message key and substitute params to get a human readable message
     * string. Override to use your translation mechanism. This default impl always
     * returns null;
     *
     * @param ex
     * @return Human readable exception message string.
     */
    protected String translate(ServiceException ex) {
        return null;
    }

}
