package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.List;

public interface LcEntryValueService {
    LabelIterationViewModel getLabelIterationValues(String iterationId, String mediaId, String taskId);

    LabelIterationViewModel updateLcEntryValues(String iterationId, String mediaId, String taskId, List<LcEntryValueUpdateBindingModel> lcEntryUpdateBindingModelList, boolean isCurrentProjectDummy);

    LcEntryValueViewModel updateSingleLcEntryValue(String lcValueId, LcEntryValueUpdateBindingModel lcEntryUpdateBinding);

    LcEntryValueViewModel createLcEntryValue(LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    void deleteLcValue(String lcValueId);

    /**
     * Gets called if a new instance of a geometry should be created. Creates the complete value tree of the geometry.
     *
     * @param lcEntryId                      The specific id of the label configuration element.
     * @param lcEntryValueCreateBindingModel Necessary information for the creation like Iteration, media id, TaskId, ...
     * @return Instance of {@link LcEntryValue}
     */
    LcEntryValue createLcEntryValueTreeGetRootInternal(String lcEntryId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    LcEntryValueViewModel createLcEntryValueTree(String lcEntryId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    /**
     * Creates or fetches (if existing) all global root geometry values.
     *
     * @param labelConfigurationId           The specific label configuration id
     * @param lcEntryValueCreateBindingModel The binding model which contains e.g. iteration id, media id, task id, etc.
     * @return List of <b>root</b> global calssification values mapped into a ViewModel
     */
    List<LcEntryValueViewModel> createGlobalClassificationsValuesGetRoots(String labelConfigurationId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    /**
     * Creates or fetches (if existing) all global root geometry values.
     *
     * @param labelConfigurationId           The specific label configuration id
     * @param lcEntryValueCreateBindingModel The binding model which contains e.g. iteration id, media id, task id, etc.
     * @return List of <b>root</b> global classification values
     */
    List<LcEntryValue> createGlobalClassificationValuesGetRootsInternal(String labelConfigurationId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    void extendValueTree(String lcEntryId, LcEntryValueExtendBindingModel lcEntryValueExtendBindingModel);

    LabelIterationViewModel extendAllConfigEntryValues(String configId, LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel);

    void validateEntryValuesBeforeTaskCompletion(String iterationId, String mediaId);

    void traverseAndValidateLcEntryValue(LcEntryValue node);

    LcEntryValue createLcEntryValueObject(String entryType);

    LcEntryValueViewModel changeTypeOfSingleLabelValue(String lcValueId, LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel);
}
