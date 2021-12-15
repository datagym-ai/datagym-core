package ai.datagym.application.accountmanagement.config;

import ai.datagym.application.accountmanagement.client.AccountManagementClient;
import ai.datagym.application.aiseg.config.BasicAuthInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@ConditionalOnProperty(
        value = "eforce.accountmgmt.enabled",
        havingValue = "true")
public class AccountManagementClientConfig {

    @Value(value = "${eforce.accountmgmt.url}")
    private String accountMgmtUrl;

    @Value(value = "${eforce.accountmgmt.username}")
    private String accountMgmtUsername;

    @Value(value = "${eforce.accountmgmt.password}")
    private String accountMgmtPassword;

    @Bean
    public AccountManagementClient accountManagementClient(){
        OkHttpClient accountManagementClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(new BasicAuthInterceptor(accountMgmtUsername, accountMgmtPassword))
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(accountMgmtUrl)
                .client(accountManagementClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(AccountManagementClient.class);
    }
}
