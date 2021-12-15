package ai.datagym.application.project.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetMediaStatusViewModel;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.errorHandling.AlreadyContainsException;
import ai.datagym.application.export.service.ExportService;
import ai.datagym.application.export.service.ExportVideoTaskService;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.viewModels.geometry.IGeometryCountViewModel;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskMapper;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.lablerRating.repo.LabelerRatingRepository;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.prelLabeling.entity.PreLabelConfiguration;
import ai.datagym.application.prelLabeling.repo.PreLabelConfigRepository;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectGeometryCountsViewModel;
import ai.datagym.application.project.models.viewModels.ProjectLabelCountByDayViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import ai.datagym.application.user.service.UserInformationService;
import ai.datagym.application.utils.GoogleString;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import io.micrometer.core.instrument.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ProjectServiceImpl implements ProjectService {
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private static final int DAYS_BEFORE_DELETE_PROJECT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private static final String DATASET_PLACEHOLDER = "Dataset";
    private static final String PROJECT_PLACEHOLDER = "Project";
    private static final String DUMMY_PROJECT_ERROR_PLACEHOLDER = "dummy_project_limit";
    private static final String DUMMY_DATASET_ERROR_PLACEHOLDER = "dummy_dataset_limit";

    private DateTimeFormatter dateFormat8 = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final ProjectRepository projectRepository;
    private final DatasetRepository datasetRepository;
    private final LabelConfigurationRepository labelConfigurationRepository;
    private final LabelConfigurationService labelConfigurationService;
    private final LabelIterationRepository labelIterationRepository;
    private final LabelTaskService labelTaskService;
    private final LabelTaskRepository labelTaskRepository;
    private final ExportService exportService;
    private final LimitService limitService;
    private final LabelerRatingRepository labelerRatingRepository;
    private final PreLabelConfigRepository preLabelConfigurationRepository;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final UserInformationService userInformationService;
    private final ExportVideoTaskService exportVideoTaskService;
    private MediaRepository mediaRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              DatasetRepository datasetRepository,
                              LabelConfigurationRepository labelConfigurationRepository,
                              LabelConfigurationService labelConfigurationService,
                              LabelIterationRepository labelIterationRepository,
                              LabelTaskService labelTaskService,
                              LabelTaskRepository labelTaskRepository,
                              ExportService exportService,
                              LimitService limitService,
                              LabelerRatingRepository labelerRatingRepository,
                              PreLabelConfigRepository preLabelConfigurationRepository,
                              LcEntryValueRepository lcEntryValueRepository,
                              UserInformationService userInformationService,
                              ExportVideoTaskService exportVideoTaskService,
                              MediaRepository mediaRepository) {
        this.projectRepository = projectRepository;
        this.datasetRepository = datasetRepository;
        this.labelConfigurationRepository = labelConfigurationRepository;
        this.labelConfigurationService = labelConfigurationService;
        this.labelIterationRepository = labelIterationRepository;
        this.labelTaskService = labelTaskService;
        this.labelTaskRepository = labelTaskRepository;
        this.exportService = exportService;
        this.limitService = limitService;
        this.labelerRatingRepository = labelerRatingRepository;
        this.preLabelConfigurationRepository = preLabelConfigurationRepository;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.userInformationService = userInformationService;
        this.exportVideoTaskService = exportVideoTaskService;
        this.mediaRepository = mediaRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public ProjectViewModel createProject(@Valid ProjectCreateBindingModel projectCreateBindingModel,
                                          boolean createDummyProject) {
        String projectOwner = projectCreateBindingModel.getOwner();
        String projectName = projectCreateBindingModel.getName();

        //Permissions check
        DataGymSecurity.isAdmin(projectOwner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(projectOwner).getPricingPlanType();

        if (projectCreateBindingModel.getMediaType() == MediaType.VIDEO &&
                !pricingPlanType.equals(DataGymPlan.TEAM_PRO.name())) {
            throw new GenericException("video_not_allowed", null, null);
        }


        if (!createDummyProject) {
            List<String> forbiddenKeyWords = labelConfigurationService.getForbiddenKeyWords();

            if (forbiddenKeyWords.contains(projectName)) {
                throw new GenericException("forbidden_keyword", null, null, projectName);
            }
        }

        if (!createDummyProject) {
            // Check Pricing Plan Limits
            limitService.increaseUsedProjectsCount(projectOwner);
        }

        long currentTime = System.currentTimeMillis();

        Project project = ProjectMapper.mapToProject(projectCreateBindingModel);
        project.setTimestamp(currentTime);

        LabelConfiguration labelConfiguration = new LabelConfiguration();
        labelConfiguration.setTimestamp(currentTime);
        labelConfiguration.setProject(project);
        LabelConfiguration savedLabelConfiguration = labelConfigurationRepository.saveAndFlush(labelConfiguration);

        LabelIteration labelIteration = new LabelIteration();
        labelIteration.setTimestamp(currentTime);
        labelIteration.setRun(1);
        labelIteration.setProject(project);
        LabelIteration savedLabelIteration = labelIterationRepository.saveAndFlush(labelIteration);

        PreLabelConfiguration preLabelConfiguration = new PreLabelConfiguration();
        preLabelConfiguration.setActivateState(false);
        preLabelConfiguration.setMappings(new ArrayList<>());
        preLabelConfiguration.setProject(project);
        PreLabelConfiguration savedPreLabelConfiguration = preLabelConfigurationRepository.saveAndFlush(
                preLabelConfiguration);

        project.setLabelConfiguration(savedLabelConfiguration);
        project.setLabelIteration(savedLabelIteration);
        project.setPreLabelConfiguration(savedPreLabelConfiguration);

        Project createdProject = projectRepository.saveAndFlush(project);

        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(createdProject, null);
        boolean projectExportable = isProjectExportable(createdProject.getId());

        projectViewModel.setExportable(projectExportable);

        // Metrics update - increaseProjectsCount
        increaseProjectsCount(projectName, projectOwner);

        return projectViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ProjectViewModel updateProject(String id, @Valid ProjectUpdateBindingModel projectUpdateBindingModel) {
        Project projectById = getProjectById(id);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        String projectName = projectById.getName();

        if (DUMMY_PROJECT_PLACEHOLDER.equals(projectName)) {
            throw new GenericException(DUMMY_PROJECT_ERROR_PLACEHOLDER, null, null);
        }

        String currentProjectName = projectById.getName();
        String newProjectName = projectUpdateBindingModel.getName();

        if (!currentProjectName.equals(newProjectName)) {
            Optional<Project> projectByName = projectRepository.findByNameAndDeletedFalseAndOwner(newProjectName,
                                                                                                  owner);

            if (projectByName.isPresent()) {
                throw new AlreadyExistsException(PROJECT_PLACEHOLDER, "name", newProjectName);
            }

            List<String> forbiddenKeyWords = labelConfigurationService.getForbiddenKeyWords();

            if (forbiddenKeyWords.contains(newProjectName)) {
                throw new GenericException("forbidden_keyword", null, null, newProjectName);
            }
        }

        Project project = ProjectMapper.mapToProject(projectUpdateBindingModel, projectById);

        long currentTime = System.currentTimeMillis();
        project.setTimestamp(currentTime);

        Project updatedProject = projectRepository.saveAndFlush(project);

        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(updatedProject,
                mediaRepository::countAllByDatasetsContainingAndDeletedFalse);
        boolean projectExportable = isProjectExportable(updatedProject.getId());
        projectViewModel.setExportable(projectExportable);

        return projectViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public ProjectViewModel getProject(String projectId) {
        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, true);

        if (!projectById.isDeleted()) {
            ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(projectById,
                    mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

            boolean projectExportable = isProjectExportable(projectById.getId());
            projectViewModel.setExportable(projectExportable);
            return projectViewModel;
        }

        throw new NotFoundException(PROJECT_PLACEHOLDER, "id", "" + projectId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public List<LabelTaskViewModel> getProjectTasks(String projectId, String filterSearchTerm,
                                                    LabelTaskState labelTaskState, int maxResults) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, true);

        boolean authenticatedAndHasCertainScope = DataGymSecurity.isAuthenticatedAndHasCertainScope(TOKEN_SCOPE_TYPE);

        if (authenticatedAndHasCertainScope && labelTaskState == null) {
            labelTaskState = LabelTaskState.WAITING;
        }

        if (!projectById.isDeleted()) {
            GoogleString googleString = new GoogleString(filterSearchTerm);

            List<LabelTask> allByProjectId = labelTaskRepository
                    .search(projectId, googleString, labelTaskState, false, maxResults);

            return allByProjectId.stream()
                    .filter(labelTask -> labelTask.getLabelTaskType() != LabelTaskType.BENCHMARK_SLAVE)
                    .map(LabelTaskMapper::mapToLabelTaskViewModel)
                    .map(o -> {
                        o.setLabeler(userInformationService.getUserName(o.getLabeler()));
                        return o;
                    })
                    .collect(Collectors.toList());
        }

        throw new NotFoundException(PROJECT_PLACEHOLDER, "id", "" + projectId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<ProjectViewModel> getAllProjects() {
        OauthUser user = SecurityContext.get();
        Map<String, String> orgs = user.orgs();

        List<ProjectViewModel> result = new ArrayList<>();

        for (Map.Entry<String, String> orgsEntry : orgs.entrySet()) {
            String currentUserOrg = orgsEntry.getKey();

            List<ProjectViewModel> currentOrgProjectViewModels = projectRepository
                    .findAllByDeletedIsFalseAndOwner(currentUserOrg).stream()
                    .map(currentProject -> {
                        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(currentProject,
                                mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

                        boolean projectExportable = isProjectExportable(currentProject.getId());
                        projectViewModel.setExportable(projectExportable);
                        return projectViewModel;
                    })
                    .collect(Collectors.toList());

            result.addAll(currentOrgProjectViewModels);
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<ProjectViewModel> getAllProjectsFromOrganisation(String orgId) {
        //Permissions check
        DataGymSecurity.isAdminOrUser(orgId, false);

        return projectRepository
                .findAllByDeletedIsFalseAndOwner(orgId).stream()
                .filter(project ->
                                !project.getName().equals(DUMMY_PROJECT_PLACEHOLDER))
                .map(currentProject -> {
                    ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(currentProject,
                            mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

                    boolean projectExportable = isProjectExportable(currentProject.getId());
                    projectViewModel.setExportable(projectExportable);
                    return projectViewModel;
                })
                .collect(Collectors.toList());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<ProjectViewModel> getSuitableProjectsForDataset(String datasetId) {
        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        DataGymSecurity.isAdminOrUser(datasetById.getOwner(), false);
        DataGymSecurity.isUserInCurrentOrg(datasetById.getOwner());

        return projectRepository.findAllByDeletedIsFalseAndOwnerAndDatasetsNotContainsAndMediaType(datasetById.getOwner(),
                                                                                                   datasetById,
                                                                                                   datasetById.getMediaType())
                .stream()
                .filter(project ->
                                !project.getName().equals(DUMMY_PROJECT_PLACEHOLDER))
                .map(currentProject -> {
                    ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(currentProject,
                            mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

                    boolean projectExportable = isProjectExportable(currentProject.getId());
                    projectViewModel.setExportable(projectExportable);
                    return projectViewModel;
                })
                .collect(Collectors.toList());

    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public List<ProjectViewModel> getAllProjectsFromOrganisationAndLoggedInUserIsAdmin() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        OauthUser user = SecurityContext.get();
        Map<String, String> orgs = user.orgs();

        List<ProjectViewModel> result = new ArrayList<>();

        for (Map.Entry<String, String> orgsEntry : orgs.entrySet()) {
            String currentUserOrg = orgsEntry.getKey();

            boolean isAdminInCurrentOrg = DataGymSecurity.checkIfUserIsAdmin(currentUserOrg, false);

            if (isAdminInCurrentOrg) {
                List<ProjectViewModel> currentOrgProjectViewModels = projectRepository
                        .findAllByDeletedIsFalseAndOwner(currentUserOrg).stream()
                        .map(currentProject -> {
                            ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(currentProject,
                                    mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

                            boolean projectExportable = isProjectExportable(currentProject.getId());
                            projectViewModel.setExportable(projectExportable);
                            return projectViewModel;
                        })
                        .collect(Collectors.toList());

                result.addAll(currentOrgProjectViewModels);
            }
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ProjectViewModel deleteProjectById(String id, boolean deleteProject) {
        Project projectById = getProjectById(id);

        //Permissions check
        String projectOwner = projectById.getOwner();
        String projectName = projectById.getName();
        DataGymSecurity.isAdmin(projectOwner, false);

        projectById.setDeleted(deleteProject);
        Long currentTime = null;

        if (deleteProject) {
            currentTime = System.currentTimeMillis();

            // Check Pricing Plan Limits
            if (!DUMMY_PROJECT_PLACEHOLDER.equals(projectName)) {
                limitService.decreaseUsedProjectsCount(projectOwner);
            }

        } else {
            // Check Pricing Plan Limits
            if (!DUMMY_PROJECT_PLACEHOLDER.equals(projectName)) {
                limitService.decreaseUsedProjectsCount(projectOwner);
            }

            List<Project> projectList = projectRepository.findAllByName(projectName);

            if (projectList.size() > 1) {
                String uuid = UUID.randomUUID().toString();
                if (projectName.length() > 90) {
                    projectName = projectName.substring(0, 89) + "_" + uuid;
                } else {
                    projectName = projectName + "_" + uuid;
                }

                projectById.setName(projectName);
            }
        }

        projectById.setDeleteTime(currentTime);

        Project deletedProject = projectRepository.saveAndFlush(projectById);
        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(deletedProject, null);

        boolean projectExportable = isProjectExportable(deletedProject.getId());
        projectViewModel.setExportable(projectExportable);
        return projectViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void permanentDeleteProjectFromDB(String id) {
        Project projectById = getProjectById(id);

        //Permissions check
        String projectOwner = projectById.getOwner();
        DataGymSecurity.isRoot(projectOwner, false);

        String projectName = projectById.getName();
        boolean deleted = projectById.isDeleted();

        // Check Pricing Plan Limits
        if (!DUMMY_PROJECT_PLACEHOLDER.equals(projectName) && !deleted) {
            limitService.decreaseUsedProjectsCount(projectOwner);
        }

        // Remove Relationship between Project and Dataset
        projectById.getDatasets().forEach((Dataset dataset) -> dataset.getProjects().remove(projectById));

        // Delete the project
        projectRepository.delete(projectById);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public void addDataset(String projectId, String datasetId) {
        Project projectById = getProjectById(projectId);
        Dataset datasetById = getDatasetById(datasetId);

        String projectName = projectById.getName();
        String datasetName = datasetById.getName();

        //Check if Project is Dummy_Project or Datasets are Dummy_Datasets
        checkIfDummyProjectOrDummyDataset(projectName, datasetName);

        //Permissions check
        String projectOwner = projectById.getOwner();
        String datasetOwner = datasetById.getOwner();

        DataGymSecurity.haveTheSameOwner(projectOwner, datasetOwner);
        DataGymSecurity.isAdmin(projectOwner, false);

        if (!projectById.getMediaType().equals(datasetById.getMediaType())) {
            throw new GenericException("mediatype_missmatch", null, null);
        }

        if (projectById.getDatasets().contains(datasetById)) {
            throw new AlreadyContainsException(PROJECT_PLACEHOLDER, DATASET_PLACEHOLDER);
        }

        projectById.getDatasets().add(datasetById);
        datasetById.getProjects().add(projectById);

        datasetById.getMedia().forEach(media -> {
            LabelTask labelTask = labelTaskService.createLabelTaskInternal(projectById,
                    media,
                    projectById.getLabelIteration());
            media.getLabelTasks().add(labelTask);
        });
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void removeDataset(String projectId, String datasetId) {
        Project projectById = getProjectById(projectId);
        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String projectOwner = projectById.getOwner();
        String datasetOwner = datasetById.getOwner();

        DataGymSecurity.haveTheSameOwner(projectOwner, datasetOwner);
        DataGymSecurity.isAdmin(projectOwner, false);

        if (!projectById.getDatasets().contains(datasetById)) {
            throw new GenericException("does_not_contain", null, null, PROJECT_PLACEHOLDER, DATASET_PLACEHOLDER);
        }

        projectById.getDatasets().remove(datasetById);
        datasetById.getProjects().remove(projectById);

        List<String> mediaIdsByDataset = mediaRepository.findMediaIdsByDataset(datasetId);

        // Delete all LabelTasks from the current Dataset
        labelTaskService.deleteAllLabelTasksFromDataset(projectById, mediaIdsByDataset);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public boolean isProjectNameUniqueAndDeletedFalse(String projectName, String owner) {
        return projectRepository
                .findByNameAndDeletedFalseAndOwner(projectName, owner).isEmpty();
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ProjectViewModel pinProject(String id, boolean setToPinned) {
        Project projectById = getProjectById(id);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, false);

        projectById.setPinned(setToPinned);

        long currentTime = System.currentTimeMillis();
        projectById.setTimestamp(currentTime);

        Project updatedProject = projectRepository.saveAndFlush(projectById);

        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(updatedProject, null);

        boolean projectExportable = isProjectExportable(updatedProject.getId());
        projectViewModel.setExportable(projectExportable);
        return projectViewModel;
    }

    @AuthUser
    @AuthScope(any = {TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public void exportProjectLabels(String projectId, HttpServletResponse httpServletResponse) throws IOException {
        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        if (!projectById.isDeleted()) {
            exportService.exportJsonLabelsByProject(projectById, httpServletResponse);
            return;
        }

        throw new NotFoundException(PROJECT_PLACEHOLDER, "id", "" + projectId);
    }

    @Override
    @AuthUser
    @AuthScope(any = {TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    public void exportVideoTask(String taskId, HttpServletResponse res) throws IOException {
        exportVideoTaskService.exportSingleVideoTask(taskId, res);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public ProjectViewModel updateReviewActivated(String projectId, boolean reviewActivated) {
        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Set reviewActivated
        projectById.setReviewActivated(reviewActivated);

        // Set currentTime
        long currentTime = System.currentTimeMillis();
        projectById.setTimestamp(currentTime);

        // Save Project
        Project updatedProject = projectRepository.save(projectById);

        ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(updatedProject, null);
        boolean projectExportable = isProjectExportable(updatedProject.getId());
        projectViewModel.setExportable(projectExportable);

        return projectViewModel;
    }

    /**
     * Aggregates data from the current Project for the Dashboard
     *
     * @param projectId the id of the Project
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public ProjectGeometryCountsViewModel getGeometryCounts(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE);

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, true);

        List<LcEntryType> geometryTypes = new ArrayList<>();
        geometryTypes.add(LcEntryType.LINE);
        geometryTypes.add(LcEntryType.RECTANGLE);
        geometryTypes.add(LcEntryType.POLYGON);
        geometryTypes.add(LcEntryType.POINT);
        ;

        String labelConfigId = projectById.getLabelConfiguration().getId();
        List<IGeometryCountViewModel> geometryCounts = lcEntryValueRepository.getGeometryCountsByConfigurationIdAndLcEntryType(
                labelConfigId,
                geometryTypes);

        long geometryCountTotal = 0L;
        for (IGeometryCountViewModel geoCountModel : geometryCounts) {
            long geometryCount = geoCountModel.getLcEntryValueCount();
            geometryCountTotal += geometryCount;
        }

        return new ProjectGeometryCountsViewModel(geometryCountTotal, geometryCounts);
    }

    /**
     * Aggregates data from the current Project for the Dashboard
     *
     * @param projectId the id of the Project
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public ProjectLabelCountByDayViewModel getGeometryCountsByDay(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE);

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, true);

        /* DISABLE THIS FEATURE DUE PERFORMANCE ISSUES - TICKET CREATED: LAB-854

        String labelIterationId = projectById.getLabelIteration().getId();
        List<IGeometryCountByDayViewModel> geometryCountByDay = lcEntryValueRepository.getGeometryCountByDayForLastMonth(labelIterationId);
        long geometryCountByDayTotal = 0L;
        for (IGeometryCountByDayViewModel iGeometryCountByDayViewModel : geometryCountByDay) {
            long geometryCount = iGeometryCountByDayViewModel.getGeometryCount();
            geometryCountByDayTotal += geometryCount;
        }
        ProjectLabelCountByDayViewModel returnData = new ProjectLabelCountByDayViewModel(
                geometryCountByDayTotal,
                geometryCountByDay);
       */

        return new ProjectLabelCountByDayViewModel(0, Collections.emptyList());
    }

    /**
     * Aggregates data from the current Project for the Dashboard
     *
     * @param projectId the id of the Project
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public ProjectDashboardViewModel getDashboardData(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE);

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, true);

        // Create new ProjectDashboardViewModel and add all Properties form the current Project
        ProjectDashboardViewModel projectDashboardViewModel = ProjectMapper
                .mapToProjectDashboardViewModel(projectById,
                        mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

        // Get the remaining Properties for the ProjectDashboardViewModel

        // Don't count tasks with deleted images.
        int countTasks = labelTaskRepository.countProjectTasksWhereMediasNotDeleted(projectId);

        int countDatasets = projectById.getDatasets().size();
        boolean hasLabelConfiguration = !projectById.getLabelConfiguration().getEntries().isEmpty();

        // Get the LabelRatings for the Project
        List<LabelerRating> labelerRatings = labelerRatingRepository.findLabelerRatingsByProjectIdAndMediaDeleted(
                projectId,
                false);
        long approvedReviewPerformance = labelerRatings.stream().mapToLong(LabelerRating::getPositive).sum();
        long declinedReviewPerformance = labelerRatings.stream().mapToLong(LabelerRating::getNegative).sum();

        Map<LabelTaskState, Long> taskStatusMap = new EnumMap<>(LabelTaskState.class);
        Arrays.stream(LabelTaskState.values())
                .forEach(state -> taskStatusMap.put(state,
                        labelTaskRepository.countProjectTasksByStateWhereMediasNotDeleted(
                                projectId,
                                state)));

        Map<MediaSourceType, Long> taskMediaDetailMap = new EnumMap<>(MediaSourceType.class);
        Arrays.stream(MediaSourceType.values())
                .forEach(mediaSourceType -> taskMediaDetailMap.put(mediaSourceType,
                        labelTaskRepository.countProjectTasksByMediaTypeWhereMediasNotDeleted(
                                projectId,
                                mediaSourceType)));

        List<DatasetMediaStatusViewModel> datasetMediaStatusViewModels = getDatasetImageStatusViewModels(projectById);
        long projectTotalInvalidImages = datasetMediaStatusViewModels
                .stream()
                .mapToLong(DatasetMediaStatusViewModel::getInvalidMediaCount)
                .sum();

        // Get Pricing Plan Type for the current Organisation
        LimitPricingPlanViewModel limitsByOrgId = limitService.getLimitsByOrgId(owner);
        String pricingPlanType = limitsByOrgId.getPricingPlanType();

        // Set all properties
        projectDashboardViewModel.setCountTasks(countTasks);
        projectDashboardViewModel.setCountDatasets(countDatasets);
        projectDashboardViewModel.setHasLabelConfiguration(hasLabelConfiguration);
        projectDashboardViewModel.setApprovedReviewPerformance(approvedReviewPerformance);
        projectDashboardViewModel.setDeclinedReviewPerformance(declinedReviewPerformance);
        projectDashboardViewModel.setTaskStatus(taskStatusMap);
        projectDashboardViewModel.setTaskMediaDetail(taskMediaDetailMap);
        projectDashboardViewModel.setCurrentPlan(pricingPlanType);
        projectDashboardViewModel.setDatasetMediaStatuses(datasetMediaStatusViewModels);
        projectDashboardViewModel.setCountInvalidImages(projectTotalInvalidImages);

        return projectDashboardViewModel;
    }

    private List<DatasetMediaStatusViewModel> getDatasetImageStatusViewModels(@NotNull Project projectById) {
        List<DatasetMediaStatusViewModel> datasetMediaStatusViewModels = new ArrayList<>();
        projectById.getDatasets().forEach((Dataset dataset) -> {

            Map<MediaSourceType, Long> mediaStatusMap = new EnumMap<>(MediaSourceType.class);

            // Count all invalid images by type
            Arrays.stream(MediaSourceType.values())
                    .forEach((MediaSourceType mediaSourceType) ->
                            mediaStatusMap.put(mediaSourceType,
                                    mediaRepository.countInvalidAndUndeletedMedias(dataset,
                                            mediaSourceType)));

            long invalidImageCount = mediaStatusMap.values().stream().mapToLong(Long::longValue).sum();

            DatasetMediaStatusViewModel datasetMediaStatusViewModel = new DatasetMediaStatusViewModel(
                    dataset.getName(),
                    invalidImageCount,
                    mediaStatusMap
            );

            datasetMediaStatusViewModels.add(datasetMediaStatusViewModel);
        });
        return datasetMediaStatusViewModels;
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_PLACEHOLDER, "id", "" + projectId));
    }

    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException(DATASET_PLACEHOLDER, "id", "" + datasetId));
    }

    private void checkIfDummyProjectOrDummyDataset(String projectName, String datasetName) {
        if (!DUMMY_PROJECT_PLACEHOLDER.equals(projectName) || !(DUMMY_DATASET_ONE_PLACEHOLDER.equals(datasetName) || DUMMY_DATASET_TWO_PLACEHOLDER.equals(
                datasetName))) {
            if (DUMMY_PROJECT_PLACEHOLDER.equals(projectName)) {
                throw new GenericException(DUMMY_PROJECT_ERROR_PLACEHOLDER, null, null);
            }

            if (DUMMY_DATASET_ONE_PLACEHOLDER.equals(datasetName) || DUMMY_DATASET_TWO_PLACEHOLDER.equals(datasetName)) {
                throw new GenericException(DUMMY_DATASET_ERROR_PLACEHOLDER, null, null);
            }
        }
    }

    private boolean isProjectExportable(String projectId) {
        long countExportableTasks = labelTaskRepository.countTasksByProjectIdAndTaskStateAndMediaDeleted(projectId);
        return countExportableTasks > 0;
    }

    private void increaseProjectsCount(String projectName, String projectOrg) {
        Metrics.summary("datagym.project.summary", "project.name",
                        projectName, "project.org", projectOrg)
                .record(1.0);
    }


    // Start time: every day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * * ")
    public void projectScheduleJob() {
        LocalDateTime localDateTime = LocalDateTime.now();

        // minus * days
        localDateTime = localDateTime.minusDays(DAYS_BEFORE_DELETE_PROJECT);

        // convert LocalDateTime to date
        long timeBeforeToDeleteProjects = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String formatLocalDateTime = dateFormat8.format(localDateTime);

        List<Project> projectsToDelete =
                projectRepository.findAllByDeleteTimeBeforeAndDeletedTrue(timeBeforeToDeleteProjects);

        StringBuilder loggingOutput = new StringBuilder();

        projectsToDelete.forEach(project -> loggingOutput.append(
                String.format("Project {id: %s, name: %s, owner: %s, countTasks: %s} ",
                        project.getId(),
                        project.getName(),
                        project.getOwner(),
                        project.getLabelTasks().size())));


        LOGGER.info("Deleting {} Projects with delete_time before : {}. Details: {}",
                projectsToDelete.size(), formatLocalDateTime, loggingOutput);


        // Delete each label task in a separate transaction to avoid OutOfMemoryError
        for (Project project : projectsToDelete) {
            List<String> labelTaskIds = labelTaskRepository.getLabelTaskIdsInProject(project.getId());
            for (String labelTaskId : labelTaskIds) {
                labelTaskService.deleteLabelTaskByIdInternal(labelTaskId);
            }
            projectRepository.delete(project);
            LOGGER.info("Successfully deleted project with id {}. Amount tasks: {}", project.getId(), labelTaskIds.size());
        }
    }
}
