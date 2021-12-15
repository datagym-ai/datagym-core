package ai.datagym.application.externalAPI.service;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueChangeValueClassBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

public interface ExternalApiService {
    List<ExternalApiProjectViewModel> getAllProjects();

    List<ExternalApiDatasetViewModel> getAllDatasets();

    ExternalApiDatasetViewModel createDataset(ExternalApiCreateDatasetBindingModel createBindingModel,
                                              boolean createDummyDataset);

    List<UrlImageUploadViewModel> createImageUrl(String datasetId, Set<String> imageUrlSet, boolean dummyProjectImages);

    MediaViewModel createImageFile(String datasetId, String filename, ServletInputStream inputStream);

    void deleteMediaFile(String imageId, boolean deleteImage);

    void exportProjectLabels(String projectId, HttpServletResponse res) throws IOException;

    void exportVideoTask(String taskId, HttpServletResponse res) throws IOException;

    void streamSegmentationBitmap(String taskId, String lcEntryKey, HttpServletResponse response) throws IOException;

    void addDataset(String projectId, String datasetId);

    void removeDataset(String projectId, String datasetId);

    String streamMediaFile(String imageId, HttpServletResponse response, boolean downloadFile) throws IOException;

    ExternalApiDatasetViewModel getDataset(String id);

    ExternalApiSchemaValidationViewModel uploadPredictedValues(String projectId, ServletInputStream inputStream);

    LcConfigDeleteViewModel clearConfig(String configId);

    LabelConfigurationViewModel uploadLabelConfiguration(String configId, List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList);

    List<LabelTaskViewModel> getProjectTasks(String projectId, String filterSearchTerm, LabelTaskState labelTaskState, int maxResults);

    LabelModeDataViewModel getTask(String taskId) throws NoSuchMethodException, JsonProcessingException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException;

    void skipTask(String taskId) throws IOException;

    LabelTaskCompleteViewModel completeTask(String taskId, LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException;

    LcEntryValueViewModel createLcEntryValueTree(String lcEntryId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel);

    LcEntryValueViewModel updateSingleLcEntryValue(String lcValueId, LcEntryValueUpdateBindingModel lcEntryUpdateBinding);

    void deleteLcValue(String lcValueId);

    LcEntryValueViewModel changeTypeOfSingleLabelValue(String lcValueId, LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel);

    void prepare(String imageId);

    AiSegResponse calculate(AiSegCalculate aiSegCalculate);

    void finish(String imageId);
}
