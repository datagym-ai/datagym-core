package ai.datagym.application.management.controller;

import ai.datagym.application.limit.models.bindingModels.LimitSetPricingPlanBindingModel;
import ai.datagym.application.limit.service.LimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * This rest-interface is ONLY used by the account-management application
 */
@RestController
@RequestMapping(value = "/api/management")
public class ManagementController {

    @Autowired
    private LimitService limitService;

    @PostMapping("/limit/reset")
    public void resetPricingPlan(@RequestBody @Valid LimitSetPricingPlanBindingModel planBindingModel) {
        limitService.resetPricingPlan(planBindingModel);
    }
}
