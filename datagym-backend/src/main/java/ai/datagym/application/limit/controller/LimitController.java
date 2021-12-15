package ai.datagym.application.limit.controller;


import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/limit")
@Validated
public class LimitController {
    private final LimitService limitService;

    @Autowired
    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    @GetMapping("/{orgId}")
    public LimitPricingPlanViewModel getLimits(@PathVariable  String orgId){
       return this.limitService.getLimitsByOrgId(orgId);
    }
}
