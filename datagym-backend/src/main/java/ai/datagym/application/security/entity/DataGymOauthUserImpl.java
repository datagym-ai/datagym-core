package ai.datagym.application.security.entity;

import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataGymOauthUserImpl implements OauthUser {
    private String sub;
    private String email;
    private String name;
    private Set<OidcOrgInfo> orgs = new HashSet<>();
    private Set<String> scopes = new HashSet<>();

    public DataGymOauthUserImpl() {
    }

    public DataGymOauthUserImpl(String sub, String email, String name, Set<OidcOrgInfo> orgs, Set<String> scopes) {
        this.sub = sub;
        this.email = email;
        this.name = name;
        this.orgs = orgs;
        this.scopes = scopes;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<OidcOrgInfo> getOrgs() {
        return orgs;
    }

    public void setOrgs(Set<OidcOrgInfo> orgs) {
        this.orgs = orgs;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public String token() {
        return null;
    }

    @Override
    public String id() {
        return this.sub;
    }

    @Override
    public Set<String> scopes() {
        Set<String> result = new HashSet<>();
        result.add("type_oauth");

        result.addAll(scopes);

        return result;
    }

    @Override
    public Map<String, String> orgs() {
        Map<String, String> result = new HashMap<>();

        for (OidcOrgInfo org : orgs) {
          String orgId =  org.getSub();
          String role =  org.getRole();

          result.put(orgId, role);
        }

        return result;
    }

    @Override
    public String toString() {
        return "DataGymOauthUserImpl{" +
                "sub='" + sub + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", orgs=" + orgs +
                ", scopes=" + scopes +
                '}';
    }
}
