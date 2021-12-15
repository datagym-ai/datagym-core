package ai.datagym.application.limit.service;

import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.models.bindingModels.LimitSetPricingPlanBindingModel;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;

import java.io.IOException;

public interface LimitService {

    /**
     * Methods gets triggered by the account-management  if:
     * a) A new organisation is created in the account-management application
     * b) If a new billing period starts
     *
     * @param planBindingModel
     */
    void resetPricingPlan(LimitSetPricingPlanBindingModel planBindingModel);

    void increaseUsedProjectsCount(String organisationId);

    void decreaseUsedProjectsCount(String organisationId);

    void increaseUsedLabelsCount(String organisationId) throws IOException;

    void decreaseUsedLabelsCount(String organisationId);

    void increaseUsedStorage(String organisationId, long fileSizeKB);

    void decreaseUsedStorage(String organisationId, long fileSizeKB);

    void increaseAiSegRemaining(String organisationId);

    void decreaseAiSegRemaining(String organisationId) throws IOException;

    void checkAiSegLimits(String organisationId);

    Limit getLimitByOrganisationIdRequired(String organisationId);

    LimitPricingPlanViewModel getLimitsByOrgId(String orgId);
}
