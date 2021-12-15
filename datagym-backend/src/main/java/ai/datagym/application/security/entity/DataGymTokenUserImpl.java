package ai.datagym.application.security.entity;

import com.eforce21.cloud.login.api.model.OauthUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataGymTokenUserImpl implements OauthUser {
    public static final String TOKEN_SCOPE_TYPE = "type_token";
    private Set<String> scopes = new HashSet<>();
    private Map<String, String> orgs = new HashMap<>();

    public DataGymTokenUserImpl() {
    }

    public DataGymTokenUserImpl(Set<String> scopes, Map<String, String> orgs) {
        this.scopes = scopes;
        this.orgs = orgs;
    }


    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Map<String, String> getOrgs() {
        return orgs;
    }

    public void setOrgs(Map<String, String> orgs) {
        this.orgs = orgs;
    }

    @Override
    public String token() {
        throw new UnsupportedOperationException("TokenUserImpl doesn't have a token.");
    }

    @Override
    public String id() {
        throw new UnsupportedOperationException("TokenUserImpl doesn't have a user id.");
    }

    @Override
    public Set<String> scopes() {
        Set<String> result = new HashSet<>();
        result.add(TOKEN_SCOPE_TYPE);

        result.addAll(scopes);

        return result;
    }

    @Override
    public Map<String, String> orgs() {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> org : orgs.entrySet()) {
            String orgId =  org.getKey();
            String role =  org.getValue();

            result.put(orgId, role);
        }

        return result;
    }

    @Override
    public String toString() {
        return "DataGymTokenUserImpl{" +
                "scopes=" + scopes +
                ", orgs=" + orgs +
                '}';
    }
}
