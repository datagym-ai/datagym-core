package ai.datagym.application.externalAPI.service;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;

public final class ExternalApiMapper {

    private ExternalApiMapper() {
    }

    public static DatasetCreateBindingModel mapToDatasetCreateBindingModel(ExternalApiCreateDatasetBindingModel from) {
        DatasetCreateBindingModel to = new DatasetCreateBindingModel();

        to.setShortDescription(from.getShortDescription());
        to.setName(from.getName());

        return to;
    }
}
