package ai.datagym.application.externalAPI.controller;

import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;
import ai.datagym.application.externalAPI.service.ApiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = "/api/token")
@Validated
public class ApiTokenController {
    public final ApiTokenService apiTokenService;

    @Autowired
    public ApiTokenController(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @GetMapping
    public List<ApiTokenViewModel> getApiTokenWhereUserAdmin(){
        return apiTokenService.getApiTokenWhereUserAdmin();
    }

    @PostMapping
    public ApiTokenViewModel createApiToken(@RequestBody @Valid ApiTokenCreateBindingModel apiTokenCreateBindingModel){
        return apiTokenService.createApiToken(apiTokenCreateBindingModel);
    }

    @DeleteMapping("/{id}")
    public ApiTokenViewModel deleteApiToken(@PathVariable @NotNull String id) {
        return apiTokenService.deleteApiTokenById(id);
    }
}
