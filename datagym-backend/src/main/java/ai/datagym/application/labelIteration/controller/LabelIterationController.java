package ai.datagym.application.labelIteration.controller;

import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/lcvalues")
public class LabelIterationController {

    private LcEntryValueService lcEntryValueService;

    @Autowired
    public LabelIterationController(LcEntryValueService lcEntryValueService) {
        this.lcEntryValueService = lcEntryValueService;
    }

    @GetMapping(value = "/{iterationId}/{mediaId}/{taskId}")
    public LabelIterationViewModel getLabelIterationValues(@PathVariable(value = "iterationId") String iterationId,
                                                           @PathVariable(value = "mediaId") String mediaId,
                                                           @PathVariable(value = "taskId") String taskId) {
        return lcEntryValueService.getLabelIterationValues(iterationId, mediaId, taskId);
    }

    @PutMapping(value = "/{iterationId}/{mediaId}/{taskId}")
    public LabelIterationViewModel updateLabelValues(@PathVariable(value = "iterationId") String iterationId,
                                                     @PathVariable(value = "mediaId") String mediaId,
                                                     @PathVariable(value = "taskId") String taskId,
                                                     @RequestBody @Valid List<LcEntryValueUpdateBindingModel> lcEntryUpdateBindingModelList) {
        return lcEntryValueService.updateLcEntryValues(iterationId, mediaId, taskId, lcEntryUpdateBindingModelList, false);
    }

    @PutMapping(value = "/{lcValueId}")
    public LcEntryValueViewModel updateSingleLabelValue(@PathVariable(value = "lcValueId") String lcValueId,
                                                        @RequestBody @Valid LcEntryValueUpdateBindingModel lcEntryUpdateBinding) {
        return lcEntryValueService.updateSingleLcEntryValue(lcValueId, lcEntryUpdateBinding);
    }


    @PutMapping(value = "/{lcValueId}/changeType")
    public LcEntryValueViewModel changeTypeOfSingleLabelValue(@PathVariable(value = "lcValueId") String lcValueId,
                                                              @RequestBody @Valid LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel) {
        return lcEntryValueService.changeTypeOfSingleLabelValue(lcValueId, lcEntryValueChangeValueClassBindingModel);
    }

    @PostMapping()
    public LcEntryValueViewModel createLabelValue(@RequestBody @Valid LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        return lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel);
    }

    @PostMapping(value = "/{lcEntryId}")
    public LcEntryValueViewModel createLabelValuesTree(@PathVariable(value = "lcEntryId") String lcEntryId,
                                                       @RequestBody @Valid LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        return lcEntryValueService.createLcEntryValueTree(lcEntryId, lcEntryValueCreateBindingModel);
    }

    @PostMapping(value = "/{configId}/{taskId}/classification")
    public List<LcEntryValueViewModel> createGlobalClassificationsValuesTree(@PathVariable(value = "configId") String configId,
                                                                             @PathVariable(value = "taskId") String taskId,
                                                                             @RequestBody @Valid LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        lcEntryValueCreateBindingModel.setLabelTaskId(taskId);
        return lcEntryValueService.createGlobalClassificationsValuesGetRoots(configId, lcEntryValueCreateBindingModel);
    }

    @DeleteMapping("/{lcValueId}")
    public void deleteLabelValue(@PathVariable(value = "lcValueId") String lcValueId) {
        lcEntryValueService.deleteLcValue(lcValueId);
    }

    /**
     * Creates {@link ai.datagym.application.labelIteration.entity.LcEntryValue}-objects
     * starting from a start point {@param lcEntryId} in the {@link ai.datagym.application.labelConfiguration.entity.LabelConfiguration}.
     * Creates {@link ai.datagym.application.labelIteration.entity.LcEntryValue}-objects for the starting LcEntry-Element and
     * all his children.
     * When the start element is not a root element in the LcEntryTree, a lcEntryParentId is required as parameter
     * in the {@param LcEntryValueExtendBindingModel} .
     */
    @PostMapping(value = "/{lcEntryId}/extend")
    public void extendValuesTree(@PathVariable(value = "lcEntryId") String lcEntryId,
                                 @RequestBody @Valid LcEntryValueExtendBindingModel lcEntryValueExtendBindingModel) {
        lcEntryValueService.extendValueTree(lcEntryId, lcEntryValueExtendBindingModel);
    }

    /**
     * Extends the current Iteration with initial LcEntryValue-Objects, if the LabelConfiguration were extended and
     * there are no initial LcEntryValue-Objects for the new LcEntries in the LabelConfiguration.
     * This will happen ONLY if there are already root-Values for the LcEntries.
     * Returns LabelIterationViewModel with all LcEntryValues for the current Configuration, Iteration and media.
     */
    @PostMapping(value = "/{configId}/extendAll")
    public LabelIterationViewModel extendAllConfigEntryValues(@PathVariable(value = "configId") String configId,
                                                              @RequestBody @Valid LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel) {
        return lcEntryValueService.extendAllConfigEntryValues(configId, lcEntryValueExtendAllBindingModel);
    }
}
