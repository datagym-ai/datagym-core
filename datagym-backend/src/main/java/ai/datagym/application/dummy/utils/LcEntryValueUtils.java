package ai.datagym.application.dummy.utils;

import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;


public class LcEntryValueUtils {
    public static LcEntryValueCreateBindingModel createLcEntryValueCreateBindingModel(
            String lcEntryId,
            String iterationId,
            String mediaId,
            String parentId,
            String labelTaskId) {
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();

        lcEntryValueCreateBindingModel.setLcEntryId(lcEntryId);
        lcEntryValueCreateBindingModel.setIterationId(iterationId);
        lcEntryValueCreateBindingModel.setMediaId(mediaId);
        lcEntryValueCreateBindingModel.setLcEntryValueParentId(parentId);
        lcEntryValueCreateBindingModel.setLabelTaskId(labelTaskId);

        return lcEntryValueCreateBindingModel;
    }
}
