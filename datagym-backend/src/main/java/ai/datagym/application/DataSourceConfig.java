package ai.datagym.application;

import ai.datagym.application.utils.DbCheckBlocker;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackageClasses = {DataSourceConfig.class},
        basePackages = {"com.eforce21.lib.bin.file"},
        entityManagerFactoryRef = "defaultEntityManagerFactory", transactionManagerRef = "defaultTransactionManager")
public class DataSourceConfig {
    public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "defaultPersistenceUnit";

    @Value(value = "${datagym.deactivate-dbCheckBlocker}")
    private boolean deactivateDbCheckBlocker;

    @Bean("customDefaultDataSourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties defaultDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.configuration")
    public HikariDataSource defaultDataSource() {
        return defaultDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean defaultEntityManagerFactory(final EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(defaultDataSource())
                .packages(DataSourceConfig.class, com.eforce21.lib.bin.file.entity.BinFileEntity.class)
                .persistenceUnit(DEFAULT_PERSISTENCE_UNIT_NAME)
                .build();
    }

    @Bean
    @Primary
    public JpaTransactionManager defaultTransactionManager(@Qualifier("defaultEntityManagerFactory") final EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Bean
    public DbCheckBlocker defaultDbCheckBlocker(@Qualifier("customDefaultDataSourceProperties") DataSourceProperties dataSourceProperties) {
        DbCheckBlocker result = new DbCheckBlocker();
        if(!deactivateDbCheckBlocker){
            result.addDataSource("defaultDataSource", dataSourceProperties);
        }
        return result;
    }
}
