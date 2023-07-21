package ai.datagym.application.limit.service;

import ai.datagym.application.accountmanagement.client.AccountManagementClient;
import ai.datagym.application.accountmanagement.client.model.FeatureTO;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.exception.LimitException;
import ai.datagym.application.limit.models.DataGymPlanDetails;
import ai.datagym.application.limit.models.bindingModels.LimitSetPricingPlanBindingModel;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.repo.LimitRepository;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import ai.datagym.application.security.service.UserInfoService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.api.model.OidcUserInfo;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static ai.datagym.application.utils.constants.CommonMessages.*;

/**
 * Responsible for:
 * - creating limits if the user uses DataGym the FIRST TIME
 * - updating limits triggered by the account-management system
 * - resetting the limits for FREE_PLAN organisations with @Scheduler
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LimitServiceImpl implements LimitService {
    private boolean deactivateLimiter;

    private Map<DataGymPlan, DataGymPlanDetails> datagymPlan = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LimitServiceImpl.class);

    private Optional<AccountManagementClient> accountManagementClient;
    private final LimitRepository limitRepository;

    public LimitServiceImpl(LimitRepository limitRepository,
                            @Value(value = "${datagym.deactivate-limiter}") boolean deactivateLimiter,
                            @Autowired(required = false) Optional<AccountManagementClient> accountManagementClient) {
        this.limitRepository = limitRepository;
        this.deactivateLimiter = deactivateLimiter;
        this.accountManagementClient = accountManagementClient;
    }

    /**
     * Fetch the plans and features of each plan from the account-management system.
     */
    @PostConstruct()
    public void init() {
        if (accountManagementClient.isEmpty()) {
            LOGGER.warn("Deactivating limiter - no account management client available");
            this.deactivateLimiter = true;
            return;
        }
        try {
            Map<String, List<FeatureTO>> response = accountManagementClient.orElseThrow(() -> new ServiceUnavailableException("account-management")).getPlanWithFeaturesForApp("DATAGYM").execute().body();

            if (response != null) {
                Arrays.stream(DataGymPlan.values()).forEach(dgPlan -> {
                    List<FeatureTO> planFeatures = response.get(dgPlan.toString());
                    if (planFeatures == null) {
                        throw new SystemException("Could not initialize limit service: Plan " + dgPlan.toString() + " not found!", null);
                    }
                    DataGymPlanDetails newPlan = new DataGymPlanDetails(dgPlan);
                    DataGymPlanDetailsMapper.map(planFeatures, newPlan);
                    datagymPlan.put(dgPlan, newPlan);
                });
            } else {
                throw new SystemException("Could not initialize limit service: no response from account-management!", null);
            }
        } catch (Exception e) {
            LOGGER.error("Error by fetching the limits from the account-management system!", e);
            throw new IllegalStateException("Error by fetching the limits from the account-management system!", e);
        }
    }

    @Override
    @AuthScope(all = {BASIC_SCOPE_TYPE})
    public void resetPricingPlan(@Valid LimitSetPricingPlanBindingModel planBindingModel) {
        String organisationId = planBindingModel.getOrganisationId();

        Limit limit = limitRepository.findByOrganisationId(organisationId).orElse(null);

        createOrUpdateLimitsInternal(limit, planBindingModel.getPricingPlanType(), organisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public LimitPricingPlanViewModel getLimitsByOrgId(String orgId) {
        //Permissions check
        DataGymSecurity.isAdminOrUser(orgId, true);

        // Get Limits by OrganisationId. If there aren't any Limits, create the Limits for the current Organisation
        Limit limitByOrganisationIdRequired = getLimitByOrganisationIdRequired(orgId);

        return LimitMapper.mapToLimitPricingPlanViewModel(limitByOrganisationIdRequired);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void increaseUsedProjectsCount(String organisationId) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int projectLimit = limitByOrganisationId.getProjectLimit();
        // Unlimited projects
        if (projectLimit == -1) {
            return;
        }

        int projectUsed = limitByOrganisationId.getProjectUsed();

        if (projectUsed + 1 > projectLimit) {
            throw new LimitException("Project", projectUsed + 1 + "", projectLimit + "", null);
        }

        limitByOrganisationId.setProjectUsed(projectUsed + 1);

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void decreaseUsedProjectsCount(String organisationId) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int projectUsed = limitByOrganisationId.getProjectUsed();

        if (projectUsed - 1 >= 0) {
            limitByOrganisationId.setProjectUsed(projectUsed - 1);
        }

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void increaseUsedLabelsCount(String organisationId) throws IOException {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int labelLimit = limitByOrganisationId.getLabelLimit();
        // Unlimited labels
        if (labelLimit == -1) {
            return;
        }

        int labelRemaining = limitByOrganisationId.getLabelRemaining();
        if (labelRemaining - 1 < 0) {
            throw new LimitException("Label", labelLimit + 1 + "", labelLimit + "", null);
        }

        limitByOrganisationId.setLabelRemaining(labelRemaining - 1);


        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void decreaseUsedLabelsCount(String organisationId) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int labelLimit = limitByOrganisationId.getLabelLimit();
        // Unlimited labels
        if (labelLimit == -1) {
            return;
        }
        int labelRemaining = limitByOrganisationId.getLabelRemaining();

        if (labelRemaining + 1 <= labelLimit) {
            limitByOrganisationId.setLabelRemaining(labelRemaining + 1);
        }

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void increaseUsedStorage(String organisationId, long fileSizeKB) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        long storageLimit = limitByOrganisationId.getStorageLimit();
        long storageUsed = limitByOrganisationId.getStorageUsed();

        if (storageUsed + fileSizeKB > storageLimit) {
            throw new LimitException("Storage", storageUsed + fileSizeKB + "", storageLimit + "", null);
        }

        limitByOrganisationId.setStorageUsed(storageUsed + fileSizeKB);

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void decreaseUsedStorage(String organisationId, long fileSizeKB) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }
        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        long storageUsed = limitByOrganisationId.getStorageUsed();

        if (storageUsed - fileSizeKB <= 0) {
            limitByOrganisationId.setStorageUsed(0);
        } else {
            limitByOrganisationId.setStorageUsed(storageUsed - fileSizeKB);
        }

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void increaseAiSegRemaining(String organisationId) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int aiSegLimit = limitByOrganisationId.getAiSegLimit();
        // Unlimited aiseg-calls
        if (aiSegLimit == -1) {
            return;
        }

        int aiSegRemaining = limitByOrganisationId.getAiSegRemaining();
        if (aiSegRemaining + 1 <= aiSegLimit) {
            limitByOrganisationId.setAiSegRemaining(aiSegRemaining + 1);
        }

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void decreaseAiSegRemaining(String organisationId) throws IOException {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int aiSegLimit = limitByOrganisationId.getAiSegLimit();

        // Unlimited aiseg-calls
        if (aiSegLimit == -1) {
            return;
        }

        int aiSegRemaining = limitByOrganisationId.getAiSegRemaining();
        if (aiSegRemaining - 1 < 0) {
            throw new LimitException("AiSeg", aiSegLimit + 1 + "", aiSegLimit + "", null);
        }

        limitByOrganisationId.setAiSegRemaining(aiSegRemaining - 1);

        limitRepository.save(limitByOrganisationId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void checkAiSegLimits(String organisationId) {
        // Deactivate pricing limits
        if (deactivateLimiter) {
            return;
        }

        // Permissions check
        DataGymSecurity.isAuthenticated();

        Limit limitByOrganisationId = getLimitByOrganisationIdRequired(organisationId);

        int aiSegLimit = limitByOrganisationId.getAiSegLimit();

        // Unlimited aiseg-calls
        if (aiSegLimit == -1) {
            return;
        }

        int aiSegRemaining = limitByOrganisationId.getAiSegRemaining();
        if (aiSegRemaining - 1 < 0) {
            throw new LimitException("AiSeg", aiSegLimit + 1 + "", aiSegLimit + "", null);
        }
    }

    /**
     * Get Limits by OrganisationId. If there aren't any Limits, create the Limits for the current Organisation
     */
    public Limit getLimitByOrganisationIdRequired(String organisationId) {
        Limit organisationLimit = limitRepository
                .findByOrganisationId(organisationId)
                .orElseGet(() -> createOrUpdateLimitsInternal(null, DataGymPlan.FREE_DEVELOPER, organisationId));

        // Set the team-pro plan if the open-core variant is applied
        if (organisationId.equals("open-core-dummy-org-sub")) {
            try {
                // Check if the field is present (only, when the security-mock module is applied)
                OidcUserInfo.class.getDeclaredField("isOpenCoreEnvironment");
                organisationLimit.setDataGymPlan(DataGymPlan.TEAM_PRO);
            } catch (NoSuchFieldException e) {
                // It is fine - nothing to do here
            }
        }
        return organisationLimit;
    }

    // “At 00:00 on day-of-month 1 in every month from January through December.”
    @Scheduled(cron = "0 0 0 1 1/1 *")
    public void resetFreePlans() {
        List<Limit> freePlans = limitRepository.findAllByDataGymPlan(DataGymPlan.FREE_DEVELOPER);
        freePlans.forEach(freePlan -> createOrUpdateLimitsInternal(freePlan, DataGymPlan.FREE_DEVELOPER, freePlan.getOrganisationId()));
    }

    /**
     * Sets or creates limits regarding to the pricing plan for a specific organisation
     *
     * @param limit           The specific {@link Limit} or <code>null</code>
     * @param pricingPlanType The specific pricing plan to set
     * @param organisationId  The specific organisationId
     * @return Instance of updated {@link Limit}
     */
    private Limit createOrUpdateLimitsInternal(Limit limit, DataGymPlan pricingPlanType, String organisationId) {
        long currentTimeMillis = System.currentTimeMillis();

        // Create a new Limit if not created yet
        if (limit == null) {
            limit = new Limit();
            limit.setOrganisationId(organisationId);
            limit.setProjectUsed(0);
            limit.setStorageUsed(0);
            limit.setTimestamp(currentTimeMillis);

            LOGGER.info("Initialized a new limit for the organisation with id: {}", organisationId);
        }

        if (organisationId.equals("open-core-dummy-org-sub")) {
            // Set the team-pro plan if the open-core variant is applied
            try {
                // Check if the field is present (only, when the security-mock module is applied)
                OidcUserInfo.class.getDeclaredField("isOpenCoreEnvironment");
                limit.setDataGymPlan(DataGymPlan.TEAM_PRO);
                limit.setProjectLimit(100);
                limit.setLabelLimit(50000);
                limit.setLabelRemaining(50000);
                limit.setStorageLimit(52428800);
                limit.setAiSegLimit(-1);
                limit.setAiSegRemaining(-1);
                limit.setApiAccess(true);
                limit.setExternalStorage(true);
                limit.setLastReset(currentTimeMillis);
            } catch (NoSuchFieldException e) {
                // It is fine - nothing to do here
            }
        } else {
            DataGymPlanDetails dataGymPlanDetails = datagymPlan.get(pricingPlanType);

            limit.setDataGymPlan(dataGymPlanDetails.getType());
            limit.setProjectLimit(dataGymPlanDetails.getProjects());
            limit.setLabelLimit(dataGymPlanDetails.getLabels());
            limit.setLabelRemaining(dataGymPlanDetails.getLabels());
            limit.setStorageLimit(dataGymPlanDetails.getStorage());
            limit.setAiSegLimit(dataGymPlanDetails.getAiseg());
            limit.setAiSegRemaining(dataGymPlanDetails.getAiseg());
            limit.setApiAccess(dataGymPlanDetails.isApiAccess());
            limit.setExternalStorage(dataGymPlanDetails.isExternalStorage());
            limit.setLastReset(currentTimeMillis);
        }


        LOGGER.info("Limit for organisation with id {} set to plan {}", organisationId, pricingPlanType);
        return limitRepository.save(limit);
    }
}
