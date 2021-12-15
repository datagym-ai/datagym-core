package ai.datagym.application.labelIteration.controller;

import ai.datagym.application.labelIteration.models.change.create.LcEntryChangeCreateBindingModel;
import ai.datagym.application.labelIteration.models.change.update.LcEntryChangeUpdateBindingModel;
import ai.datagym.application.labelIteration.models.change.viewModels.LcEntryChangeViewModel;
import ai.datagym.application.labelIteration.service.LcEntryValueChangeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/lcvalues/change")
public class LabelValueChangeController {

    private final LcEntryValueChangeService valueChangeService;

    public LabelValueChangeController(LcEntryValueChangeService valueChangeService) {
        this.valueChangeService = valueChangeService;
    }

    @PostMapping
    public LcEntryChangeViewModel createValueChange(@RequestBody @Valid LcEntryChangeCreateBindingModel changeCreate) {
        return valueChangeService.createValueChange(changeCreate);
    }

    @PutMapping("/{changeId}")
    public LcEntryChangeViewModel updateValueChange(@PathVariable(value = "changeId") String changeId,
                                                    @RequestBody @Valid LcEntryChangeUpdateBindingModel updateData) {
        return valueChangeService.updateValueChange(changeId, updateData);
    }

    @DeleteMapping("/{changeId}")
    public void deleteValueChange(@PathVariable(value = "changeId") String changeId) {
        valueChangeService.deleteValueChange(changeId);
    }
}
