package ai.datagym.application.externalAPI.service;

import ai.datagym.application.externalAPI.entity.ApiToken;
import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;

public final class ApiTokenMapper {

    private ApiTokenMapper() {
    }

    public static ApiTokenViewModel mapToApiTokenViewModel(ApiToken from) {
        ApiTokenViewModel to = new ApiTokenViewModel();

        to.setId(from.getId());
        to.setName(from.getName());
        to.setCreatedAt(from.getCreatedAt());
        to.setLastUsed(from.getLastUsed());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setOwner(from.getOwner());
        return to;
    }

    public static ApiToken mapToApiToken(ApiTokenCreateBindingModel from) {
        long currentTime = System.currentTimeMillis();

        ApiToken to = new ApiToken();

        to.setName(from.getName());
        to.setCreatedAt(currentTime);
        to.setLastUsed(null);
        to.setDeleted(false);
        to.setDeleteTime(null);
        to.setOwner(from.getOwner());

        return to;
    }
}
