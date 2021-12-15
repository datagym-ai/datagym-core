package ai.datagym.application.user.service;

import ai.datagym.application.accountmanagement.client.model.OrgDataMinTO;
import ai.datagym.application.accountmanagement.client.model.UserDataMinTO;

public interface UserInformationService {

    /**
     * Returns the name of an specific user
     *
     * @param id The specific id of the user
     * @return Username or id if not available
     */
    String getUserName(String id);

    /**
     * Returns the minimal data object of an user.
     *
     * @param id The specific id of the user
     * @return Instance of {@link UserDataMinTO} or {@code null}
     */
    UserDataMinTO getUserDataMin(String id);

    /**
     * Returns the minimal data object of an organisation.
     *
     * @param id The specific id of the organisation.
     * @return Instance of {@link OrgDataMinTO}
     */
    OrgDataMinTO getOrgDataMin(String id);
}
