package ai.datagym.application.externalAPI.controller;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.externalAPI.service.ExternalApiService;
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
import com.eforce21.lib.exception.Detail;
import com.eforce21.lib.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1")
@Validated
public class ExternalApiController {
    private final ExternalApiService externalApiService;

    @Autowired
    public ExternalApiController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    @GetMapping("/project")
    public List<ExternalApiProjectViewModel> getAllProjects() {
        return externalApiService.getAllProjects();
    }

    @PostMapping("/project/{projectId}/prediction")
    public ExternalApiSchemaValidationViewModel uploadPredictedValues(HttpServletRequest httpServletRequest,
                                                                      @PathVariable(value = "projectId") String projectId) throws IOException {
        return externalApiService.uploadPredictedValues(projectId, httpServletRequest.getInputStream());
    }

    @GetMapping("/dataset")
    public List<ExternalApiDatasetViewModel> getAllDatasets() {
        return externalApiService.getAllDatasets();
    }

    @GetMapping("/dataset/{datasetId}")
    public ExternalApiDatasetViewModel getDataset(@PathVariable("datasetId") @NotNull String datasetId) {
        return externalApiService.getDataset(datasetId);
    }

    @PostMapping("/project/{projectId}/dataset/{datasetId}")
    public void addDataset(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId,
                           @PathVariable("datasetId") @NotBlank @Length(min = 1) String datasetId) {
        externalApiService.addDataset(projectId, datasetId);
    }

    @DeleteMapping("/project/{projectId}/dataset/{datasetId}/remove")
    public void removeDataset(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId,
                              @PathVariable("datasetId") @NotBlank @Length(min = 1) String datasetId) {
        externalApiService.removeDataset(projectId, datasetId);
    }

    @PostMapping("/dataset")
    public ExternalApiDatasetViewModel createDataset(@RequestBody @Valid ExternalApiCreateDatasetBindingModel createBindingModel) {
        return externalApiService.createDataset(createBindingModel, false);
    }

    @PostMapping("/media/{datasetId}/url")
    public List<UrlImageUploadViewModel> createImageUrl(@PathVariable("datasetId") String datasetId,
                                                        @RequestBody @Valid Set<String> imageUrlSet) {
        return externalApiService.createImageUrl(datasetId, imageUrlSet, false);
    }

    @PostMapping("/media/{datasetId}/file")
    public MediaViewModel createImageFile(HttpServletRequest req,
                                          @PathVariable("datasetId") String datasetId) throws IOException {
        String filename = req.getHeader("X-filename");

        ValidationException ve = new ValidationException();

        if (filename == null || filename.isEmpty()) {
            ve.addDetail(new Detail("Header: X-filename", "ex_val_empty"));
        }

        if (ve.hasDetails()) {
            throw ve;
        }

        return externalApiService.createImageFile(datasetId, filename, req.getInputStream());
    }

    // Sets isDeleted to true
    @DeleteMapping("/media/{mediaId}")
    public void deleteMedia(@NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId) {
        externalApiService.deleteMediaFile(mediaId, true);
    }

    // Get Image
    @GetMapping("/media/{mediaId}")
    public String streamMediaFile(HttpServletResponse response,
                                  @NotBlank @Length(min = 1) @PathVariable("mediaId") String mediaId,
                                  @RequestParam(value = "dl", defaultValue = "true") boolean downloadFile) throws IOException {
        return externalApiService.streamMediaFile(mediaId, response, downloadFile);
    }

    /**
     * Exports JSON-File with all {@link ai.datagym.application.labelIteration.entity.LcEntryValue}s of
     * all {@link ai.datagym.application.labelTask.entity.LabelTask}s with {@link ai.datagym.application.labelTask.entity.LabelTaskState} 'COMPLETED', 'REVIEWED'
     * or 'SKIPPED' in the current project{@param projectId}.
     */
    @GetMapping("/export/{projectId}")
    public void exportLabels(HttpServletResponse res,
                             @NotBlank @Length(min = 1) @PathVariable("projectId") String projectId)
            throws IOException {
        externalApiService.exportProjectLabels(projectId, res);
    }

    @GetMapping("/exportVideoTask/{taskId}")
    public void exportVideoTask(HttpServletResponse res,
                                @NotBlank @Length(min = 1) @PathVariable("taskId") String taskId) throws IOException {
        externalApiService.exportVideoTask(taskId, res);
    }

    /**
     * Streams the image segmentation bitmap.
     * <p>
     * Do not change the @GetMapping. It is also hard coded in the implementation of
     * {@link ai.datagym.application.project.service.ProjectService}
     */
    @GetMapping("/export/bitmap/{taskId}/{lcEntryKey}")
    public void streamSegmentationBitmap(HttpServletResponse response,
                                         @NotBlank @Length(min = 1) @PathVariable("taskId") String taskId,
                                         @NotBlank @Length(min = 1) @PathVariable("lcEntryKey") String lcEntryKey) throws IOException {

        lcEntryKey = URLDecoder.decode(lcEntryKey, StandardCharsets.UTF_8);

        externalApiService.streamSegmentationBitmap(taskId, lcEntryKey, response);
    }

    /**
     * Deletes all LcEntries from the current Configuration. The LcEntryValues, that are connected with this
     * Label Configuration, will be also deleted as consequence of the deleting the LcEntries.
     * The LabelConfiguration-Entity will NOT be deleted. The LabelTaskState of all Tasks with State 'COMPLETED'
     * or 'REVIEWED' will be set to 'WAITING_CHANGED'
     */
    @DeleteMapping("/config/{configId}")
    public LcConfigDeleteViewModel clearConfig(@NotBlank @Length(min = 1) @PathVariable("configId") String configId) {
        return externalApiService.clearConfig(configId);
    }

    @PutMapping("/config/{configId}")
    public LabelConfigurationViewModel uploadLabelConfiguration(@PathVariable(value = "configId") String configId,
                                                                @RequestBody @Valid List<LcEntryUpdateBindingModel> entries) {
        return externalApiService.uploadLabelConfiguration(configId, entries);
    }

    @GetMapping("/task/{projectId}/list")
    public List<LabelTaskViewModel> getAllProjectTasks(@PathVariable("projectId") @NotNull String projectId,
                                                       @RequestParam(value = "search", required = false) @Pattern(regexp = "^[a-zA-Z0-9_ -]*$") String filterSearchTerm,
                                                       @RequestParam(value = "state", required = false) LabelTaskState labelTaskState,
                                                       @RequestParam(value = "limit", required = false, defaultValue = "0") int maxResults) {
        return externalApiService.getProjectTasks(projectId, filterSearchTerm, labelTaskState, maxResults);
    }

    @GetMapping(value = "/task/{taskId}")
    public LabelModeDataViewModel getTask(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId) throws NoSuchMethodException, JsonProcessingException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return externalApiService.getTask(taskId);
    }

    @PutMapping("/task/{taskId}/skipTask")
    public void skipTask(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId) throws IOException {
        externalApiService.skipTask(taskId);
    }

    @PutMapping("/task/{taskId}/completeTask")
    public LabelTaskCompleteViewModel completeTask(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId,
                                                   @RequestBody @Valid LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException {
        return externalApiService.completeTask(taskId, labelTaskCompleteBindingModel);
    }

    @PostMapping(value = "/lcValue/{lcEntryId}")
    public LcEntryValueViewModel createLabelValuesTree(@PathVariable(value = "lcEntryId") String lcEntryId,
                                                       @RequestBody @Valid LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        return externalApiService.createLcEntryValueTree(lcEntryId, lcEntryValueCreateBindingModel);
    }

    @PutMapping(value = "/lcValue/{lcValueId}")
    public LcEntryValueViewModel updateSingleLabelValue(@PathVariable(value = "lcValueId") String lcValueId,
                                                        @RequestBody @Valid LcEntryValueUpdateBindingModel lcEntryUpdateBinding) {
        return externalApiService.updateSingleLcEntryValue(lcValueId, lcEntryUpdateBinding);
    }

    @DeleteMapping("/lcValue/{lcValueId}")
    public void deleteLabelValue(@PathVariable(value = "lcValueId") String lcValueId) {
        externalApiService.deleteLcValue(lcValueId);
    }

    @PutMapping(value = "/lcValue/{lcValueId}/changeType")
    public LcEntryValueViewModel changeTypeOfSingleLabelValue(@PathVariable(value = "lcValueId") String lcValueId,
                                                              @RequestBody @Valid LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel) {
        return externalApiService.changeTypeOfSingleLabelValue(lcValueId, lcEntryValueChangeValueClassBindingModel);
    }

    @PostMapping("/aiseg/{imageId}/prepare")
    public void prepareImage(@PathVariable("imageId") String imageId) {
        externalApiService.prepare(imageId);
    }

    @PostMapping("/aiseg/calculate")
    public AiSegResponse calculateImage(@RequestBody AiSegCalculate aiSegCalculate) {
        return externalApiService.calculate(aiSegCalculate);
    }

    @DeleteMapping("/aiseg/{imageId}/finish")
    public void finishImage(@PathVariable("imageId") String imageId) {
        externalApiService.finish(imageId);
    }
}
