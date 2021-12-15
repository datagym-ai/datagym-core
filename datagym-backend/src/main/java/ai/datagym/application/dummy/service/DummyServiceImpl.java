package ai.datagym.application.dummy.service;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.dummy.models.bindingModels.labelConfiguration.DummyConfigBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelConfiguration.DummyLcEntryBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyValueUpdateBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelTask.DummyLabelTaskBindingModel;
import ai.datagym.application.dummy.models.bindingModels.media.DummyMediaViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyDatasetViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyProjectBindingModel;
import ai.datagym.application.dummy.utils.LcEntryValueUtils;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.CustomUserTaskDummyEvent;
import ai.datagym.application.media.entity.UrlImage;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.service.ProjectService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DummyServiceImpl implements DummyService, ApplicationListener<CustomUserTaskDummyEvent> {
    public static final String DUMMY_PROJECT_CREATION_EXCEPTION = "dummy_project_creation";

    private String labelConfigurationId;

    private final ProjectService projectService;
    private final DatasetService datasetService;
    private final LabelTaskRepository labelTaskRepository;
    private final LcEntryValueService lcEntryValueService;
    private final LabelConfigurationService labelConfigurationService;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    public DummyServiceImpl(ProjectService projectService,
                            DatasetService datasetService,
                            LabelTaskRepository labelTaskRepository,
                            LcEntryValueRepository lcEntryValueRepository,
                            LcEntryValueService lcEntryValueService,
                            LabelConfigurationService labelConfigurationService,
                            ObjectMapper objectMapper,
                            ModelMapper modelMapper) {
        this.projectService = projectService;
        this.datasetService = datasetService;
        this.labelTaskRepository = labelTaskRepository;
        this.lcEntryValueService = lcEntryValueService;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.labelConfigurationService = labelConfigurationService;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
    }

    @AuthUser
    @AuthScope(any = {BASIC_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public void createDummyDataForOrg(String orgId) {
        // Find the organisation where the current user is admin
        if ("null".equals(orgId)) {
            LinkedHashMap<String, String> loggedInUserOrganisationsSorted = DataGymSecurity.getLoggedInUserOrganisations()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            for (Map.Entry<String, String> currentOrg : loggedInUserOrganisationsSorted.entrySet()) {
                if (currentOrg.getValue().equals("ADMIN")) {
                    orgId = currentOrg.getKey();
                    break;
                }
            }
        }

        //Permissions check
        DataGymSecurity.isAdmin(orgId, false);

        // Create Project and Datasets
        createProjectAndDatasetsInternal(orgId);
    }


    /**
     * Event Listener -  listen to CustomUserTaskDummyEvent's. If an Event is received, a new Dummy Project will be
     * created.
     *
     * @param event - a CustomUserTaskDummyEvent with the owner(OrganisationId) as parameter
     */

    @AuthUser
    @AuthScope(any = {BASIC_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public void onApplicationEvent(CustomUserTaskDummyEvent event) {
        String owner = event.getOwner();

        createDummyDataForOrg(owner);
    }

    @Override
    public void createProjectAndDatasetsInternal(String orgId) {

        // Check if projectName is Unique
        String projectName = DUMMY_PROJECT_PLACEHOLDER;
        checkIfProjectNamesIsUnique(orgId, projectName);

        DummyProjectBindingModel dummyProjectBindingModel;
        DummyDatasetViewModel[] dummyDatasetViewModel;
        DummyLabelTaskBindingModel[] dummyLabelTaskBindingModel;

        try {
            // Get the URL of Project.json
            URL projectResourceUrl = DummyServiceImpl.class
                    .getClassLoader()
                    .getResource("Dummy_Project_JSON/Project.json");

            // Get the URL of LabelTasks.json
            URL datasetsUrl = DummyServiceImpl.class
                    .getClassLoader()
                    .getResource("Dummy_Project_JSON/Datasets.json");

            // Get the URL of LabelTasks.json
            URL labelTasksResourceUrl = DummyServiceImpl.class
                    .getClassLoader()
                    .getResource("Dummy_Project_JSON/LabelTasks.json");

            // Parse JSON-File with Values for the current Project
            dummyProjectBindingModel = objectMapper
                    .readValue(Objects.requireNonNull(projectResourceUrl), DummyProjectBindingModel.class);

            // Parse JSON-File with Values for the Datasets from the Project
            dummyDatasetViewModel = objectMapper
                    .readValue(Objects.requireNonNull(datasetsUrl), DummyDatasetViewModel[].class);

            // Parse JSON-File with Values for the LabelTasks
            dummyLabelTaskBindingModel = objectMapper
                    .readValue(Objects.requireNonNull(labelTasksResourceUrl), DummyLabelTaskBindingModel[].class);

        } catch (Exception e) {
            throw new GenericException(DUMMY_PROJECT_CREATION_EXCEPTION, null, null);
        }

        // Create Dummy Project
        String projectDescription = dummyProjectBindingModel.getDescription();
        String projectShortDescription = dummyProjectBindingModel.getShortDescription();

        // Create dummyProjectCreateBindingModel
        ProjectCreateBindingModel dummyProjectCreateBindingModel =
                createProjectCreateBindingModel(orgId, projectName, projectShortDescription, projectDescription);

        // Create Project
        ProjectViewModel dummyProject = projectService.createProject(dummyProjectCreateBindingModel, true);

        String dummyProjectId = dummyProject.getId();
        String currentLabelConfigurationId = dummyProject.getLabelConfigurationId();

        // set the Value of the current ConfigId
        this.setLabelConfigurationId(currentLabelConfigurationId);

        // Create LabelConfigurationEntries for the current Project
        LabelConfigurationViewModel testProjectLabelConfigEntries = createTestProjectLabelConfigEntries();

        int datasetCount = 1;
        // Iterate over the datasets
        for (DummyDatasetViewModel dataset : dummyDatasetViewModel) {
            String datasetName;

            if (datasetCount == 1) {
                datasetName = DUMMY_DATASET_ONE_PLACEHOLDER;
                datasetCount++;
            } else if (datasetCount == 2) {
                datasetName = DUMMY_DATASET_TWO_PLACEHOLDER;
                datasetCount++;
            } else {
                datasetName = dataset.getName();
            }

            String datasetShortDescription = dataset.getShortDescription();

            // Check if datasetName is Unique
            checkIfDatasetNamesIsUnique(orgId, datasetName);

            // Create DatasetCreateBindingModel
            DatasetCreateBindingModel currentDatasetCreateBindingModel =
                    createDatasetCreateBindingModel(orgId, datasetName, datasetShortDescription);

            DatasetViewModel currentDummyDataset = datasetService.createDataset(currentDatasetCreateBindingModel, true);

            String currentDatasetId = currentDummyDataset.getId();

            // Add current Dataset to the Project
            projectService.addDataset(dummyProjectId, currentDatasetId);

            List<DummyMediaViewModel> mediaFromCurrentDataset = dataset.getMedia();

            Set<String> datasetMediaUrls = new HashSet<>();

            // Create UrlImages for current Dataset
            for (DummyMediaViewModel currentImageViewModel : mediaFromCurrentDataset) {
                String url = currentImageViewModel.getUrl();
                if (url != null) {
                    datasetMediaUrls.add(url);
                }
            }

            // Add all Images to the current Dataset
            addUrlImagesToDataset(currentDatasetId, datasetMediaUrls);

            // Get all Tasks from the Dummy Project and current Dataset
            List<LabelTask> tasksFromCurrentDataset = labelTaskRepository
                    .findAllByProjectIdAndDatasetId(dummyProjectId, currentDatasetId);

            // Create LcEntryValues for the Geometries and Classifications for all LabelTasks of the current Dataset
            createTestProjectLabelGeometriesAndClassifications(tasksFromCurrentDataset, testProjectLabelConfigEntries, dummyLabelTaskBindingModel);
        }
    }

    /**
     * Create LabelConfigurationEntries for the current Project
     */
    private LabelConfigurationViewModel createTestProjectLabelConfigEntries() {
        List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList = new ArrayList<>();

        DummyConfigBindingModel dummyConfigBindingModel = new DummyConfigBindingModel();

        try {
            // Get the URL of ProjectConfiguration.json
            URL projectConfigJson = DummyServiceImpl.class
                    .getClassLoader()
                    .getResource("Dummy_Project_JSON/ProjectConfiguration.json");

            // Parse JSON-File with Values for the Label Configuration of the current Project
            dummyConfigBindingModel = objectMapper
                    .readValue(Objects.requireNonNull(projectConfigJson), DummyConfigBindingModel.class);

        } catch (Exception e) {
            throw new GenericException(DUMMY_PROJECT_CREATION_EXCEPTION, null, null);
        }

        List<DummyLcEntryBindingModel> entries = dummyConfigBindingModel.getEntries();

        for (DummyLcEntryBindingModel entry : entries) {
            LcEntryUpdateBindingModel lcEntryUpdateBindingModel = modelMapper.map(entry, LcEntryUpdateBindingModel.class);

            lcEntryUpdateBindingModelList.add(lcEntryUpdateBindingModel);
        }

        // Create Label Configuration Entries
        return labelConfigurationService.updateLabelConfiguration(labelConfigurationId, lcEntryUpdateBindingModelList, true);
    }

    /**
     * Create LcEntryValues for the Geometries and Classifications for all LabelTasks of the current Dataset
     */
    private void createTestProjectLabelGeometriesAndClassifications(List<LabelTask> tasksFromCurrentDataset,
                                                                    LabelConfigurationViewModel testProjectLabelConfigEntries,
                                                                    DummyLabelTaskBindingModel[] dummyLabelTaskBindingModel) {

        String loggedInUserId = DataGymSecurity.getLoggedInUserId();
        List<LcEntryViewModel> entries = testProjectLabelConfigEntries.getEntries();

        // Iterate over all LabelTasks from current Dataset and check if there are any LcEntryValues in DummyValueUpdateBindingModel
        // for the current Task. If so, add this LCEntryValues to the current LabelTask
        for (LabelTask currentLabelTaskFromDataset : tasksFromCurrentDataset) {

            UrlImage urlImage = (UrlImage) currentLabelTaskFromDataset.getMedia();
            String currentTaskImageUrl = urlImage.getUrl();

            // Check if Task with "currentTaskImageUrl" exists in the LabelTasks.json
            Optional<DummyLabelTaskBindingModel> optionalTaskWithImageUrl = Arrays.stream(dummyLabelTaskBindingModel)
                    .filter(labelTaskBindingModel -> labelTaskBindingModel.getMedia().getUrl().equals(currentTaskImageUrl))
                    .findFirst();

            //If Task with "currentTaskImageUrl" is present in DummyLabelTaskBindingModel, get this Task and update it
            // with the Values from the DummyValueUpdateBindingModel
            if (optionalTaskWithImageUrl.isPresent()) {
                DummyLabelTaskBindingModel taskBindingModel = optionalTaskWithImageUrl.get();

                List<DummyValueUpdateBindingModel> currentTaskEntryValues = taskBindingModel.getLabelIteration().getEntryValues();

                String iterationId = currentLabelTaskFromDataset.getLabelIteration().getId();
                String mediaId = currentLabelTaskFromDataset.getMedia().getId();

                String labelTaskState = taskBindingModel.getLabelTaskState();

                // Set LabelTaskState of the current Task
                currentLabelTaskFromDataset.setLabelTaskState(LabelTaskState.valueOf(labelTaskState));

                // Set Labeler of the current Task as the current LoggedIn User, if LabelTaskState meets the conditions
                if (labelTaskState.equals(LabelTaskState.IN_PROGRESS.name()) || labelTaskState.equals(LabelTaskState.COMPLETED.name()) ||
                        labelTaskState.equals(LabelTaskState.SKIPPED.name()) || labelTaskState.equals(LabelTaskState.REVIEWED.name())) {
                    currentLabelTaskFromDataset.setLabeler(loggedInUserId);
                }

                List<LcEntryValueUpdateBindingModel> updateBindingModels = new ArrayList<>();

                String currentLabelTaskId = currentLabelTaskFromDataset.getId();

                // Iterate over all RootValuesBindingModels and create LcEntryValues
                for (DummyValueUpdateBindingModel entryValue : currentTaskEntryValues) {

                    LcEntryValueUpdateBindingModel currentValue = modelMapper.map(entryValue, LcEntryValueUpdateBindingModel.class);

                    String entryKeyLcEntry = entryValue.getEntryKeyLcEntry();

                    // Create LcEntryValueTree with placeholder Entities for the Values
                    LcEntryValueViewModel valuesTreeForCurrentTask = createValuesTreeForCurrentTask(currentLabelTaskFromDataset, entries, entryKeyLcEntry);

                    // Set the id's for the LcEntry and LcEntryValue in the LcEntryValueUpdateBindingModel
                    fillIdsForTheValuesOfCurrentValueTree(valuesTreeForCurrentTask, currentValue, currentLabelTaskId);

                    updateBindingModels.add(currentValue);
                }

                // Flush changes to be able to access the ids
                lcEntryValueRepository.flush();

                // Update ALL values from current Task
                lcEntryValueService.updateLcEntryValues(iterationId, mediaId, currentLabelTaskId, updateBindingModels, true);
            }
        }
    }

    /**
     * Set the id's for the LcEntry and LcEntryValue in the LcEntryValueUpdateBindingModel
     **/
    private void fillIdsForTheValuesOfCurrentValueTree(LcEntryValueViewModel node, LcEntryValueUpdateBindingModel currentValue, String currentLabelTaskId) {
        if (node != null) {
            String lcValueId = node.getId();
            String lcEntryId = node.getLcEntryId();

            currentValue.setId(lcValueId);
            currentValue.setLcEntryId(lcEntryId);
            currentValue.setLabelTaskId(currentLabelTaskId);
        }

        List<LcEntryValueViewModel> childrenValuePlaceholder = Objects.requireNonNull(node).getChildren();
        List<LcEntryValueUpdateBindingModel> childrenUpdateBindingModel = currentValue.getChildren();

        if (!childrenValuePlaceholder.isEmpty() && !childrenUpdateBindingModel.isEmpty()) {
            for (int i = 0; i < childrenValuePlaceholder.size(); i++) {
                LcEntryValueViewModel currentValuePlaceHolder = childrenValuePlaceholder.get(i);
                LcEntryValueUpdateBindingModel currentUpdateBindingModel = childrenUpdateBindingModel.get(i);

                fillIdsForTheValuesOfCurrentValueTree(currentValuePlaceHolder, currentUpdateBindingModel, currentLabelTaskId);
            }
        }
    }

    private LcEntryValueViewModel createValuesTreeForCurrentTask(LabelTask labelTask, List<LcEntryViewModel> entries, String entryKey) {
        Optional<LcEntryViewModel> entry = entries.stream().filter(entrie -> entrie.getEntryKey().equals(entryKey)).findFirst();

        if (entry.isPresent()) {
            LcEntryViewModel jetPolygonLcEntryViewModel = entry.get();
            String lcEntryId = jetPolygonLcEntryViewModel.getId();
            String mediaId = labelTask.getMedia().getId();
            String iterationId = labelTask.getLabelIteration().getId();
            String lcEntryValueParentId = jetPolygonLcEntryViewModel.getLcEntryParentId();
            String labelTaskId = labelTask.getId();

            LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createLcEntryValueCreateBindingModel
                    (lcEntryId, iterationId, mediaId, lcEntryValueParentId, labelTaskId);

            return lcEntryValueService
                    .createLcEntryValueTree(lcEntryId, lcEntryValueCreateBindingModel);
        }

        throw new GenericException(DUMMY_PROJECT_CREATION_EXCEPTION, null, null);
    }

    // Check if projectName, firstDatasetName and secondDatasetName are Unique
    private void checkIfProjectNamesIsUnique(String orgId, String projectName) {
        boolean projectNameUnique = projectService.isProjectNameUniqueAndDeletedFalse(projectName, orgId);

        if (!projectNameUnique) {
            throw new AlreadyExistsException("Project", "name", projectName);
        }
    }

    // Check if projectName, firstDatasetName and secondDatasetName are Unique
    private void checkIfDatasetNamesIsUnique(String orgId, String datasetName) {
        boolean firstDatasetNameUnique = datasetService.isDatasetNameUniqueAndDeletedFalse(datasetName, orgId);

        if (!firstDatasetNameUnique) {
            throw new AlreadyExistsException("Dataset", "name", datasetName);
        }
    }

    private List<UrlImageUploadViewModel> addUrlImagesToDataset(String datasetId, Set<String> datasetImageUrls) {
        // Create the Url-Images for the current Dataset
        return datasetService.createImagesByShareableLink(datasetId, datasetImageUrls, true);
    }

    private ProjectCreateBindingModel createProjectCreateBindingModel(String orgId, String projectName, String projectShortDescription, String description) {
        ProjectCreateBindingModel projectCreateBindingModel = new ProjectCreateBindingModel();
        projectCreateBindingModel.setOwner(orgId);
        projectCreateBindingModel.setName(projectName);
        projectCreateBindingModel.setShortDescription(projectShortDescription);
        projectCreateBindingModel.setDescription(description);

        return projectCreateBindingModel;
    }

    private DatasetCreateBindingModel createDatasetCreateBindingModel(String orgId, String name, String shortDescription) {
        DatasetCreateBindingModel datasetCreateBindingModel = new DatasetCreateBindingModel();

        datasetCreateBindingModel.setOwner(orgId);
        datasetCreateBindingModel.setName(name);
        datasetCreateBindingModel.setShortDescription(shortDescription);

        return datasetCreateBindingModel;
    }

    public String getLabelConfigurationId() {
        return labelConfigurationId;
    }

    public void setLabelConfigurationId(String labelConfigurationId) {
        this.labelConfigurationId = labelConfigurationId;
    }

}
