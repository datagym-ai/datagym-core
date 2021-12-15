package ai.datagym.application.externalAPI.service;

import ai.datagym.application.externalAPI.entity.ApiToken;
import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;
import ai.datagym.application.externalAPI.repo.ApiTokenRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ApiTokenServiceImpl implements ApiTokenService {
    private boolean deactivateLimiter;

    private final ApiTokenRepository apiTokenRepository;
    private final LabelConfigurationService labelConfigurationService;
    private final LimitService limitService;

    @Autowired
    public ApiTokenServiceImpl(ApiTokenRepository apiTokenRepository,
                               LabelConfigurationService labelConfigurationService,
                               LimitService limitService,
                               @Value(value = "${datagym.deactivate-limiter}") boolean deactivateLimiter) {
        this.apiTokenRepository = apiTokenRepository;
        this.labelConfigurationService = labelConfigurationService;
        this.limitService = limitService;
        this.deactivateLimiter = deactivateLimiter;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<ApiTokenViewModel> getApiTokenWhereUserAdmin() {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        OauthUser user = SecurityContext.get();
        Map<String, String> orgs = user.orgs();

        List<ApiTokenViewModel> result = new ArrayList<>();

        for (Map.Entry<String, String> orgsEntry : orgs.entrySet()) {
            String currentUserOrg = orgsEntry.getKey();

            boolean isAdminInCurrentOrg = DataGymSecurity.checkIfUserIsAdmin(currentUserOrg, false);

            if (isAdminInCurrentOrg) {
                List<ApiTokenViewModel> apiTokenViewModels = apiTokenRepository
                        .findAllByDeletedIsFalseAndOwner(currentUserOrg)
                        .stream()
                        .map(ApiTokenMapper::mapToApiTokenViewModel)
                        .collect(Collectors.toList());

                result.addAll(apiTokenViewModels);
            }
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ApiTokenViewModel createApiToken(@NotNull ApiTokenCreateBindingModel apiTokenCreateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        String owner = apiTokenCreateBindingModel.getOwner();

        //Permissions check
        DataGymSecurity.isAdmin(owner, false);

        // Deactivate pricing limits for the Unit-Tests
        if (!deactivateLimiter) {

            //Check if Organisation-PricingPlan allows creation of ApiTokens
            String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

            if (DataGymPlan.FREE_DEVELOPER.name().equals(pricingPlanType)) {
                throw new GenericException("apiToken_creation_not_allowed", null, null);
            }
        }

        // Check forbidden keywords
        String apiTokenName = apiTokenCreateBindingModel.getName();
        List<String> forbiddenKeyWords = labelConfigurationService.getForbiddenKeyWords();

        if (forbiddenKeyWords.contains(apiTokenName)) {
            throw new GenericException("forbidden_keyword", null, null, apiTokenName);
        }

        ApiToken apiToken = ApiTokenMapper.mapToApiToken(apiTokenCreateBindingModel);
        ApiToken createdApiToken = apiTokenRepository.saveAndFlush(apiToken);

        return ApiTokenMapper.mapToApiTokenViewModel(createdApiToken);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public boolean isApiTokenNameUniqueAndDeletedFalse(String name, String owner) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        return apiTokenRepository
                .findByNameAndDeletedFalseAndOwner(name, owner)
                .isEmpty();
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ApiTokenViewModel deleteApiTokenById(String id) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        ApiToken apiTokenById = getApiTokenById(id);

        //Permissions check
        String owner = apiTokenById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        long currentTime = System.currentTimeMillis();

        apiTokenById.setDeleted(true);
        apiTokenById.setDeleteTime(currentTime);

        ApiToken deletedApiToken = apiTokenRepository.saveAndFlush(apiTokenById);

        return ApiTokenMapper.mapToApiTokenViewModel(deletedApiToken);
    }

    private ApiToken getApiTokenById(String apiTokenId) {
        return apiTokenRepository
                .findById(apiTokenId)
                .orElseThrow(() -> new NotFoundException("ApiToken", "id", "" + apiTokenId));
    }
}
