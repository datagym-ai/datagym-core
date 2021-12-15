package com.eforce21.cloud.login.api.model;

/**
 * OpenIdConnect OrgInfo (Custom extension to UserInfo).
 *
 * @author t
 */
public class OidcOrgInfo {

    private String sub;
    private String name;
    private String role;
    private boolean personal;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    @Override
    public String toString() {
        return "OidcOrgInfo [sub=" + sub + ", name=" + name + ", role=" + role + ", personal=" + personal + "]";
    }


}
