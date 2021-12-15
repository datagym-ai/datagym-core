package com.eforce21.lib.exception.springsecurity;

import com.eforce21.lib.exception.spring.EforceExceptionHandlerSpring;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implementation of spring security {@link AccessDeniedHandler} catching
 * {@link AccessDeniedException}s. Don't know why these spring security guys
 * don't use common spring exception handling and instead making their own
 * shit.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionHandlerSpringSecurityAccessDenied extends EforceExceptionHandlerSpring implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException, ServletException {
        handleException(ex, req, res);
    }

}
