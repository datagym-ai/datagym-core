package ai.datagym.application.user.service;

import ai.datagym.application.accountmanagement.client.AccountManagementClient;
import ai.datagym.application.accountmanagement.client.model.OrgDataMinTO;
import ai.datagym.application.accountmanagement.client.model.UserDataMinTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class UserInformationServiceImpl implements UserInformationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInformationServiceImpl.class);

    @Autowired
    private Optional<AccountManagementClient> accountManagementClient;

    private Map<String, UserDataMinTO> fetchedUserData = new HashMap<>();
    private Map<String, OrgDataMinTO> fetchedOrgData = new HashMap<>();

    //Disable scheduler by default
    @Scheduled(cron = "${eforce.accountmgmt.fetch-schedule:-}")
    public void updateUsers() {
        fetchAllUserFromAccountManagement(null);
        fetchAllOrgsFromAccountManagement(null);
    }

    /**
     * Internal helper method to get a specific user. If no user found, the name equals the id.
     *
     * @param userId The specific id of the user
     * @return Instance of {@link UserDataMinTO}
     */
    private UserDataMinTO getUser(String userId) {
        UserDataMinTO userDataMinTO = fetchedUserData.get(userId);
        if (userDataMinTO != null) {
            return userDataMinTO;
        } else{
            // Try to fetch user if not available
            if (fetchAllUserFromAccountManagement(List.of(userId))) {
                userDataMinTO = fetchedUserData.get(userId);
                // Even after successful fetch no user is present
                if (userDataMinTO != null) {
                    return userDataMinTO;
                }
            }
            return new UserDataMinTO(userId, userId);
        }
    }


    /**
     * @param userIds The specific id of the user
     * @return {@code true} if at least one user was fetches successfully
     */
    private boolean fetchAllUserFromAccountManagement(List<String> userIds) {
        if (accountManagementClient.isEmpty()) {
            return false;
        }
        boolean addedNewUser = false;
        try {
            List<UserDataMinTO> userData = accountManagementClient.get().getUserData(userIds).execute().body();

            if (userData != null) {
                for (UserDataMinTO user : userData) {
                    if (!fetchedUserData.containsKey(user.getSub())) {
                        addedNewUser = true;
                    }
                    fetchedUserData.put(user.getSub(), user);
                }
            } else {

            }
        } catch (IOException e) {
            LOGGER.error("Error by fetching user from the account-management system!", e);
        }
        return addedNewUser;
    }

    /**
     * @param orgIds The specific id of the organisation
     * @return {@code true} if at least one organisation was fetches successfully
     */
    private boolean fetchAllOrgsFromAccountManagement(List<String> orgIds) {
        if (accountManagementClient.isEmpty()) {
            return false;
        }
        boolean addedNewOrg = false;
        try {
            List<OrgDataMinTO> orgData = accountManagementClient.get().getOrgData(orgIds).execute().body();

            if (orgData != null) {
                for (OrgDataMinTO org : orgData) {
                    if (!fetchedOrgData.containsKey(org.getSub())) {
                        addedNewOrg = true;
                    }
                    fetchedOrgData.put(org.getSub(), org);
                }
            } else {

            }
        } catch (IOException e) {
            LOGGER.error("Error by fetching organisations from the account-management system!", e);
        }
        return addedNewOrg;
    }


    @Override
    public String getUserName(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        UserDataMinTO userData = getUser(id);
        if (userData == null) {
            return id;
        } else {
            return userData.getName();
        }
    }

    @Override
    public UserDataMinTO getUserDataMin(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        return getUser(id);
    }

    @Override
    public OrgDataMinTO getOrgDataMin(String id) {
        OrgDataMinTO orgDataMinTO = fetchedOrgData.get(id);
        if (orgDataMinTO != null) {
            return orgDataMinTO;
        } else {
            if (fetchAllOrgsFromAccountManagement(List.of(id))) {
                orgDataMinTO = fetchedOrgData.get(id);
                if (orgDataMinTO != null) {
                    return orgDataMinTO;
                }
            }
            return new OrgDataMinTO(id, id, true);
        }
    }

}
