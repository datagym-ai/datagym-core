package ai.datagym.application.testUtils;

import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.models.bindingModels.LimitSetPricingPlanBindingModel;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;

import java.util.UUID;

public class LimitsUtils {
    public static final String LIMIT_ID = "TestId " + UUID.randomUUID();
    public static final String ORGANISATION_ID = "eforce21";
    private static final Long TIME = System.currentTimeMillis();

    public static Limit createTesLimit() {
        return new Limit() {{
            setId(LIMIT_ID);
            setOrganisationId("orgId");
            setDataGymPlan(DataGymPlan.FREE_DEVELOPER);
            setProjectLimit(10);
            setProjectUsed(1);
            setLabelLimit(100);
            setLabelRemaining(100);
            setStorageLimit(1000);
            setStorageUsed(0);
            setAiSegLimit(50);
            setAiSegRemaining(50);
            setApiAccess(true);
            setExternalStorage(true);
            setLastReset(TIME);
            setTimestamp(TIME);
        }};
    }

    public static LimitPricingPlanViewModel createTesLimitPricingPlanViewModel() {
        return new LimitPricingPlanViewModel() {{
            setId(LIMIT_ID);
            setOrganisationId("orgId");
            setPricingPlanType("FREE_DEVELOPER");
            setProjectLimit(10);
            setProjectUsed(1);
            setLabelLimit(100);
            setLabelRemaining(100);
            setStorageLimit(1000);
            setStorageUsed(0);
            setAiSegLimit(50);
            setAiSegRemaining(50);
            setApiAccess(true);
            setExternalStorage(true);
            setLastReset(TIME);
            setTimestamp(TIME);
        }};
    }

    public static LimitPricingPlanViewModel createTestLimitPricingPlanProViewModel() {
        return new LimitPricingPlanViewModel() {{
            setId(LIMIT_ID);
            setOrganisationId("orgId");
            setPricingPlanType(DataGymPlan.TEAM_PRO.name());
            setProjectLimit(10);
            setProjectUsed(1);
            setLabelLimit(100);
            setLabelRemaining(100);
            setStorageLimit(1000);
            setStorageUsed(0);
            setAiSegLimit(50);
            setAiSegRemaining(50);
            setApiAccess(true);
            setExternalStorage(true);
            setLastReset(TIME);
            setTimestamp(TIME);
        }};
    }

    public static LimitSetPricingPlanBindingModel createTesLimitSetPricingPlanBindingModel() {
        return new LimitSetPricingPlanBindingModel() {{
            setOrganisationId(ORGANISATION_ID);
            setPricingPlanType(DataGymPlan.FREE_DEVELOPER);
        }};
    }
}
