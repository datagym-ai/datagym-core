package ai.datagym.application.aiseg.config;

import ai.datagym.application.aiseg.client.AiSegClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(
        value = "aiseglb.enabled",
        havingValue = "true")
public class AiSegConfig {

    @Value("${aiseglb.url}")
    private String baseUrl;

    @Value("${aiseglb.username}")
    private String authUsername;

    @Value("${aiseglb.password}")
    private String authPassword;
    @Value("${aiseglb.timeout}")
    private int timeout;

    @Bean
    public AiSegClient aiSegClient() {
        OkHttpClient aiSegClient = new OkHttpClient()
                .newBuilder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                // the authenticator()-Method didnt work, so we are using a Interceptor
                .addInterceptor(new BasicAuthInterceptor(authUsername, authPassword))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(aiSegClient)
                .addConverterFactory(JacksonConverterFactory.create(createObjectMapper()))
                .build();
        return retrofit.create(AiSegClient.class);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return om;
    }
}
