package com.eforce21.lib.exception.springsecurity;

import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.ServiceException;
import com.eforce21.lib.exception.UnauthorizedException;
import com.eforce21.lib.exception.base.EforceExceptionConverter;
import org.springframework.security.access.AccessDeniedException;

import javax.security.sasl.AuthenticationException;

/**
 * Convert spring security {@link AccessDeniedException} to own {@link ForbiddenException}
 * and {@link AuthenticationException} to own {@link UnauthorizedException}.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionConverterSpringSecurity implements EforceExceptionConverter {

	@Override
	public ServiceException convert(Throwable t) {
		if (t instanceof AccessDeniedException) {
			return convertAccessDeniedException((AccessDeniedException) t);
		} else if (t instanceof AuthenticationException) {
			return convertAuthenticationException((AuthenticationException) t);
		}
		return null;
	}

	protected ServiceException convertAccessDeniedException(AccessDeniedException e) {
		return new ForbiddenException("Access denied", e);
	}

	protected ServiceException convertAuthenticationException(AuthenticationException e) {
		return new UnauthorizedException(e.getMessage(), e);
	}


}
