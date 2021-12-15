package ai.datagym.application.limit.service;

import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;

public final class LimitMapper {
    private LimitMapper() {
    }

    public static LimitPricingPlanViewModel mapToLimitPricingPlanViewModel(Limit from) {
        LimitPricingPlanViewModel to = new LimitPricingPlanViewModel();

        to.setId(from.getId());
        to.setOrganisationId(from.getOrganisationId());
        to.setPricingPlanType(from.getDataGymPlan().toString());
        to.setProjectLimit(from.getProjectLimit());
        to.setProjectUsed(from.getProjectUsed());
        to.setLabelLimit(from.getLabelLimit());
        to.setLabelRemaining(from.getLabelRemaining());
        to.setStorageLimit(from.getStorageLimit());
        to.setStorageUsed(from.getStorageUsed());
        to.setAiSegLimit(from.getAiSegLimit());
        to.setAiSegRemaining(from.getAiSegRemaining());
        to.setApiAccess(from.isApiAccess());
        to.setExternalStorage(from.isExternalStorage());
        to.setTimestamp(from.getTimestamp());
        to.setLastReset(from.getLastReset());

        return to;
    }
}
