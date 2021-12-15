package com.eforce21.lib.exception.spring;

import com.eforce21.lib.exception.ServiceException;
import com.eforce21.lib.exception.SystemException;
import com.eforce21.lib.exception.base.EforceExceptionWriter;
import com.eforce21.lib.exception.base.EforceExceptionWriterJson;
import com.eforce21.lib.exception.to.ErrorTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for spring exception handler implementations autowiring
 * required/configured bean dependencies and using spring logging.
 *
 * @author thomas.kuhlins
 */
public abstract class EforceExceptionHandlerSpring {

	protected final Log l = LogFactory.getLog(getClass());
	protected Consumer<ServiceException> logConsumer = new Consumer<ServiceException>() {
		public void accept(ServiceException sex) {
			// SystemExceptions are always logged since the cause will be hidden.
			if (sex instanceof SystemException) {
				l.error(sex.getMessage(), sex);
			}
			// Everything else is passed to the client, so only log in debug.
			else if (l.isDebugEnabled()) {
				l.debug("Passing exception to client. " + sex.getClass().getCanonicalName() + ": " + sex.getMessage(),
						sex);
			}
		}

		;
	};
	@Autowired
	private EforceExceptionSerializerSpring serializer;
	@Autowired(required = false)
	private List<EforceExceptionWriter> writers = new ArrayList<EforceExceptionWriter>();

	@PostConstruct
	public void initAbstract() {
		if (writers.isEmpty()) {
			writers.add(new EforceExceptionWriterJson());
		}
		writers.sort(Comparator.comparing(EforceExceptionWriter::getOrder));
	}

	/**
	 * Handle all kinds of exceptions in the common eforce way. This should be
	 * called at system boundaries to deliver clean and uniform exception
	 * information to the client. Unknown and unexpected exceptions will be
	 * converted to own {@link SystemException}s. Depending on the exception type,
	 * they get logged and critical information is hidden.
	 *
	 * @param ex
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	protected void handleException(Exception ex, HttpServletRequest req, HttpServletResponse res) throws IOException {
		ErrorTO error = serializer.serialize(ex, logConsumer);

		EforceExceptionWriter writer = null;

		for (MediaType mt : MediaType.parseMediaTypes(req.getHeader("Accept"))) {
			writer = writers.stream().filter(w -> mt.includes(MediaType.parseMediaType(w.getProduces()))).findFirst()
					.orElse(null);
			if (writer != null) {
				break;
			}
		}
		if (writer == null) {
			writer = writers.get(0);
		}

		writer.write(error, req, res);
	}

}
