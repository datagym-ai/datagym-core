package ai.datagym.application.security.service;

import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import com.eforce21.cloud.login.api.model.OidcUserInfoMin;

public final class UserInfoModelMapper {
    private UserInfoModelMapper(){}

    public static UserMinInfoViewModel mapToUserMinInfoViewModel(OidcUserInfoMin from) {
        UserMinInfoViewModel to = new UserMinInfoViewModel();
        to.setId(from.getSub());
        to.setName(from.getName());
        return to;
    }
}
