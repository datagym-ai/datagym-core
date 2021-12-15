package ai.datagym.application.limit.models.bindingModels;

import ai.datagym.application.limit.entity.DataGymPlan;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LimitSetPricingPlanBindingModel {
    @NotNull
    @NotEmpty
    private String organisationId;

    @NotNull
    private DataGymPlan pricingPlanType;

    public LimitSetPricingPlanBindingModel() {
    }

    public LimitSetPricingPlanBindingModel(@NotNull @NotEmpty String organisationId, @NotNull DataGymPlan pricingPlanType) {
        this.organisationId = organisationId;
        this.pricingPlanType = pricingPlanType;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public DataGymPlan getPricingPlanType() {
        return pricingPlanType;
    }

    public void setPricingPlanType(DataGymPlan pricingPlanType) {
        this.pricingPlanType = pricingPlanType;
    }

    @Override
    public String toString() {
        return "LimitSetPricingPlanBindingModel{" +
                "organisationId='" + organisationId + '\'' +
                ", pricingPlanType=" + pricingPlanType +
                '}';
    }
}
