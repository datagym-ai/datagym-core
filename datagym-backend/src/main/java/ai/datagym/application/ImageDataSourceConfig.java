package ai.datagym.application;

import ai.datagym.application.utils.DbCheckBlocker;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = {"com.eforce21.lib.bin.data"},
        entityManagerFactoryRef = "imagesEntityManagerFactory",
        transactionManagerRef = "imagesTransactionManager")
@ComponentScan(basePackages = {"com.eforce21.lib.bin.data"})
public class ImageDataSourceConfig {
    @Value(value = "${datagym.deactivate-dbCheckBlocker}")
    private boolean deactivateDbCheckBlocker;

    @Bean("customImageDataSourceProperties")
    @ConfigurationProperties("images.datasource")
    public DataSourceProperties imageDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("images.datasource.configuration")
    public HikariDataSource imageDataSource() {
        return imageDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean imagesEntityManagerFactory(final EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(imageDataSource())
                .persistenceUnit("imagesPersistenceUnit")
                .packages("com.eforce21.lib.bin.data")
                .build();
    }

    @Bean
    public JpaTransactionManager imagesTransactionManager(@Qualifier("imagesEntityManagerFactory") final EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Bean
    public DbCheckBlocker imageDbCheckBlocker(@Qualifier("customImageDataSourceProperties") DataSourceProperties dataSourceProperties) {
        DbCheckBlocker result = new DbCheckBlocker();
        if(!deactivateDbCheckBlocker){
            result.addDataSource("imageDataSource", dataSourceProperties);
        }
        return result;
    }
}
