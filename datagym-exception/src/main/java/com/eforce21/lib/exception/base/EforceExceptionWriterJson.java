package com.eforce21.lib.exception.base;

import com.eforce21.lib.exception.to.ErrorTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSON {@link EforceExceptionWriter} using Jackson. Produces application/json
 * with order 0.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionWriterJson extends EforceExceptionWriterAbstract {

    /**
     * We use our own plain ObjectMapper. We don't want to depend on Springs
     * autowire and don't want to use another (wrongly) configured ObjectMapper.
     */
    private ObjectMapper om = new ObjectMapper();

    public EforceExceptionWriterJson() {
        this.produces = "application/json";
        this.order = 0;
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void write(ErrorTO error, HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO We better should check for committed responses here, but where to log?
        res.setStatus(error.getCode());
        res.setContentType("application/json; charset=utf-8");
        om.writeValue(res.getOutputStream(), error);
    }

}
