package ai.datagym.application.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link BeanPostProcessor} intercepting whenever spring is about to initialize
 * a {@link DataSource}. Checks database availability in a blocking loop by
 * trying to connect and perform a simple validation query till the check
 * succeeds or maxwait is reached. In later case an exception is thrown.
 * <p>
 * Build for fancy modern microservice-enabled cloud-native environments where
 * nobody cares about startup order and service dependency management.
 *
 * @author t
 */
public class DbCheckBlocker implements BeanPostProcessor {

	private static final Logger L = LoggerFactory.getLogger(DbCheckBlocker.class);

	/**
	 * Wait and repeat each X seconds.
	 */
	private long interval = 10;

	/**
	 * Cancel startup with exception after X seconds of trying. Large default of 600
	 * seconds because some databases requires much time to initialize or start
	 * especially in clustered/replicated/loadbalanced setups.
	 */
	private long maxwait = 600;

	/**
	 * Datasources to wait for with bean name as key.
	 */
	private Map<String, DataSourceProperties> datasources = new HashMap<>();

	public void setDatasources(Map<String, DataSourceProperties> datasources) {
		this.datasources = datasources;
	}

	public void addDataSource(String dataSourceName, DataSourceProperties dataSourceProperties) {
		this.datasources.put(dataSourceName, dataSourceProperties);
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setMaxwait(long maxwait) {
		this.maxwait = maxwait;
	}


	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		if (bean instanceof DataSource) {
			DataSourceProperties dsp = datasources.get(beanName);
			if (dsp == null) {
				L.info("DataSource about to initialize -> Name: {}, Type: {}. Not configured to check, so we keep goin on.",
						beanName, bean.getClass().getSimpleName());
				return bean;
			}

			L.info("DataSource about to initialize -> Name: {}, Type: {}. Checking and blocking till it's available.",
					beanName, bean.getClass().getSimpleName());

			long checkTill = System.currentTimeMillis() + (maxwait * 1000);

			Exception ex = null;

			do {
				try (Connection c = DriverManager.getConnection(dsp.getUrl(), dsp.getUsername(), dsp.getPassword())) {
					c.createStatement().executeQuery("select 1").next();
					ex = null;
					L.info("Database check succeeded. Going on...");
				} catch (SQLException e) {
					L.warn("Database check failed. Trying again in {}s. Reason: {} -> {}.", interval,
							e.getClass().getSimpleName(), e.getMessage());
					ex = e;
				}

				try {
					Thread.sleep(interval * 1000);
				} catch (InterruptedException e) {
					// Don't care
				}
			} while (ex != null && System.currentTimeMillis() < checkTill);

			if (ex != null) {
				throw new BeanInitializationException(
						"DataSource still not available after " + maxwait + "s. Canceling startup.", ex);
			}
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		return bean;
	}

}
