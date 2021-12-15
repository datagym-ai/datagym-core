package com.eforce21.cloud.login.client.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AuthScope {

    /**
     * User must have any (at least one) of the specified scopes to pass.
     *
     * @return
     */
    String[] any() default {};

    /**
     * User must have all of the specified scopes to pass.
     *
     * @return
     */
    String[] all() default {};
}
