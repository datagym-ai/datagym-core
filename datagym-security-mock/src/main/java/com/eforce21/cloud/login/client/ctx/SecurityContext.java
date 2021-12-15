package com.eforce21.cloud.login.client.ctx;

import com.eforce21.cloud.login.api.model.DataGymOauthUserMockImpl;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;
import com.eforce21.cloud.login.api.model.OidcUserInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecurityContext {

    private static boolean intentionalSetNull = false;
    private static ThreadLocal<OauthUser> oauthUser = new ThreadLocal<>();

    public static void set(OauthUser ctx) {
        if (ctx == null) {
            intentionalSetNull = true;
        } else {
            intentionalSetNull = false;

        }
        oauthUser.set(ctx);

    }

    public static OauthUser get() {
        if (oauthUser.get() == null && !intentionalSetNull) {
            oauthUser.set(OidcUserInfo.getMock());
            return oauthUser.get();
        }
        return oauthUser.get();
    }

    public static void clear() {
        oauthUser.set(null);
        intentionalSetNull = true;
    }

}
