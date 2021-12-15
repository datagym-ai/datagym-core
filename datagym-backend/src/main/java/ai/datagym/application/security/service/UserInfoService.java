package ai.datagym.application.security.service;

import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import com.eforce21.cloud.login.api.model.OidcUserInfo;

import java.io.IOException;
import java.util.List;

public interface UserInfoService {

    OidcUserInfo getCurrentUserFullInfo();

    List<UserMinInfoViewModel> getAllUsersFromOrg(String organisationId, String role) throws IOException;

    String getOrgName(String organisationId, OidcUserInfo userInfo);
}
