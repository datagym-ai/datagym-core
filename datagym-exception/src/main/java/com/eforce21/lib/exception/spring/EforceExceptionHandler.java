package com.eforce21.lib.exception.spring;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Global exception handler for spring based applications catching everything
 * thrown out of the controllers using {@link ControllerAdvice} +
 * {@link ExceptionHandler} mechanisms.
 *
 * @author thomas.kuhlins
 */
@ControllerAdvice
public class EforceExceptionHandler extends EforceExceptionHandlerSpring {

	@ExceptionHandler(value = {Exception.class})
	protected void handleIt(Exception ex, HttpServletRequest req, HttpServletResponse res) throws IOException {
		handleException(ex, req, res);
	}

}
