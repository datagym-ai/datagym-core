package ai.datagym.application.externalAPI.service;

import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;

import java.util.List;

public interface ApiTokenService {
    List<ApiTokenViewModel> getApiTokenWhereUserAdmin();

    ApiTokenViewModel createApiToken(ApiTokenCreateBindingModel apiTokenCreateBindingModel);

    boolean isApiTokenNameUniqueAndDeletedFalse(String name, String owner);

    ApiTokenViewModel deleteApiTokenById(String id);
}
