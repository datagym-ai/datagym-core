package ai.datagym.application.security.entity;

import com.eforce21.cloud.login.api.model.OauthUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataGymBasicUserImpl implements OauthUser {
    public static final String BASIC_SCOPE_TYPE = "type_basic";
    private Set<String> scopes = new HashSet<>();

    public DataGymBasicUserImpl() {
    }

    public DataGymBasicUserImpl(Collection<String> scopes) {
        this.scopes.add(BASIC_SCOPE_TYPE);
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
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
    public Map<String, String> orgs() {
        throw new UnsupportedOperationException("BasicUserImpl doesn't have a orgs.");
    }

    @Override
    public Set<String> scopes() {
        Set<String> result = new HashSet<>();
        result.add(BASIC_SCOPE_TYPE);

        result.addAll(scopes);

        return result;
    }

    @Override
    public String toString() {
        return "DataGymBasicUserImpl{" +
                "scopes=" + scopes +
                '}';
    }
}
