package com.eforce21.cloud.login.api.model;

/**
 * OpenIdConnect minimal UserInfo. Use for searches or wherever you dont want to
 * expose private user details like email or org/app assignment details.
 *
 * @author t
 */
public class OidcUserInfoMin {

    private String sub;
    private String name;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "OidcUserInfoMin [sub=" + sub + ", name=" + name + "]";
    }

}
