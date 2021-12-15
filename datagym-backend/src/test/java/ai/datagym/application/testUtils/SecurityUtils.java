package ai.datagym.application.testUtils;

import ai.datagym.application.security.entity.DataGymOauthUserImpl;
import ai.datagym.application.security.entity.DataGymTokenUserImpl;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;

import java.util.*;

public final class SecurityUtils {
    public static final String OAUTH_SCOPE_TYPE = "type_oauth";
    public static final String TOKEN_SCOPE_TYPE = "type_token";
    public static final String SUPER_ADMIN_SCOPE_TYPE = "account.admin";
    public static final String BASIC_SCOPE_TYPE = "type_basic";

    private SecurityUtils() {
    }

    public static OauthUser createTestOauthUser(String sub, String name, String email, Set<OidcOrgInfo> orgInfoSet, Set<String> scopes) {
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl(sub, email, name, orgInfoSet, scopes);
    }

    public static OidcOrgInfo createOrgInfoInstance(String sub, String name, String role, boolean personal) {
        OidcOrgInfo orgInfo = new OidcOrgInfo();
        orgInfo.setSub(sub);
        orgInfo.setRole(role);
        orgInfo.setName(name);
        orgInfo.setPersonal(personal);

        return orgInfo;
    }

    public static OauthUser createOauthUserWithTwoOrgsAndWithValues() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole("ADMIN");
        firstOrgInfo.setSub("eforce21");
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName("reiner");

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>() {{
            add(firstOrgInfo);
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl("eforce21", "reiner@zufall.com", "reiner", orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithTwoOrgsAndWithValuesAndNoAdminRole() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole("USER");
        firstOrgInfo.setSub("eforce21");
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName("reiner");

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>() {{
            add(firstOrgInfo);
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl("eforce21", "reiner@zufall.com", "reiner", orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithOneOrgsAndWithValues() {
        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>() {{
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl("eforce21", "reiner@zufall.com", "reiner", orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithInvalidRole() {
        OidcOrgInfo orgInfoInstance = createOrgInfoInstance("eforce21", "name", "INVALID_ROLE", true);
        HashSet<OidcOrgInfo> oidcOrgInfos = new HashSet<>() {{
            add(orgInfoInstance);
        }};

        Set<String> scopes = new HashSet<>();

        return SecurityUtils.createTestOauthUser("eforce21", "name", "email@email.com", oidcOrgInfos, scopes);
    }

    public static OauthUser createOauthUserWithRootRole() {
        OidcOrgInfo orgInfoInstance = createOrgInfoInstance("eforce21", "name", "ROOT", true);
        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");
        HashSet<OidcOrgInfo> oidcOrgInfos = new HashSet<>() {{
            add(orgInfoInstance);
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>();

        return SecurityUtils.createTestOauthUser("eforce21", "name", "email@email.com", oidcOrgInfos, scopes);
    }

    public static OauthUser createTestTokenUser() {
        Set<String> scopes = new HashSet<>();
        scopes.add(TOKEN_SCOPE_TYPE);

        Map<String, String> orgs = new HashMap<>();
        orgs.put("eforce21", "ADMIN");

        return new DataGymTokenUserImpl(scopes, orgs);
    }

    public static OauthUser createSuperAdminUserWithTwoOrgsAndWithValues() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole("ADMIN");
        firstOrgInfo.setSub("eforce21");
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName("reiner");

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>() {{
            add(firstOrgInfo);
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>(Arrays.asList(SUPER_ADMIN_SCOPE_TYPE, OAUTH_SCOPE_TYPE));

        return new DataGymOauthUserImpl("eforce21", "reiner@zufall.com", "reiner", orgInfoSet, scopes);
    }

    public static OauthUser createUserWithBasicScope() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole("ADMIN");
        firstOrgInfo.setSub("eforce21");
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName("reiner");

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub("datagym");
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName("reiner");

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>() {{
            add(firstOrgInfo);
            add(secondOrgInfo);
        }};

        Set<String> scopes = new HashSet<>(Arrays.asList(BASIC_SCOPE_TYPE));

        return new DataGymOauthUserImpl("eforce21", "reiner@zufall.com", "reiner", orgInfoSet, scopes);
    }
}
