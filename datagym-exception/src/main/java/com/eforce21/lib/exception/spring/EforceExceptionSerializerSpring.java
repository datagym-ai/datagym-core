package com.eforce21.lib.exception.spring;

import com.eforce21.lib.exception.ServiceException;
import com.eforce21.lib.exception.base.EforceExceptionConverter;
import com.eforce21.lib.exception.base.EforceExceptionSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;
import java.util.Locale;

/**
 * Spring-enabled {@link EforceExceptionSerializer} with translation backed by
 * springs MessageSource mechanism.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionSerializerSpring extends EforceExceptionSerializer {

    /**
     * If there's a global MessageSource defined by surrounding application: Use it
     * as first approach and give the application the opportunity to overwrite
     * messages.
     */
    @Autowired(required = false)
    private MessageSource messageSource;
    /**
     * Internal MessageSource with default messages as fallback. Intentionally not
     * registered in ctx to not overwrite a global bean.
     */
    private ResourceBundleMessageSource internalMessageSource;

    public EforceExceptionSerializerSpring() {
        internalMessageSource = new ResourceBundleMessageSource();
        internalMessageSource.setBasename("com/eforce21/lib/exception/messages");
        internalMessageSource.setDefaultEncoding("UTF-8");
    }

    /**
     * Override for autowiring.
     */
    @Autowired(required = false)
    @Override
    public void setConverters(List<EforceExceptionConverter> converters) {
        super.setConverters(converters);
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Translate exception key and params to human readable message string.
     *
     * @param ex Exception to translate.
     * @return Human readable message string. Never null.
     */
    @Override
    protected String translate(ServiceException ex) {
        String result = null;

        // Resolve via global message source
        if (messageSource != null) {
            result = messageSource.getMessage(ex.getKey(), ex.getParams().toArray(), null, Locale.ROOT);
        }

        // Fallback to local message source
        if (result == null) {
            result = internalMessageSource.getMessage(ex.getKey(), ex.getParams().toArray(),
                    "No msg available for key: " + ex.getKey(), Locale.ROOT);
        }

        return result;
    }

}
