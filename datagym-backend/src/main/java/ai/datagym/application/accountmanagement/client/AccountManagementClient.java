package ai.datagym.application.accountmanagement.client;

import ai.datagym.application.accountmanagement.client.model.FeatureTO;
import ai.datagym.application.accountmanagement.client.model.OrgDataMinTO;
import ai.datagym.application.accountmanagement.client.model.UserDataMinTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

/**
 * Retrofit2 Client for the Account-Management REST-Interface
 */
public interface AccountManagementClient {

    @GET("/api/external/app/{appName}/features")
    Call<Map<String, List<FeatureTO>>> getPlanWithFeaturesForApp(@Path("appName") String name);

    @GET("/api/external/data/user")
    Call<List<UserDataMinTO>> getUserData(@Query("userIds") List<String> userIds);

    @GET("/api/external/data/org")
    Call<List<OrgDataMinTO>> getOrgData(@Query("orgIds") List<String> orgIds);
}
