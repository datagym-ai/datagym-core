package com.eforce21.cloud.login.client.service;

import com.eforce21.cloud.login.api.model.OidcJwks;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;
import com.eforce21.cloud.login.api.model.OidcUserInfo;
import com.eforce21.cloud.login.api.model.OidcUserInfoMin;
import com.eforce21.cloud.login.client.ctx.MockRetrofitCall;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.util.*;

@Component
public class ServiceOidcImpl implements ServiceOidc {

    /**
     * Get {@link OidcUserInfo} for authenticated user.
     *
     * @param authHeader Bearer access token.
     * @return
     */
    public Call<OidcUserInfo> userinfo(@Header("Authorization") String authHeader) {
        Response<OidcUserInfo> fakeResponse = Response.success(OidcUserInfo.getMock());
        return new MockRetrofitCall(fakeResponse);
    }

    /**
     * Search for ACTIVE users. Results are limited to 20.
     *
     * @param authHeader   Bearer access token.
     * @param searchString Search string checked case-insensitive against username
     *                     via "contains" and against email via "=". Spaces separate
     *                     multiple AND criteria.
     * @param orgId        Return users from this organisation.
     * @param appName      Return users permitted for this app. OrgId must be
     *                     specified, cause permissions are always per org per app.
     * @param nappName     Return users not permitted for this app. OrgId must be
     *                     specified, cause permissions are always per org per app.
     * @param norgId       Return users not in this organisation. OrgId must not be
     *                     specified if using this one.
     * @return
     */
    public Call<List<OidcUserInfoMin>> usersearch(@Header("Authorization") String authHeader, @Query("s") String searchString,
                                                  @Query("org") String orgId, @Query("role") String role, @Query("app") String appName, @Query("napp") String nappName,
                                                  @Query("norg") String norgId) {
        Response<List<OidcUserInfoMin>> fakeResponse = Response.success(Collections.emptyList());

        return new MockRetrofitCall<>(fakeResponse);
    }

    @Override
    public Call<OidcJwks> jwks() {
        return null;
    }
}
