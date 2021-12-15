package ai.datagym.application.security.util;

import ai.datagym.application.security.entity.DataGymOauthUserImpl;
import ai.datagym.application.security.entity.DataGymTokenUserImpl;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DataGymSecurityUtils {
    public static final String OAUTH_SCOPE_TYPE = "type_oauth";
    public static final String TOKEN_SCOPE_TYPE = "type_token";
    public static final String SUPER_ADMIN_SCOPE_TYPE = "account.admin";
    public static final String BASIC_SCOPE_TYPE = "type_basic";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    public static final String ROOT_ROLE = "ROOT";
    public static final String EFORCE_21 = "eforce21";
    public static final String DATAGYM = "datagym";
    public static final String USER_FIRST_NAME = "reiner";
    public static final String USER_EMAIL = "reiner@zufall.com";

    private DataGymSecurityUtils() {
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
        firstOrgInfo.setRole(ADMIN_ROLE);
        firstOrgInfo.setSub(EFORCE_21);
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName(USER_FIRST_NAME);

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole(USER_ROLE);
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>();
        orgInfoSet.add(firstOrgInfo);
        orgInfoSet.add(secondOrgInfo);


        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl(EFORCE_21, USER_EMAIL, USER_FIRST_NAME, orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithTwoOrgsAndWithValuesAndNoAdminRole() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole(USER_ROLE);
        firstOrgInfo.setSub(EFORCE_21);
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName(USER_FIRST_NAME);

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>();
        orgInfoSet.add(firstOrgInfo);
        orgInfoSet.add(secondOrgInfo);

        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl(EFORCE_21, USER_EMAIL, USER_FIRST_NAME, orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithOneOrgsAndWithValues() {
        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole(USER_ROLE);
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>();
        orgInfoSet.add(secondOrgInfo);

        Set<String> scopes = new HashSet<>();
        scopes.add(OAUTH_SCOPE_TYPE);

        return new DataGymOauthUserImpl(EFORCE_21, USER_EMAIL, USER_FIRST_NAME, orgInfoSet, scopes);
    }

    public static OauthUser createOauthUserWithRootRole() {
        OidcOrgInfo orgInfoInstance = createOrgInfoInstance(EFORCE_21, "name", ROOT_ROLE, true);
        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole(USER_ROLE);
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);
        HashSet<OidcOrgInfo> oidcOrgInfos = new HashSet<>();
        oidcOrgInfos.add(orgInfoInstance);
        oidcOrgInfos.add(secondOrgInfo);

        Set<String> scopes = new HashSet<>();

        return DataGymSecurityUtils.createTestOauthUser(EFORCE_21, "name", "email@email.com", oidcOrgInfos, scopes);
    }

    public static OauthUser createTestTokenUser() {
        Set<String> scopes = new HashSet<>();
        scopes.add(TOKEN_SCOPE_TYPE);

        Map<String, String> orgs = new HashMap<>();
        orgs.put(EFORCE_21, ADMIN_ROLE);

        return new DataGymTokenUserImpl(scopes, orgs);
    }

    public static OauthUser createSuperAdminUserWithTwoOrgsAndWithValues() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole(ADMIN_ROLE);
        firstOrgInfo.setSub(EFORCE_21);
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName(USER_FIRST_NAME);

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole("USER");
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>();
        orgInfoSet.add(firstOrgInfo);
        orgInfoSet.add(secondOrgInfo);

        Set<String> scopes = new HashSet<>(Arrays.asList(SUPER_ADMIN_SCOPE_TYPE, OAUTH_SCOPE_TYPE));

        return new DataGymOauthUserImpl(EFORCE_21, USER_EMAIL, USER_FIRST_NAME, orgInfoSet, scopes);
    }

    public static OauthUser createUserWithBasicScope() {
        OidcOrgInfo firstOrgInfo = new OidcOrgInfo();
        firstOrgInfo.setRole(ADMIN_ROLE);
        firstOrgInfo.setSub(EFORCE_21);
        firstOrgInfo.setPersonal(true);
        firstOrgInfo.setName(USER_FIRST_NAME);

        OidcOrgInfo secondOrgInfo = new OidcOrgInfo();
        secondOrgInfo.setRole(USER_ROLE);
        secondOrgInfo.setSub(DATAGYM);
        secondOrgInfo.setPersonal(true);
        secondOrgInfo.setName(USER_FIRST_NAME);

        Set<OidcOrgInfo> orgInfoSet = new HashSet<>();
        orgInfoSet.add(firstOrgInfo);
        orgInfoSet.add(secondOrgInfo);

        Set<String> scopes = new HashSet<>(Arrays.asList(BASIC_SCOPE_TYPE));

        return new DataGymOauthUserImpl(EFORCE_21, USER_EMAIL, USER_FIRST_NAME, orgInfoSet, scopes);
    }
}
