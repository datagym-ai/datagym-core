package com.eforce21.lib.exception.springsecurity;

import com.eforce21.lib.exception.spring.EforceExceptionHandlerSpring;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implementation of spring security {@link EforceExceptionHandlerSpringSecurityAuthenticationEntryPoint} catching
 * {@link AuthenticationException}s. Don't know why these spring security guys
 * don't use common spring exception handling and instead making their own
 * shit.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionHandlerSpringSecurityAuthenticationEntryPoint extends EforceExceptionHandlerSpring implements AuthenticationEntryPoint {

    private boolean sendWwwAuthenticate = false;

    private String sendRedirectLocation = null;

    /**
     * Configure EntryPoint to sent WWW-Authenticate header on unauthorized.
     * Defaults to false.
     *
     * @param sendWwwAuthenticate true to send WWW-Authenticate header, false to not send.
     * @return
     */
    public EforceExceptionHandlerSpringSecurityAuthenticationEntryPoint configureWwwAuthenticate(boolean sendWwwAuthenticate) {
        this.sendWwwAuthenticate = sendWwwAuthenticate;
        return this;
    }

    /**
     * Configure EntryPoint to sent Redirect instead of Unauthorized. Therefore a
     * location is required. Defaults to null.
     *
     * @param redirectLocation Location to redirect to.
     * @return
     */
    public EforceExceptionHandlerSpringSecurityAuthenticationEntryPoint configureRedirectLocation(String redirectLocation) {
        this.sendRedirectLocation = redirectLocation;
        return this;
    }

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException, ServletException {
        if (sendWwwAuthenticate) {
            res.setHeader("WWW-Authenticate", "Basic realm=\"realm\"");
        }

        if (sendRedirectLocation == null || sendRedirectLocation.isEmpty()) {
            handleException(ex, req, res);
        } else {
            res.sendRedirect(sendRedirectLocation);
        }
    }

}
