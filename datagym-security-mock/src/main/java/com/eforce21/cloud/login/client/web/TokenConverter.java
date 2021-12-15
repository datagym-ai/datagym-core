package com.eforce21.cloud.login.client.web;

import com.eforce21.cloud.login.client.ctx.TokenUserImpl;

public interface TokenConverter {

    /**
     * Convert an application internal token to an OauthUser.
     *
     * @param token
     * @return OauthUser or null if token is invalid/expired/whatever.
     */
    TokenUserImpl convert(String token);

}
