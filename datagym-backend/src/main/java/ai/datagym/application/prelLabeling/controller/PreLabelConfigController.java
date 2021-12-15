package ai.datagym.application.prelLabeling.controller;

import ai.datagym.application.prelLabeling.models.bindingModels.PreLabelConfigUpdateBindingModel;
import ai.datagym.application.prelLabeling.models.viewModels.PreLabelInfoViewModel;
import ai.datagym.application.prelLabeling.service.PreLabelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/prelabelconfig")
@Validated
public class PreLabelConfigController {

    private final PreLabelConfigService preLabelConfigService;

    @Autowired
    public PreLabelConfigController(PreLabelConfigService preLabelConfigService) {
        this.preLabelConfigService = preLabelConfigService;
    }

    @GetMapping(value = "/{projectId}")
    public PreLabelInfoViewModel getPreLabelInfoByProject(@PathVariable(name = "projectId") String projectId) {
        return preLabelConfigService.getPreLabelInfoByProject(projectId);
    }

    @PutMapping(value = "/{projectId}")
    public PreLabelInfoViewModel updatePreLabelConfigByProject(
            @PathVariable(name = "projectId") String projectId,
            @RequestBody @Valid PreLabelConfigUpdateBindingModel preLabelConfigUpdateBindingModel) {
        return preLabelConfigService.updatePreLabelConfigByProject(projectId, preLabelConfigUpdateBindingModel);
    }
}
