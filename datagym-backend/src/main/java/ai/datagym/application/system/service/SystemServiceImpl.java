package ai.datagym.application.system.service;

import ai.datagym.application.system.models.DataGymEnvironment;
import ai.datagym.application.system.models.SystemInfoAppTO;
import ai.datagym.application.system.models.SystemInfoServerTO;
import ai.datagym.application.system.models.SystemInfoTO;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import static ai.datagym.application.utils.constants.CommonMessages.BASIC_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class SystemServiceImpl implements SystemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemServiceImpl.class);
    private SystemInfoAppTO infoAppTO = new SystemInfoAppTO();

    private final Environment environment;

    public SystemServiceImpl(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void start() {
        readInfo();
    }

    @AuthUser
    @AuthScope(any = {BASIC_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public SystemInfoTO getInfo() {
        SystemInfoTO result = new SystemInfoTO();
        result.setApp(infoAppTO);
        result.setServer(readInfoServer());

        Profiles profiles = Profiles.of("dev-mysql", "dev-system", "generate", "test-system");
        if (environment.acceptsProfiles(profiles)) {
            result.setEnvironment(DataGymEnvironment.DEV);
        } else {
            result.setEnvironment(DataGymEnvironment.PROD);
        }
        return result;
    }

    /**
     * Read build properties the classic way so we don't need to hassle with Spring EL
     * conflicting with maven replacements whenever IDE failed to replace props.
     */
    private void readInfo() {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/build.properties")) {
            p.load(in);
            infoAppTO.setName(p.getProperty("buildName"));
            infoAppTO.setVersion(p.getProperty("buildVersion"));
            infoAppTO.setDate(p.getProperty("buildDate"));
        } catch (IOException e) {
            LOGGER.error("Failed to read build properties. " + e.getMessage(), e);
        }
    }

    private SystemInfoServerTO readInfoServer() {
        SystemInfoServerTO result = new SystemInfoServerTO();
        result.setOs(System.getProperty("os.name") + " - " + System.getProperty("os.version") + " - " + System.getProperty("os.arch"));
        result.setJava(System.getProperty("java.version") + " - " + System.getProperty("java.vendor"));
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        result.setEnv("Charset: " + Charset.defaultCharset() + ", Encoding: " + System.getProperty("file.encoding") + ", Timezone: " + timeZone.getID() + " - " + timeZone.getDisplayName());

        //Format UTC Time
        ZonedDateTime zonedDateTimeUTC = Instant.now().atZone(ZoneId.of("UTC"));
        String formattedUTCTime = getFormattedTime(zonedDateTimeUTC);

        //Format Local Time
        ZonedDateTime zonedDateTimeSystem = Instant.now().atZone(ZoneId.systemDefault());
        String formattedLocalTime = getFormattedTime(zonedDateTimeSystem);

        long ts = System.currentTimeMillis();
        result.setTimeStamp(ts);
        result.setTimeUtc(formattedUTCTime);
        result.setTimeLocal(formattedLocalTime);

        return result;
    }

    // Format Time
    private String getFormattedTime(ZonedDateTime zonedDateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String zoneNameSystem = zonedDateTime.getZone().getId();

        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        String format = dtf.format(localDateTime);
        String formattedTime = String.format("%s - %s", format, zoneNameSystem);
        LOGGER.info("System Info: {} Time: {}", zoneNameSystem, formattedTime);

        return formattedTime;
    }
}
