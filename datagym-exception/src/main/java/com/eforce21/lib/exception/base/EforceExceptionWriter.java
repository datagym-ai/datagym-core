package com.eforce21.lib.exception.base;

import com.eforce21.lib.exception.to.ErrorTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Write {@link ErrorTO}s to the {@link HttpServletResponse}.
 *
 * @author thomas.kuhlins
 */
public interface EforceExceptionWriter {

    void write(ErrorTO error, HttpServletRequest req, HttpServletResponse res) throws IOException;

    int getOrder();

    String getProduces();

}
