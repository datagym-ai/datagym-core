package ai.datagym.application;

import com.eforce21.cloud.login.client.ModuleCloudLoginClient;
import com.eforce21.cloud.login.client.web.BasicConverterProps;
import com.eforce21.lib.bin.data.dao.BinDataRepo;
import com.eforce21.lib.bin.data.dao.BinDataRepoDb;
import com.eforce21.lib.bin.data.dao.BinDataRepoDbMssql;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.bin.file.service.BinFileServiceImpl;
import com.eforce21.lib.exception.base.EforceExceptionWriter;
import com.eforce21.lib.exception.base.EforceExceptionWriterJson;
import com.eforce21.lib.exception.spring.EforceExceptionConverterSpringWeb;
import com.eforce21.lib.exception.spring.EforceExceptionHandler;
import com.eforce21.lib.exception.spring.EforceExceptionSerializerSpring;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EntityScan(basePackageClasses = {Application.class})
@EnableScheduling
@Import(ModuleCloudLoginClient.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EforceExceptionHandler eforceExceptionHandler() {
        return new EforceExceptionHandler();
    }

    /**
     * Serialize exceptions using translation.
     */
    @Bean
    public EforceExceptionSerializerSpring eforceExceptionSerializer() {
        return new EforceExceptionSerializerSpring();
    }

    /**
     * Convert exceptions thrown by spring web.
     */
    @Bean
    public EforceExceptionConverterSpringWeb eforceExceptionConverterSpringWeb() {
        return new EforceExceptionConverterSpringWeb();
    }

    @Bean
    public EforceExceptionWriter eforceExceptionWriterMvc() {
        return new EforceExceptionWriterJson();
    }

    /**
     * Store binaries in database.
     *
     * @param imageDataSource
     * @param jdbcUrl
     * @return
     */
    @Bean
    public BinDataRepo binRepo(@Qualifier("imageDataSource") DataSource imageDataSource,
                               @Value("${images.datasource.url}") String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:sqlserver")) {
            return new BinDataRepoDbMssql(new JdbcTemplate(imageDataSource));
        } else {
            return new BinDataRepoDb(new JdbcTemplate(imageDataSource));
        }
    }

    @Bean
    public BinFileService binService() {
        return new BinFileServiceImpl();
    }

    @Bean
    @ConfigurationProperties("basic")
    public BasicConverterProps basicConverter() {
        return new BasicConverterProps();
    }
}
