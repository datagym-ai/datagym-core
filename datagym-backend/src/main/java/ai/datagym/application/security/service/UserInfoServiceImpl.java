package ai.datagym.application.security.service;

import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.api.model.OidcOrgInfo;
import com.eforce21.cloud.login.api.model.OidcUserInfo;
import com.eforce21.cloud.login.api.model.OidcUserInfoMin;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.service.ServiceOidc;
import com.eforce21.lib.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.BASIC_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.TOKEN_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class UserInfoServiceImpl implements UserInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoServiceImpl.class);
    private static final String USER_INFO_EXCEPTION_NAME = "user_info";

    private final ServiceOidc serviceOidc;

    @Autowired
    public UserInfoServiceImpl(ServiceOidc serviceOidc) {
        this.serviceOidc = serviceOidc;
    }

    /**
     * Get the full Info about the current loggedIn user
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public OidcUserInfo getCurrentUserFullInfo() {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        String userToken = DataGymSecurity.getUserToken();

        try {
            Response<OidcUserInfo> userInfoExecute = serviceOidc.userinfo("Bearer: " + userToken).execute();

            if (userInfoExecute.isSuccessful()) {
                return userInfoExecute.body();
            }
            LOGGER.error("Get UserInfo from the Login System failed");
            throw new GenericException(USER_INFO_EXCEPTION_NAME, null, null);
        } catch (IOException e) {

            LOGGER.error("Get UserInfo from the Login System failed");
            throw new GenericException(USER_INFO_EXCEPTION_NAME, null, null);
        }
    }

    /**
     * Get all Admin-Users for the current Organisation from the Login System
     *
     * @param organisationId the Id of the Organisation, in which will be searched for admin users
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public List<UserMinInfoViewModel> getAllUsersFromOrg(String organisationId, String role) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        String userToken = DataGymSecurity.getUserToken();
        String appName = "DATAGYM";

        try {
            // Fetch all Users from the current Organisation from the Login System
            // TODO APP Name is not available at this moment, add it in the future
            Response<List<OidcUserInfoMin>> execute = serviceOidc
                    .usersearch(
                            "Bearer: " + userToken,
                            "",
                            organisationId,
                            role,
                            null,
                            null,
                            null
                    ).execute();

            if (execute.isSuccessful()) {
                List<OidcUserInfoMin> body = execute.body();

                return Objects.requireNonNull(body)
                        .stream()
                        .map(UserInfoModelMapper::mapToUserMinInfoViewModel)
                        .collect(Collectors.toList());
            }

            LOGGER.error("Get All Users from Organisation from the Login System failed");
            throw new GenericException(USER_INFO_EXCEPTION_NAME, null, null);
        } catch (IOException e) {
            LOGGER.error("Get All Users from Organisation from the Login System failed");
            throw new GenericException(USER_INFO_EXCEPTION_NAME, null, null);
        }
    }

    /**
     * Get the name of the current Organisation.
     *
     * @param organisationId the Id of the Organisation of whom we are searching the name
     * @param userInfo       full info about an User, who is in the organisation with the "organisationId"
     * @return the name of the Organisation with id equal to "organisationId", If the user isn't in
     * the organisation with "organisationId", "null" will be returned
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public String getOrgName(String organisationId, OidcUserInfo userInfo) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        for (OidcOrgInfo org : userInfo.getOrgs()) {
            String orgId = org.getSub();

            if (orgId.equals(organisationId)) {
                return org.getName();
            }
        }

        return null;
    }
}
