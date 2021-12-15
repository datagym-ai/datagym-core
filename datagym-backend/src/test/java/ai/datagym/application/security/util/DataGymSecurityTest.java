package ai.datagym.application.security.util;

import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ai.datagym.application.testUtils.SecurityUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class DataGymSecurityTest {

    @Test
    void isAdmin_whenUserIsAuthenticatedAndIsAdmin_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdmin = DataGymSecurity.isAdmin("eforce21", false);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void isAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdmin("eforce21", false)
        );
    }

    @Test
    void isAdmin_whenUserIsNotInTheOrgAndUserIsNotSuperAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdmin("test_org", false)
        );
    }

    @Test
    void isAdmin_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdmin("datagym", false)
        );
    }

    @Test
    void isAdmin_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdmin = DataGymSecurity.isAdmin("test_org", true);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void isAdmin_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsNotAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdmin("test_org", false)
        );
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsAuthenticatedAndIsAdmin_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin("eforce21", false);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.checkIfUserIsAdmin("eforce21", false)
        );
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsNotInTheOrgAndUserIsNotSuperAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.checkIfUserIsAdmin("test_org", false)
        );
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then

        // When
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin("datagym", false);

        // Then
        assertFalse(isAdmin);
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin("test_org", true);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void checkIfUserIsAdmin_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsNotAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.checkIfUserIsAdmin("test_org", false)
        );
    }

    @Test
    void checkIfUserIsAdmin_whenUserHasBasicScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createUserWithBasicScope();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin("test_org", true);

        // Then
        assertTrue(isAdmin);
    }

    @Test
    void isAdminOrUser_whenUserIsAuthenticatedAndIsAdmin_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrUser = DataGymSecurity.isAdminOrUser("eforce21", false);

        // Then
        assertTrue(isAdminOrUser);
    }

    @Test
    void isAdminOrUser_whenUserHasUserRole_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrUser = DataGymSecurity.isAdminOrUser("datagym", false);

        // Then
        assertTrue(isAdminOrUser);
    }

    @Test
    void isAdminOrUser_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrUser("eforce21", false)
        );
    }

    @Test
    void isAdminOrUser_whenUserIsNotInTheOrgAndUserIsNotSuperAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrUser("test_org", false)
        );
    }

    @Test
    void isAdminOrUser_whenUserHasNotAdminOrUserRole_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrUser("eforce21", false)
        );
    }

    @Test
    void isAdminOrUser_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrUser = DataGymSecurity.isAdminOrUser("test_org", true);

        // Then
        assertTrue(isAdminOrUser);
    }

    @Test
    void isAdminOrUser_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsNotAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrUser("test_org", false)
        );
    }

    @Test
    void isAdminOrUser_whenUserHasBasicScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createUserWithBasicScope();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrUser = DataGymSecurity.isAdminOrUser("test_org", true);

        // Then
        assertTrue(isAdminOrUser);
    }

    @Test
    void isRoot_whenUserIsAuthenticatedAndIsAdmin_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // When
        boolean isRoot = DataGymSecurity.isRoot("eforce21", false);

        // Then
        assertTrue(isRoot);
    }

    @Test
    void isRoot_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isRoot("eforce21", false)
        );
    }

    @Test
    void isRoot_whenUserIsNotInTheOrgAndUserIsNotSuperAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isRoot("test_org", false)
        );
    }

    @Test
    void isRoot_whenUserIsNotRoot_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isRoot("datagym", false)
        );
    }

    @Test
    void isRoot_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isRoot = DataGymSecurity.isRoot("test_org", true);

        // Then
        assertTrue(isRoot);
    }

    @Test
    void isRoot_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsNotAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isRoot("test_org", false)
        );
    }

    @Test
    void isAdminOrLabeler_whenUserIsAuthenticatedAndIsAdmin_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrLabeler = DataGymSecurity.isAdminOrLabeler("eforce21", null, false);

        // Then
        assertTrue(isAdminOrLabeler);
    }

    @Test
    void isAdminOrLabeler_whenUserIsAuthenticatedAndIsLabeler_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrLabeler = DataGymSecurity.isAdminOrLabeler("datagym", "eforce21", false);

        // Then
        assertTrue(isAdminOrLabeler);
    }

    @Test
    void isAdminOrLabeler_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrLabeler("eforce21", null, false)
        );
    }

    @Test
    void isAdminOrLabeler_whenUserIsNotInTheOrgAndUserIsNotSuperAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrLabeler("test_org", null, false)
        );
    }

    @Test
    void isAdminOrLabeler_whenUserIsNotAdminAndNotLabeler_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrLabeler("datagym", null, false)
        );
    }

    @Test
    void isAdminOrLabeler_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAdminOrLabeler = DataGymSecurity.isAdminOrLabeler("test_org", null, true);

        // Then
        assertTrue(isAdminOrLabeler);
    }

    @Test
    void isAdminOrLabeler_whenUserIsNotInTheOrgAndUserIsSuperAdminAndMethodIsNotAllowedForSuperAdmins_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAdminOrLabeler("test_org", null, false)
        );
    }

    @Test
    void isAuthenticated_whenUserIsAuthenticated_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        boolean isAuthenticated = DataGymSecurity.isAuthenticated();

        // Then
        assertTrue(isAuthenticated);
    }

    @Test
    void isAuthenticated_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::isAuthenticated
        );
    }

    @Test
    void getLoggedInUserOrganisations_whenUserIsAuthenticatedAndTwoOrganisations_returnsTwoOrganisations() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        Map<String, String> loggedInUserOrganisations = DataGymSecurity.getLoggedInUserOrganisations();

        // Then
        assertNotNull(loggedInUserOrganisations);
        assertEquals(2, loggedInUserOrganisations.size());
        assertEquals("USER", loggedInUserOrganisations.get("datagym"));
        assertEquals("ADMIN", loggedInUserOrganisations.get("eforce21"));
    }

    @Test
    void getLoggedInUserOrganisations_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getLoggedInUserOrganisations
        );
    }

    @Test
    void getLoggedInUserId_whenUserIsAuthenticatedAndHasOauthScope_returnsLoggedInUserId() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        // Then
        assertNotNull(loggedInUserId);
        assertEquals("eforce21", loggedInUserId);
    }

    @Test
    void getLoggedInUserId_whenUserIsAuthenticatedAndHasTokenScope_returnsLoggedInUserId() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        // Then
        assertNotNull(loggedInUserId);
        assertEquals("token_user", loggedInUserId);
    }

    @Test
    void getLoggedInUserId_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getLoggedInUserId
        );
    }

    @Test
    void isUserInCurrentOrg_whenUserIsAuthenticatedAndIsMemberOfTheOrg_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isUserInCurrentOrg = DataGymSecurity.isUserInCurrentOrg("datagym");

        // Then
        assertTrue(isUserInCurrentOrg);
    }

    @Test
    void isUserInCurrentOrg_whenUserIsAuthenticatedAndIsNotMemberOfTheOrg_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isUserInCurrentOrg("test_org")
        );
    }

    @Test
    void isUserInCurrentOrg_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isUserInCurrentOrg("test_org")
        );
    }

    @Test
    void getUserToken_whenUserIsAuthenticatedAndHasTokenScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getUserToken
        );
    }

    @Test
    void getUserToken_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getUserToken
        );
    }

    @Test
    void getCurrentOrgFromApiToken_whenUserIsAuthenticatedAndHasTokenScope_returnsCurrentOrgFromApiToken() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        String currentOrgFromApiToken = DataGymSecurity.getCurrentOrgFromApiToken();

        // Then
        assertNotNull(currentOrgFromApiToken);
        assertEquals("eforce21", currentOrgFromApiToken);
    }

    @Test
    void getCurrentOrgFromApiToken_whenUserIsAuthenticatedAndDoesNotHaveTokenScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getCurrentOrgFromApiToken
        );
    }

    @Test
    void getCurrentOrgFromApiToken_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                DataGymSecurity::getCurrentOrgFromApiToken
        );
    }

    @Test
    void haveTheSameOwner_whenFirstOwnerEqualsSecondOwner_returnsTrue() {
        // When
        boolean haveTheSameOwner = DataGymSecurity.haveTheSameOwner("eforce21", "eforce21");

        // Then
        assertTrue(haveTheSameOwner);
    }

    @Test
    void haveTheSameOwner_whenFirstOwnerDoesNotEqualSecondOwner_throwException() {
        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.haveTheSameOwner("firstOwner", "secondOwner")
        );
    }

    @Test
    void isAuthenticatedAndHasAnyScope_whenUserIsAuthenticatedAndHasAnyScopeFromInput_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        boolean isAuthenticatedAndHasAnyScope = DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        // Then
        assertTrue(isAuthenticatedAndHasAnyScope);
    }

    @Test
    void isAuthenticatedAndHasAnyScope_whenUserDoesNotHaveAnyScopeFromInput_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE)
        );
    }

    @Test
    void isAuthenticatedAndHasAnyScope_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE)
        );
    }

    @Test
    void isAuthenticatedAndHasAllScopes() {
    }

    @Test
    void isAuthenticatedAndHasAllScopes_whenUserIsAuthenticatedAndHasAllScopesFromInput_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createUserWithBasicScope();
        SecurityContext.set(oauthUser);

        // When
        boolean isAuthenticatedAndHasAllScopes = DataGymSecurity.isAuthenticatedAndHasAllScopes(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        // Then
        assertTrue(isAuthenticatedAndHasAllScopes);
    }

    @Test
    void isAuthenticatedAndHasAllScopes_whenUserDoesNotHaveAllScopesFromInput_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAuthenticatedAndHasAllScopes(BASIC_SCOPE_TYPE, OAUTH_SCOPE_TYPE)
        );
    }

    @Test
    void isAuthenticatedAndHasAllScopes_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Then
        assertThrows(ForbiddenException.class,
                () -> DataGymSecurity.isAuthenticatedAndHasAllScopes(BASIC_SCOPE_TYPE, OAUTH_SCOPE_TYPE)
        );
    }
}