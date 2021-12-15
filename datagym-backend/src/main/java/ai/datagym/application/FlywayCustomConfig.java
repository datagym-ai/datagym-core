package ai.datagym.application;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "customFlyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class FlywayCustomConfig {

    @Resource(name = "imageDataSource")
    private DataSource imageDataSource;

    @Resource(name = "defaultDataSource")
    private DataSource defaultDataSource;

    @Bean(name = "flywayDefault")
    public Flyway flywayDefault() {
        return Flyway.configure()
                .locations("db/migration")
                .dataSource(defaultDataSource).load();
    }

    @Bean(name = "flywayImages")
    @ConfigurationProperties(prefix = "flyway.images")
    public Flyway flywayImages() {
        return Flyway.configure()
                .locations("db/migration-images")
                .dataSource(imageDataSource).load();
    }


    @Bean
    @Primary
    public FlywayMigrationInitializer flywayInitializerOne(@Qualifier("flywayDefault") Flyway defaultFlyway) {
        return new FlywayMigrationInitializer(defaultFlyway, null);
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializerTwo(@Qualifier("flywayImages") Flyway flywayImages) {
        return new FlywayMigrationInitializer(flywayImages, null);
    }


}
