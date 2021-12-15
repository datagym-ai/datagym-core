package ai.datagym.application.externalAPI.service;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import ai.datagym.application.export.service.ExportSegmentationService;
import ai.datagym.application.export.service.ExportVideoTaskService;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.entity.LabelSource;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueChangeValueClassBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValidation;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.service.MediaService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.project.service.ProjectService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;


@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ExternalApiServiceImpl implements ExternalApiService {
    private static final String ENTRY_TYPE_NOT_FOUND = "entry_type_not_found";
    private static final String ENTRY_TYPE = "entry type";
    private static final String ENTRY_VALUE_EXCEPTION = "entry_value_validation";

    private final ProjectService projectService;
    private final DatasetService datasetService;
    private final LcEntryValueService lcEntryValueService;
    private final MediaService mediaService;
    private final ProjectRepository projectRepository;
    private final LcEntryRepository lcEntryRepository;
    private final LabelTaskRepository labelTaskRepository;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final LabelConfigurationService labelConfigurationService;
    private final LabelConfigurationRepository labelConfigurationRepository;
    private final LabelTaskService labelTaskService;
    private final Optional<AiSegService> aiSegService;
    private final ExportSegmentationService exportSegmentationService;
    private final LcEntryValidation lcEntryValidation;
    private final ExportVideoTaskService exportVideoTaskService;

    public ExternalApiServiceImpl(ProjectService projectService,
                                  DatasetService datasetService,
                                  LcEntryValueService lcEntryValueService,
                                  MediaService mediaService,
                                  MediaRepository mediaRepositoryMock, ProjectRepository projectRepository,
                                  LcEntryRepository lcEntryRepository,
                                  LabelTaskRepository labelTaskRepository,
                                  LcEntryValueRepository lcEntryValueRepository,
                                  ObjectMapper objectMapper,
                                  ModelMapper modelMapper,
                                  LabelConfigurationService labelConfigurationService,
                                  LabelConfigurationRepository labelConfigurationRepository,
                                  LabelTaskService labelTaskService,
                                  @Autowired(required = false) Optional<AiSegService> aiSegService,
                                  ExportSegmentationService exportSegmentationService,
                                  LcEntryValidation lcEntryValidation,
                                  ExportVideoTaskService exportVideoTaskService) {
        this.projectService = projectService;
        this.datasetService = datasetService;
        this.lcEntryValueService = lcEntryValueService;
        this.mediaService = mediaService;
        this.lcEntryRepository = lcEntryRepository;
        this.labelTaskRepository = labelTaskRepository;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.modelMapper = modelMapper;
        this.projectRepository = projectRepository;
        this.objectMapper = objectMapper;
        this.labelConfigurationService = labelConfigurationService;
        this.labelConfigurationRepository = labelConfigurationRepository;
        this.labelTaskService = labelTaskService;
        this.aiSegService = aiSegService;
        this.exportSegmentationService = exportSegmentationService;
        this.lcEntryValidation = lcEntryValidation;
        this.exportVideoTaskService = exportVideoTaskService;
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public List<ExternalApiProjectViewModel> getAllProjects() {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        List<ProjectViewModel> projects = projectService.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();

        return projects.stream()
                .map(projectViewModel -> modelMapper.map(projectViewModel, ExternalApiProjectViewModel.class))
                .collect(Collectors.toList());
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public List<ExternalApiDatasetViewModel> getAllDatasets() {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        List<DatasetViewModel> datasets = datasetService.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();

        return datasets.stream()
                .map(projectViewModel -> modelMapper.map(projectViewModel, ExternalApiDatasetViewModel.class))
                .collect(Collectors.toList());
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public ExternalApiDatasetViewModel getDataset(String id) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        DatasetViewModel dataset = datasetService.getDataset(id, true);

        return modelMapper.map(dataset, ExternalApiDatasetViewModel.class);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public ExternalApiDatasetViewModel createDataset(ExternalApiCreateDatasetBindingModel externalApiCreateDatasetBindingModel, boolean createDummyDataset) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        // Get the Organisation from the ApiToken
        String currentOrgFromApiToken = DataGymSecurity.getCurrentOrgFromApiToken();
        String datasetName = externalApiCreateDatasetBindingModel.getName();

        // Check if DatasetName is Unique
        checkIfDatasetNameIsUnique(datasetName, currentOrgFromApiToken);

        DatasetCreateBindingModel datasetCreateBindingModel = ExternalApiMapper
                .mapToDatasetCreateBindingModel(externalApiCreateDatasetBindingModel);

        datasetCreateBindingModel.setOwner(currentOrgFromApiToken);

        // Create new Dataset
        DatasetViewModel dataset = datasetService.createDataset(datasetCreateBindingModel, createDummyDataset);

        return modelMapper.map(dataset, ExternalApiDatasetViewModel.class);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public List<UrlImageUploadViewModel> createImageUrl(String datasetId, Set<String> imageUrlSet, boolean dummyProjectImages) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return datasetService.createImagesByShareableLink(datasetId, imageUrlSet, false);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public MediaViewModel createImageFile(String datasetId, String filename, ServletInputStream inputStream) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return datasetService.createImageFile(datasetId, filename, inputStream);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void deleteMediaFile(String mediaId, boolean deleteMedia) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        mediaService.deleteMediaFile(mediaId, true);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void addDataset(String projectId, String datasetId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        projectService.addDataset(projectId, datasetId);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void removeDataset(String projectId, String datasetId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        projectService.removeDataset(projectId, datasetId);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public String streamMediaFile(String mediaId, HttpServletResponse response, boolean downloadFile) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return mediaService.streamMediaFile(mediaId, response, downloadFile);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void exportProjectLabels(String projectId, HttpServletResponse res) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        projectService.exportProjectLabels(projectId, res);
    }

    @Override
    public void exportVideoTask(String taskId, HttpServletResponse res) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        exportVideoTaskService.exportSingleVideoTask(taskId, res);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LcConfigDeleteViewModel clearConfig(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return labelConfigurationService.clearConfig(configId);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LabelConfigurationViewModel uploadLabelConfiguration(String configId, List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        LabelConfiguration configById = findConfigById(configId);

        // If there are any LcEntries in the current Configuration, throw exception
        if (!configById.getEntries().isEmpty()) {
            throw new GenericException("config_not_cleared", null, null, configId);
        }

        return labelConfigurationService.updateLabelConfiguration(configId, lcEntryUpdateBindingModelList, true);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public List<LabelTaskViewModel> getProjectTasks(String projectId, String filterSearchTerm, LabelTaskState labelTaskState, int maxResults) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return projectService.getProjectTasks(projectId, filterSearchTerm, labelTaskState, maxResults);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LabelModeDataViewModel getTask(String taskId) throws NoSuchMethodException, JsonProcessingException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return labelTaskService.getTask(taskId);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void skipTask(String taskId) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        labelTaskService.skipTask(taskId);

    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LabelTaskCompleteViewModel completeTask(String taskId, LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return labelTaskService.completeTask(taskId, labelTaskCompleteBindingModel);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel createLcEntryValueTree(String lcEntryId, LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return lcEntryValueService.createLcEntryValueTree(lcEntryId, lcEntryValueCreateBindingModel);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel updateSingleLcEntryValue(String lcValueId, LcEntryValueUpdateBindingModel lcEntryUpdateBinding) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return lcEntryValueService.updateSingleLcEntryValue(lcValueId, lcEntryUpdateBinding);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void deleteLcValue(String lcValueId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        lcEntryValueService.deleteLcValue(lcValueId);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel changeTypeOfSingleLabelValue(String lcValueId, LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return lcEntryValueService.changeTypeOfSingleLabelValue(lcValueId, lcEntryValueChangeValueClassBindingModel);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void prepare(String imageId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).prepare(imageId, null, null);

    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public AiSegResponse calculate(AiSegCalculate aiSegCalculate) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        return aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).calculate(aiSegCalculate);
    }

    @AuthScope(any = {TOKEN_SCOPE_TYPE})
    @Override
    public void finish(String imageId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE);

        aiSegService.orElseThrow(() -> new ServiceUnavailableException("ai")).finish(imageId);
    }

    @Override
    public void streamSegmentationBitmap(String taskId, String lcEntryKey, HttpServletResponse response) throws IOException {
        //Permissions check
        /*
         * This is just a 'proxy method' redirecting the request directly to the
         * ExportSegmentationService. All permission checks are implemented there.
         */
        exportSegmentationService.streamSegmentationBitmap(taskId, lcEntryKey, response);
    }

    /**
     * Parses the input JSON-data for the LcEntryValues and saves them in the Database
     */
    @AuthScope(any = {TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public ExternalApiSchemaValidationViewModel uploadPredictedValues(String projectId, ServletInputStream inputStream) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE);

        // Check if Project exists
        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        JsonNode root;

        ExternalApiSchemaValidationViewModel externalApiSchemaValidationViewModel = new ExternalApiSchemaValidationViewModel();

        try {
            JsonParser jsonParser = objectMapper.getFactory().createParser(inputStream);
            root = objectMapper.readTree(jsonParser);

            if (root == null) {
                throw new GenericException("error_messages", null, null, "Unable to parse the content. No data available?");
            }

            JSONArray jsonToValidate = new JSONArray(new JSONTokener(root.toPrettyString()));

            InputStream inputStreamSchema = ExternalApiServiceImpl.class.getClassLoader().getResourceAsStream("JsonSchema/JsonSchemaImportValidation.json");
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStreamSchema));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(jsonToValidate);

            readJSON(root, projectById);

        } catch (ValidationException validationException) {
            StringBuilder errorMessages = new StringBuilder();
            Map<String, Set<String>> locationAndErrorMessages = new TreeMap<>(
                    (s1, s2) -> {
                        if (s1.length() > s2.length()) {
                            return 1;
                        } else if (s1.length() < s2.length()) {
                            return -1;
                        } else {
                            return s1.compareTo(s2);
                        }
                    });
            Set<String> errorMessagesList = new HashSet<>();

            if (validationException.getAllMessages().size() > 1) {
                validationException.getCausingExceptions().forEach(ce -> {
                    if (ce.getAllMessages().size() == 1) {
                        errorMessagesList.add(ce.getErrorMessage());
                        locationAndErrorMessages.put(ce.getPointerToViolation(), new HashSet<>(errorMessagesList));
                    } else {
                        Set<String> locations = new HashSet<>();

                        ce.getAllMessages().forEach(message -> locations.add(message.split(":")[0]));

                        locations.forEach(locationInSet -> {
                            ce.getAllMessages().forEach(message -> {
                                if (message.split(":")[0].equals(locationInSet)) {
                                    errorMessagesList.add(message.split(locationInSet + ": ")[1]);
                                }
                            });
                            locationAndErrorMessages.put(locationInSet, new HashSet<>(errorMessagesList));
                            errorMessagesList.clear();
                        });
                    }
                });
            } else {
                errorMessagesList.add(validationException.getErrorMessage());
                locationAndErrorMessages.put(validationException.getPointerToViolation(), new HashSet<>(errorMessagesList));
            }

            String validationFailedMessage = "Schema validation failed with " + locationAndErrorMessages.size() + " error(s)";

            errorMessages.append(validationFailedMessage);
            errorMessages.append(System.getProperty("line.separator"));
            errorMessages.append(System.getProperty("line.separator"));
            locationAndErrorMessages.forEach((location, errors) -> {
                errorMessages.append(location);
                errorMessages.append(System.getProperty("line.separator"));
                if (errors.size() > 1) {
                    errorMessages.append("Either: ");
                    errorMessages.append(System.getProperty("line.separator"));
                }
                errors.forEach(error -> {
                    errorMessages.append(error);
                    errorMessages.append(System.getProperty("line.separator"));
                });
                errorMessages.append(System.getProperty("line.separator"));
            });

            externalApiSchemaValidationViewModel.setErrorMessages(errorMessages.toString());
            return externalApiSchemaValidationViewModel;
        } catch (JsonProcessingException e) {
            StringBuilder errorMessage = new StringBuilder();
            String location = e.getLocation().toString().substring(e.getLocation().toString().indexOf("line"));
            location = location.substring(0, location.length() - 1);
            errorMessage.append(location);
            errorMessage.append(System.getProperty("line.separator"));
            errorMessage.append(e.getOriginalMessage());
            externalApiSchemaValidationViewModel.setErrorMessages(errorMessage.toString());
            return externalApiSchemaValidationViewModel;
        } catch (JSONException | IOException e) {
            throw new GenericException("error_messages", null, null, e.getLocalizedMessage());
        }
        externalApiSchemaValidationViewModel.setErrorMessages("");

        return externalApiSchemaValidationViewModel;
    }

    private void readJSON(JsonNode root, Project projectById) {

        String projectId = projectById.getId();
        String labelIterationId = projectById.getLabelIteration().getId();

        Iterator<JsonNode> globalAttributes = root.elements();
        while (globalAttributes.hasNext()) {
            JsonNode globalObject = globalAttributes.next();
            JsonNode internalMediaId = globalObject.path("internal_media_ID");
            JsonNode labels = globalObject.path("labels");
            JsonNode keepData = globalObject.path("keepData");
            JsonNode globalClassifications = globalObject.path("global_classifications");

            if (!internalMediaId.isMissingNode()) {
                LabelTask labelTask = checkIfLabelTaskIsCreatedForTheCurrentMedia(projectId,
                        internalMediaId.asText(),
                        labelIterationId);

                if (internalMediaId.isTextual() && labelTask != null) {
                    if (!keepData.isMissingNode() && !keepData.isBoolean()) {
                        throw new GenericException("upload_illegal_value",
                                                   null,
                                                   null,
                                                   keepData.asText("no boolean value"),
                                                   "keepData");
                    }
                    // If keepData is missing, then behave like it is set to <false>
                    if (!keepData.isMissingNode() && !keepData.asBoolean()) {
                        lcEntryValueRepository.deleteAllByLabelIterationIdAndMediaId(labelIterationId,
                                internalMediaId.asText());
                    }

                    readGlobalClassifications(globalClassifications, projectById, internalMediaId.asText(), labelTask);
                    readLabels(labels, projectById, internalMediaId.asText(), labelTask);
                } else {
                    throw new GenericException("error_messages",
                                               null,
                                               null,
                                               "The internal_media_ID doesn't match an existing task");
                }
            }
        }

    }

    /**
     * Read the LcEntryValues of the Global Classifications for the current media.
     * The LcEntryValues will be saved in the Database.
     */
    private void readGlobalClassifications(JsonNode globalClassifications, Project projectById, String currentMediaId, LabelTask labelTask) {

        // Create LcEntryValueCreateBindingModel for the creation of the Global Classifications
        String labelIterationId = projectById.getLabelIteration().getId();
        String configId = projectById.getLabelConfiguration().getId();
        String labelTaskId = labelTask.getId();

        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();
        lcEntryValueCreateBindingModel.setMediaId(currentMediaId);
        lcEntryValueCreateBindingModel.setIterationId(labelIterationId);
        lcEntryValueCreateBindingModel.setLcEntryId(null);
        lcEntryValueCreateBindingModel.setLcEntryValueParentId(null);
        lcEntryValueCreateBindingModel.setLcEntryValueParent(null);
        lcEntryValueCreateBindingModel.setLabelTaskId(labelTaskId);

        // Create all Global Classifications or fetch them, if already created. This method returns a List of
        // all Root-GlobalClassificationsValues
        List<LcEntryValue> globalClassificationsValues = lcEntryValueService
                .createGlobalClassificationValuesGetRootsInternal(configId, lcEntryValueCreateBindingModel);

        globalClassifications.fieldNames().forEachRemaining(classificationKey -> {
            LcEntryValue lcEntryValueViewModel = globalClassificationsValues.stream()
                    .filter(currentValueViewModel -> currentValueViewModel.getLcEntry().getEntryKey().equals(classificationKey))
                    .findFirst().orElseThrow(() -> new GenericException("wrong_entry_id", null, null, classificationKey));

            readCurrentValuesTree(lcEntryValueViewModel, globalClassifications.path(classificationKey));

        });
    }

    /**
     * Iterates over the current element {@param currentNode} and his children and
     * creates {@link LcEntryValue}'s from the Nodes of the InputStream
     */
    private void readCurrentValuesTree(LcEntryValue lcEntryValue, JsonNode currentClassification) {

        // Update the LcEntryValueKey of the current LcEntryValue with the Data from the InputStream
        updateCurrentLcEntryValue(currentClassification, lcEntryValue);

        currentClassification.elements().forEachRemaining(child -> {
            if (child.isObject()) {
                String nextClassificationLcEntryKey = child.fieldNames().next();
                LcEntry currentNodeLcEntry = lcEntryValue.getLcEntry();
                String configId = currentNodeLcEntry.getConfiguration().getId();
                String currentNodeLcEntryId = currentNodeLcEntry.getId();

                // Check if LcEntry with the LcEntryKey exists in the current Project
                getLcEntryByLcEntryKeyAndConfigId(nextClassificationLcEntryKey, configId, currentNodeLcEntryId);

                //Get the LcEntryValue with currentChildPropertyName and lcEntryValueParentId from the DB and set the child to this value
                LcEntryValue nextValue = getLcEntryValueByParentIdAndLcEntryKey(lcEntryValue.getId(), nextClassificationLcEntryKey);

                // Recursive call of the readCurrentValesTree() -Method
                JsonNode childNode = child.path(child.fieldNames().next());
                if (!childNode.isMissingNode()) readCurrentValuesTree(nextValue, childNode);
            }
        });
    }

    /**
     * Updates the LcEntryValueKey of the current LcEntryValue with the Data from the InputStream
     */
    private void updateCurrentLcEntryValue(JsonNode currentClassification, LcEntryValue lcEntryValue) {
        String type = lcEntryValue.getLcEntry().getType().name();

        // Set the LabelSource for the current Classification to 'API_UPLOAD'
        lcEntryValue.setLabelSource(LabelSource.API_UPLOAD);

        List<String> lcEntryValuesList = new ArrayList<>();


        currentClassification.elements().forEachRemaining(val -> {
            if (val.isValueNode() && val.isTextual()) {
                lcEntryValuesList.add(val.asText());
            }
        });


        switch (type) {
            case CHECKLIST:
                if (lcEntryValue instanceof LcEntryCheckListValue) {
                    LcEntryCheckListValue lcEntryCheckListValue = (LcEntryCheckListValue) lcEntryValue;
                    if (lcEntryValuesList.size() >= 1) {
                        lcEntryCheckListValue.setCheckedValues(lcEntryValuesList);
                        lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(lcEntryCheckListValue, false);
                    }
                }
                break;
            case SELECT:
                if (lcEntryValue instanceof LcEntrySelectValue) {
                    LcEntrySelectValue lcEntrySelectValue = (LcEntrySelectValue) lcEntryValue;
                    if (lcEntryValuesList.size() == 1) {
                        lcEntrySelectValue.setSelectKey(lcEntryValuesList.get(0));
                        lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(lcEntrySelectValue, false);
                    }
                }
                break;
            case FREETEXT:
                if (lcEntryValue instanceof LcEntryTextValue) {
                    LcEntryTextValue lcEntryTextValue = (LcEntryTextValue) lcEntryValue;
                    if (lcEntryValuesList.size() == 1) {
                        lcEntryTextValue.setText(lcEntryValuesList.get(0));
                        lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(lcEntryTextValue, false);
                    }
                }
                break;
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
        }
    }

    /**
     * Read the LcEntryValues for the current Geometry and his Classifications
     */
    private void readLabels(JsonNode labels, Project projectById, String currentMediaId, LabelTask labelTask) {

        String labelConfigurationId = projectById.getLabelConfiguration().getId();
        String labelIterationId = projectById.getLabelIteration().getId();

        labels.fieldNames().forEachRemaining((String geometryName) -> {
            String lcEntryId = getLcEntryByLcEntryKeyAndConfigId(geometryName, labelConfigurationId, null).getId();
            labels.path(geometryName).elements().forEachRemaining(geometry ->
                    readNextMediaValues(geometry, null, labelConfigurationId, currentMediaId, lcEntryId, labelIterationId, labelTask));
        });
        //At this point there are no more possible exceptions, so the jsonUpload was successful and is saved in the DB
        labelTask.setHasJsonUpload(true);
        labelTaskRepository.save(labelTask);
    }

    /**
     * Read the current Geometry-Data from the InputStream
     */
    private void readNextMediaValues(JsonNode geometry, LcEntryValue parent, String configId,
                                     String currentMediaId, String lcEntryId, String labelIterationId,
                                     LabelTask labelTask) {
        // Get parent id if available
        LcEntryValue lcEntryValue = createLcEntryValue(currentMediaId, lcEntryId, labelIterationId, labelTask, parent);

        Arrays.asList("polygon", "rectangle", "line", "point", "geometry").forEach((String geometryType) -> {
            JsonNode path = geometry.path(geometryType);
            if (!path.isMissingNode()) {
                readGeometryValues(path, lcEntryValue);
            }
        });
        readGeometryClassifications(geometry.path("classifications"), lcEntryValue);

        // Handle nested geometries
        if (parent == null) {
            geometry.path("nested_geometries").fieldNames().forEachRemaining((String geometryName) -> {
                String nestedGeoLcEntryId = getLcEntryByLcEntryKeyAndConfigId(geometryName, configId,
                        lcEntryValue.getLcEntry().getId()).getId();
                geometry.path("nested_geometries").path(geometryName).elements().forEachRemaining((JsonNode nestedGeometry) ->
                        readNextMediaValues(nestedGeometry, lcEntryValue, configId, currentMediaId,
                                nestedGeoLcEntryId, labelIterationId, labelTask));
            });
        }
    }

    /**
     * Read the Geometry-Values for the current Geometry
     */
    private void readGeometryValues(JsonNode geometry, LcEntryValue lcEntryValue) {

        // Set the LabelSource for the current Geometry to 'API_UPLOAD'
        lcEntryValue.setLabelSource(LabelSource.API_UPLOAD);

        geometry.elements().forEachRemaining(pointCoordinatesNode -> getGeometryCoordinates(pointCoordinatesNode, lcEntryValue));

        //Validate Values in the current LcEntryValue
        lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(lcEntryValue, false);
    }

    /**
     * Get the Coordinates of the Current Point and save them into the pointPojoList
     */
    private void getGeometryCoordinates(JsonNode pointCoordinatesNode, LcEntryValue lcEntryValue) {
        String lcEntryType = lcEntryValue.getLcEntry().getType().name();

        switch (lcEntryType) {
            case POLYGON:
                if (lcEntryValue instanceof LcEntryPolygonValue) {
                    LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;

                    // Read the Coordinates of the Current Point and save them in the pointPojoList and jsonUploadErrorPointPojos
                    PointPojo pointPojo = mapToPointPojo(pointCoordinatesNode, lcEntryValue);
                    lcEntryPolygonValue.getPoints().add(pointPojo);
                    pointPojo.setLcEntryPolygonValue(lcEntryPolygonValue);
                }
                break;
            case LINE:
                if (lcEntryValue instanceof LcEntryLineValue) {
                    LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;

                    // Read the Coordinates of the Current Point and save them into the pointPojoList and jsonUploadErrorPointPojos
                    PointPojo pointPojo = mapToPointPojo(pointCoordinatesNode, lcEntryValue);
                    lcEntryLineValue.getPoints().add(pointPojo);
                    pointPojo.setLcEntryLineValue(lcEntryLineValue);
                }
                break;
            case POINT:
                if (lcEntryValue instanceof LcEntryPointValue) {
                    LcEntryPointValue lcEntryPointValue = (LcEntryPointValue) lcEntryValue;

                    // Read the Coordinates of the Current Point and save them into the pointPojoList
                    PointPojo pointPojo = mapToPointPojo(pointCoordinatesNode, lcEntryValue);
                    lcEntryPointValue.setX(pointPojo.getX());
                    lcEntryPointValue.setY(pointPojo.getY());
                }
                break;
            case RECTANGLE:
                if (lcEntryValue instanceof LcEntryRectangleValue) {
                    // Read and fill the Values for the current Rectangle
                    setRectangleCoordinates(pointCoordinatesNode, lcEntryValue);
                }
                break;
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, lcEntryType);
        }
    }

    /**
     * Read the Coordinates of the Current Point and save them into the pointPojo
     */
    private PointPojo mapToPointPojo(JsonNode pointCoordinatesNode, LcEntryValue lcEntryValue) {
        PointPojo pointPojo = new PointPojo();

        pointCoordinatesNode.fieldNames().forEachRemaining(coordinate -> {
            JsonNode coordinateNode = pointCoordinatesNode.path(coordinate);
            if (coordinateNode.isMissingNode()) {
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
            }
            if (coordinateNode.isNumber()) {
                if (coordinate.equals("x")) {
                    pointPojo.setX(coordinateNode.asDouble());
                } else if (coordinate.equals("y")) {
                    pointPojo.setY(coordinateNode.asDouble());
                } else {
                    throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
                }

            } else if (coordinateNode.isTextual()) {
                if (coordinate.equals("x")) {
                    try {
                        pointPojo.setX(Double.parseDouble(coordinateNode.asText()));
                    } catch (Exception e) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
                    }
                } else if (coordinate.equals("y")) {
                    try {
                        pointPojo.setY(Double.parseDouble(coordinateNode.asText()));
                    } catch (Exception e) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
                    }
                } else {
                    throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
                }
            } else {
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
            }
        });

        return pointPojo;
    }

    /**
     * Read and fill the Values for the current Rectangle
     */
    private void setRectangleCoordinates(JsonNode pointCoordinatesNode, LcEntryValue lcEntryValue) {
        String lcEntryType = lcEntryValue.getLcEntry().getType().name();

        if (lcEntryValue instanceof LcEntryRectangleValue) {
            LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;

            pointCoordinatesNode.fieldNames().forEachRemaining(coordinate -> {
                JsonNode coordinateNode = pointCoordinatesNode.path(coordinate);
                if (coordinateNode.isMissingNode()) {
                    throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, lcEntryValue.getLcEntry().getType().name());
                }
                if (coordinateNode.isNumber()) {
                    switch (coordinate) {
                        case "x":
                            lcEntryRectangleValue.setX(coordinateNode.asDouble());
                            break;
                        case "y":
                            lcEntryRectangleValue.setY(coordinateNode.asDouble());
                            break;
                        case "w":
                            lcEntryRectangleValue.setWidth(coordinateNode.asDouble());
                            break;
                        case "h":
                            lcEntryRectangleValue.setHeight(coordinateNode.asDouble());
                            break;
                        default:
                            throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, lcEntryType);
                    }
                } else if (coordinateNode.isTextual()) {
                    try {
                        switch (coordinate) {
                            case "x":
                                lcEntryRectangleValue.setX(Double.parseDouble(coordinateNode.asText()));
                                break;
                            case "y":
                                lcEntryRectangleValue.setY(Double.parseDouble(coordinateNode.asText()));
                                break;
                            case "w":
                                lcEntryRectangleValue.setWidth(Double.parseDouble(coordinateNode.asText()));
                                break;
                            case "h":
                                lcEntryRectangleValue.setHeight(Double.parseDouble(coordinateNode.asText()));
                                break;
                            default:
                                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, lcEntryType);
                        }
                    } catch (Exception e) {
                        throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, lcEntryType);
                    }
                } else {
                    throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, lcEntryType);
                }
            });
        }
    }

    /**
     * Read the LcEntryValues of the Geometry Classifications for the current media.
     * The LcEntryValues will be saved in the Database.
     */
    private void readGeometryClassifications(JsonNode classifications, LcEntryValue lcEntryValue) {
        classifications.fieldNames().forEachRemaining(classificationKey -> {
            LcEntryValue currentValue = lcEntryValue.getChildren().stream().filter(currentChild -> currentChild.getLcEntry().getEntryKey()
                    .equals(classificationKey))
                    .findFirst()
                    .orElseThrow(() -> new GenericException("wrong_entry_id", null, null, classificationKey));
            readCurrentValuesTree(currentValue, classifications.path(classificationKey));
        });
    }

    /**
     * Create new LcEntryValue for the current Geometry
     */
    private LcEntryValue createLcEntryValue(String currentMediaId, String lcEntryId, String labelIterationId, LabelTask labelTask, LcEntryValue lcEntryValueParent) {
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();
        lcEntryValueCreateBindingModel.setMediaId(currentMediaId);
        lcEntryValueCreateBindingModel.setIterationId(labelIterationId);
        lcEntryValueCreateBindingModel.setLcEntryId(lcEntryId);
        lcEntryValueCreateBindingModel.setLabelTaskId(labelTask.getId());
        lcEntryValueCreateBindingModel.setLcEntryValueParentId(lcEntryValueParent != null ? lcEntryValueParent.getId() : null);
        lcEntryValueCreateBindingModel.setLcEntryValueParent(lcEntryValueParent);

        // Create Geometry with LcEntryKey equals to currentRootClassificationEntryKey
        return lcEntryValueService.createLcEntryValueTreeGetRootInternal(lcEntryId, lcEntryValueCreateBindingModel);
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    private LcEntryValue getLcEntryValue(String lcEntryValueId) {
        return lcEntryValueRepository.findById(lcEntryValueId)
                .orElseThrow(() -> new NotFoundException("Label Entry Value", "id", "" + lcEntryValueId));
    }

    private LcEntryValue getLcEntryValueByParentIdAndLcEntryKey(String lcEntryValueParentId, String lcEntryKey) {
        return lcEntryValueRepository.findByLcEntryValueParentIdAndLcEntryEntryKey(lcEntryValueParentId, lcEntryKey)
                .orElseThrow(() -> new NotFoundException("LabelEntryValue", "lcEntryKey:", "" + lcEntryKey));
    }

    private LcEntry getLcEntryByLcEntryKeyAndConfigId(String lcEntryKey, String configId, String lcEntryParentId) {
        return lcEntryRepository.findByEntryKeyAndConfigurationIdAndParentEntryId(lcEntryKey, configId, lcEntryParentId)
                .orElseThrow(() -> new GenericException("wrong_entry_id", null, null, lcEntryKey));
    }

    private LabelTask checkIfLabelTaskIsCreatedForTheCurrentMedia(String projectId, String mediaId, String labelIterationId) {
        return labelTaskRepository
                .findByProjectIdAndMediaIdAndLabelIterationIdAndLabelTaskType(projectId, mediaId, labelIterationId, LabelTaskType.DEFAULT)
                .orElseThrow(() -> new GenericException("json_upload_not_found_task", null, null));
    }

    private LabelConfiguration findConfigById(String configId) {
        return labelConfigurationRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Label Configuration", "id", "" + configId));
    }

    /**
     * Checks if dataset with this name {@param datasetName} in the current organisation {@param organisation} already exists
     */
    private void checkIfDatasetNameIsUnique(String datasetName, String organisation) {

        boolean isDataSetNameUnique = datasetService.isDatasetNameUniqueAndDeletedFalse(datasetName, organisation);

        if (!isDataSetNameUnique) {
            throw new AlreadyExistsException("Dataset", "name", datasetName);
        }
    }
}
