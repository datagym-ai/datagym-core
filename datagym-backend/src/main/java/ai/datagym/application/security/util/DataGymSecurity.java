package ai.datagym.application.security.util;

import ai.datagym.application.errorHandling.FeatureNotForOpenCoreException;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ai.datagym.application.utils.constants.CommonMessages.BASIC_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.SUPER_ADMIN_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.TOKEN_SCOPE_TYPE;

public final class DataGymSecurity {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String ROOT_ROLE = "ROOT";

    private DataGymSecurity() {
    }

    public static boolean isAdmin(String owner, boolean allowForSuperUser) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();
        Map<String, String> orgs = user.orgs();

        // Check if User is in the current Organisation. If not, check if user is SuperAdmin
        if (!orgs.containsKey(owner)) {
            // Check if user is SuperAdmin and whether the method is allowed for a SuperAdmin
            if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && allowForSuperUser) {
                return true;
            } else if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && !allowForSuperUser) {
                throw new ForbiddenException();
            }
        }

        if (!scopes.contains(BASIC_SCOPE_TYPE)) {
            if (!orgs.containsKey(owner)) {
                throw new ForbiddenException();
            }

            boolean hasAdminRole = orgs.get(owner).equals(ADMIN_ROLE);

            if (!hasAdminRole) {
                throw new ForbiddenException();
            }
        }

        return true;
    }

    public static boolean checkIfUserIsAdmin(String owner, boolean allowForSuperUser) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();
        Map<String, String> orgs = user.orgs();

        // Check if User is in the current Organisation. If not, check if user is SuperAdmin
        if (!orgs.containsKey(owner)) {
            // Check if user is SuperAdmin and whether the method is allowed for a SuperAdmin
            if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && allowForSuperUser) {
                return true;
            } else if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && !allowForSuperUser) {
                throw new ForbiddenException();
            }
        }

        if (!scopes.contains(BASIC_SCOPE_TYPE)) {
            if (!orgs.containsKey(owner)) {
                throw new ForbiddenException();
            }
            return orgs.get(owner).equals(ADMIN_ROLE);
        }

        return true;
    }

    /**
     * Throw an exception, if the open-core edition of DataGym.ai is active
     */
    public static void disallowOnOpenCore() {
        OauthUser user = SecurityContext.get();

        if (user.id().equals("open-core-dummy-user-sub")) {
            throw new FeatureNotForOpenCoreException();
        }
    }

    public static boolean isAdminOrUser(String owner, boolean allowForSuperUser) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();
        Map<String, String> orgs = user.orgs();

        // Check if User is in the current Organisation. If not, check if user is SuperAdmin
        if (!orgs.containsKey(owner)) {
            // Check if user is SuperAdmin and whether the method is allowed for a SuperAdmin
            if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && allowForSuperUser) {
                return true;
            } else if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && !allowForSuperUser) {
                throw new ForbiddenException();
            }
        }

        if (!scopes.contains(BASIC_SCOPE_TYPE)) {
            if (!orgs.containsKey(owner)) {
                throw new ForbiddenException();
            }

            boolean hasAdminOrUserRole = orgs.get(owner).equals(ADMIN_ROLE) || orgs.get(owner).equals(USER_ROLE);

            if (!hasAdminOrUserRole) {
                throw new ForbiddenException();
            }
        }

        return true;
    }

    public static boolean isRoot(String owner, boolean allowForSuperUser) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();
        Map<String, String> orgs = user.orgs();

        // Check if User is in the current Organisation. If not, check if user is SuperAdmin
        if (!orgs.containsKey(owner)) {
            // Check if user is SuperAdmin and whether the method is allowed for a SuperAdmin
            if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && allowForSuperUser) {
                return true;
            } else if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && !allowForSuperUser) {
                throw new ForbiddenException();
            }
        }

        if (!orgs.containsKey(owner)) {
            throw new ForbiddenException();
        }

        boolean hasRootRole = orgs.get(owner).equals(ROOT_ROLE);

        if (!hasRootRole) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean haveTheSameOwner(String firstOwner, String secondOwner) {
        if (!firstOwner.equals(secondOwner)) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean isUserInCurrentOrg(String org) {
        OauthUser user = SecurityContext.get();

        if (user == null || !user.orgs().containsKey(org)) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean isAdminOrLabeler(String projectOrganisation, String currentTaskLabelerId, boolean allowForSuperUser) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();
        Map<String, String> orgs = user.orgs();

        // Check if User is in the current Organisation. If not, check if user is SuperAdmin
        if (!orgs.containsKey(projectOrganisation)) {
            // Check if user is SuperAdmin and whether the method is allowed for a SuperAdmin
            if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && allowForSuperUser) {
                return true;
            } else if (scopes.contains(SUPER_ADMIN_SCOPE_TYPE) && !allowForSuperUser) {
                throw new ForbiddenException();
            }
        }

        if (!orgs.containsKey(projectOrganisation)) {
            throw new ForbiddenException();
        }

        boolean hasAdminRole = orgs.get(projectOrganisation).equals(ADMIN_ROLE);

        if (scopes.contains(TOKEN_SCOPE_TYPE)) {
            if (!hasAdminRole) {
                throw new ForbiddenException();
            }
        } else {
            String userId = user.id();

            if (!userId.equals(currentTaskLabelerId) && !hasAdminRole) {
                throw new ForbiddenException();
            }
        }

        return true;
    }

    public static String getLoggedInUserId() {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();

        if (scopes.contains(OAUTH_SCOPE_TYPE)) {
            return user.id();
        } else if (scopes.contains(TOKEN_SCOPE_TYPE)) {
            return "token_user";
        } else if (scopes.contains(BASIC_SCOPE_TYPE)) {
            return "dummy";
        }

        return "no_user";
    }

    public static String getUserToken() {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();

        if (scopes.contains(OAUTH_SCOPE_TYPE)) {
            return user.token();
        }

        throw new ForbiddenException();
    }

    /**
     * Returns the OrganisationId from the the ApiToken. This method works ONLY for users with "TOKEN_SCOPE_TYPE"
     */
    public static String getCurrentOrgFromApiToken() {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> scopes = user.scopes();

        // Checks if User has "TOKEN_SCOPE_TYPE"
        if (scopes.contains(TOKEN_SCOPE_TYPE)) {
            Map<String, String> orgs = user.orgs();

            return orgs.keySet()
                    .stream()
                    .findFirst()
                    .orElseThrow(ForbiddenException::new);
        }

        throw new ForbiddenException();
    }

    public static Map<String, String> getLoggedInUserOrganisations() {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        return user.orgs();
    }

    public static boolean isAuthenticated() {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean isAuthenticatedAndHasAnyScope(String... scopesArr) {
        Set<String> scopes = new HashSet<>(Arrays.asList(scopesArr));

        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> currentUserScopes = user.scopes();

        if (currentUserScopes.stream().noneMatch(scopes::contains)) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean isAuthenticatedAndHasAllScopes(String... scopesArr) {
        Set<String> scopes = new HashSet<>(Arrays.asList(scopesArr));

        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> currentUserScopes = user.scopes();

        if (!currentUserScopes.containsAll(scopes)) {
            throw new ForbiddenException();
        }

        return true;
    }

    public static boolean isAuthenticatedAndHasCertainScope(String scope) {
        OauthUser user = SecurityContext.get();

        if (user == null) {
            throw new ForbiddenException();
        }

        Set<String> currentUserScopes = user.scopes();

        return currentUserScopes.contains(scope);
    }
}
