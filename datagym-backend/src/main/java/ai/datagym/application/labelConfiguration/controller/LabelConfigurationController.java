package ai.datagym.application.labelConfiguration.controller;

import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.export.LcEntryExport;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/lconfig")
@Validated
public class LabelConfigurationController {
    private final LabelConfigurationService labelConfigurationService;

    @Autowired
    public LabelConfigurationController(LabelConfigurationService labelConfigurationService) {
        this.labelConfigurationService = labelConfigurationService;
    }

    @GetMapping(value = "/{id}")
    public LabelConfigurationViewModel getLabelConfiguration(@PathVariable("id") String id) {
        return labelConfigurationService.getLabelConfiguration(id);
    }

    @GetMapping(value = "/{id}/export")
    public List<LcEntryExport> exportLabelConfiguration(HttpServletResponse res,
                                                        @NotBlank @Length(min = 1) @PathVariable("id") String id) throws IOException {

        return labelConfigurationService.exportLabelConfiguration(id, res);
    }

    @PostMapping("/{id}/import")
    public LabelConfigurationViewModel importLabelConfiguration(@PathVariable(value = "id") String configId,
                                                                @RequestBody @Valid List<LcEntryUpdateBindingModel> entries) {

        return labelConfigurationService.importLabelConfiguration(configId, entries);
    }

    @PutMapping(value = "/{id}")
    public LabelConfigurationViewModel updateLabelConfiguration(@PathVariable(value = "id") String configId,
                                                                @RequestParam(value = "changeStatus", required = false, defaultValue = "true") boolean changeStatus,
                                                                @RequestBody @Valid List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList) {
        return labelConfigurationService.updateLabelConfiguration(configId, lcEntryUpdateBindingModelList, changeStatus);
    }

    @GetMapping(value = "/forbiddenKeywords")
    public List<String> getForbiddenKeyWords() {
        return labelConfigurationService.getForbiddenKeyWords();
    }

    @GetMapping("/hasConfigChanged")
    public LcConfigHasConfigChangedViewModel hasConfigChanged(@RequestParam(value = "lastChangedConfig") Long lastChangedConfig,
                                                              @RequestParam(value = "iterationId") String iterationId) {
        return labelConfigurationService.hasConfigChanged(lastChangedConfig, iterationId);
    }

    @DeleteMapping("/{configId}")
    public LcConfigDeleteViewModel clearConfig(@NotBlank @Length(min = 1) @PathVariable("configId") String configId) {
        return labelConfigurationService.clearConfig(configId);
    }
}
