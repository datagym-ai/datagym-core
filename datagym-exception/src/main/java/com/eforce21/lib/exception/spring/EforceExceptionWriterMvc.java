package com.eforce21.lib.exception.spring;

import com.eforce21.lib.exception.base.EforceExceptionWriter;
import com.eforce21.lib.exception.base.EforceExceptionWriterAbstract;
import com.eforce21.lib.exception.to.ErrorTO;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring MVC {@link EforceExceptionWriter} using the configured {@link View} to
 * render. The {@link ErrorTO} is passed as "error" within the model. Produces
 * text/html with order 100 but but feel free to change to produce other media
 * types.
 *
 * @author thomas.kuhlins
 */
public class EforceExceptionWriterMvc extends EforceExceptionWriterAbstract {

	private View view;

	public EforceExceptionWriterMvc(View view) {
		this.produces = "text/html";
		this.order = 100;
		this.view = view;
	}

	@Override
	public void write(ErrorTO error, HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setStatus(error.getCode());

		Map<String, Object> model = new HashMap<>();
		model.put("error", error);
		customizeModel(model);

		try {
			view.render(model, req, res);
		} catch (Exception e) {
			throw new IOException("Cannot render exception. View rendering failed. " + e.getMessage(), e);
		}
	}

	protected void customizeModel(Map<String, Object> model) {
		// For extension/override via subclassing.
	}

}
