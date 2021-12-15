package com.eforce21.lib.exception.spring;

import com.eforce21.lib.exception.Detail;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.ServiceException;
import com.eforce21.lib.exception.ValidationException;
import com.eforce21.lib.exception.base.EforceExceptionConverter;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

/**
 * Convert Spring web exceptions originally handled by
 * {@link ResponseEntityExceptionHandler}. Every exception is handled in it's
 * own method for easy subclassing if you want other or more detailed
 * exceptions.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionConverterSpringWeb implements EforceExceptionConverter {

    @Override
    public ServiceException convert(Throwable t) {
        if (t instanceof NoHandlerFoundException) {
            return convertNoHandlerFoundException((NoHandlerFoundException) t);
        } else if (t instanceof MethodArgumentNotValidException) {
            return convertMethodArgumentNotValidException((MethodArgumentNotValidException) t);
        } else if (t instanceof TypeMismatchException) {
            return convertTypeMismatchException((TypeMismatchException) t);
        } else if (t instanceof MissingServletRequestParameterException) {
            return convertMissingServletRequestParameterException((MissingServletRequestParameterException) t);
        } else if (t instanceof ServletRequestBindingException) {
            return convertServletRequestBindingException((ServletRequestBindingException) t);
        } else if (t instanceof HttpMessageNotReadableException) {
            return convertHttpMessageNotReadableException((HttpMessageNotReadableException) t);
        } else if (t instanceof MissingServletRequestPartException) {
            return convertMissingServletRequestPartException((MissingServletRequestPartException) t);
        } else if (t instanceof BindException) {
            return convertBindException((BindException) t);
        } else if (t instanceof HttpRequestMethodNotSupportedException) {
            return convertHttpRequestMethodNotSupportedException((HttpRequestMethodNotSupportedException) t);
        } else if (t instanceof HttpMediaTypeNotSupportedException) {
            return convertHttpMediaTypeNotSupportedException((HttpMediaTypeNotSupportedException) t);
        } else if (t instanceof HttpMediaTypeNotAcceptableException) {
            return convertHttpMediaTypeNotAcceptableException((HttpMediaTypeNotAcceptableException) t);
        } else if (t instanceof MissingPathVariableException) {
            return convertMissingPathVariableException((MissingPathVariableException) t);
        } else if (t instanceof ConversionNotSupportedException) {
            return convertConversionNotSupportedException((ConversionNotSupportedException) t);
        } else if (t instanceof HttpMessageNotWritableException) {
            return convertHttpMessageNotWritableException((HttpMessageNotWritableException) t);
        } else if (t instanceof AsyncRequestTimeoutException) {
            return convertAsyncRequestTimeoutException((AsyncRequestTimeoutException) t);
        }

        return null;
    }

    protected ServiceException convertNoHandlerFoundException(NoHandlerFoundException e) {
        return new NotFoundException("handler", "url", e.getRequestURL());
    }

    /**
     * Convert that strange Spring/JSR-Bean-Validation format to match our nice
     * concept.
     *
     * @param e
     * @return
     */
    protected ServiceException convertMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ValidationException ve = new ValidationException();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            Object[] arguments = fe.getArguments();
            Object[] params = Arrays.copyOfRange(arguments, 1, arguments.length);
            ve.addDetail(new Detail(fe.getField(), fe.getCode(), params));
        }
        return ve;
    }

    protected ServiceException convertTypeMismatchException(TypeMismatchException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertBindException(BindException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertMissingServletRequestPartException(MissingServletRequestPartException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertServletRequestBindingException(ServletRequestBindingException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        return new ValidationException(e);
    }

    protected ServiceException convertHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        // Original uses 405 Method Not Allowed, but we don't care for simplicity.
        return new ValidationException(e);
    }

    protected ServiceException convertHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        // Original uses 415 Unsupported Media Type, but we don't care for simplicity.
        return new ValidationException(e);
    }

    protected ServiceException convertHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        // Original uses 406 Not Acceptable, but we don't care for simplicity.
        return new ValidationException(e);
    }

    protected ServiceException convertMissingPathVariableException(MissingPathVariableException e) {
        // Original uses 500 Internal Server Error, so we let it pass to result in
        // SystemException(500).
        return null;
    }

    protected ServiceException convertConversionNotSupportedException(ConversionNotSupportedException e) {
        // Original uses 500 Internal Server Error, so we let it pass to result in
        // SystemException(500).
        return null;
    }

    protected ServiceException convertHttpMessageNotWritableException(HttpMessageNotWritableException e) {
        // Original uses 500 Internal Server Error, so we let it pass to result in
        // SystemException(500).
        return null;
    }

    protected ServiceException convertAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        // Original uses 503 Service Unavailable, but we let it pass to result in
        // SystemException(500).
        return null;
    }

}
