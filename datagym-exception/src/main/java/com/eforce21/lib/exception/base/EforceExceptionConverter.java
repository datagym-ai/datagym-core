package com.eforce21.lib.exception.base;

import com.eforce21.lib.exception.ServiceException;

/**
 * Convert your own or your libraries/frameworks exceptions to eForce
 * exceptions. Converters are typically chained, so be sure to type check and
 * return null on exception types you're not responsible for.
 *
 * @author thomas.kuhlins
 */
public interface EforceExceptionConverter {

	/**
	 * Convert any thrown exception to a {@link ServiceException} to allow proper
	 * handling and display of i18n parameterized messages.
	 *
	 * @param t
	 * @return Converted exception or null if not responsible.
	 */
	ServiceException convert(Throwable t);

}
