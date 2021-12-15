package ai.datagym.application.labelTask.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.awsS3.AwsS3HelperService;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueExtendAllBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskMoveAllBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskReviewBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.service.LabelerRatingService;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.AwsS3Video;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.service.MediaMapper;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.SystemException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LabelTaskServiceImpl implements LabelTaskService {
    private boolean deactivateLimiter;

    private static final String WRONG_LABEL_TASK_STATE = "wrong_label_task_state";

    private final LabelTaskRepository labelTaskRepository;
    private final LabelIterationRepository labelIterationRepository;
    private final MediaRepository mediaRepository;
    private final ProjectRepository projectRepository;
    private final DatasetRepository datasetRepository;
    private final LabelConfigurationService labelConfigurationService;
    private final LcEntryValueService lcEntryValueService;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final LimitService limitService;
    private final LabelerRatingService labelerRatingService;
    private final Optional<AwsS3HelperService> awsS3HelperService;

    @Autowired
    public LabelTaskServiceImpl(LabelTaskRepository labelTaskRepository,
                                LabelIterationRepository labelIterationRepository,
                                MediaRepository mediaRepository,
                                ProjectRepository projectRepository,
                                DatasetRepository datasetRepository,
                                LabelConfigurationService labelConfigurationService,
                                LcEntryValueService lcEntryValueService,
                                LcEntryValueRepository lcEntryValueReposiory,
                                LimitService limitService,
                                @Value(value = "${datagym.deactivate-limiter}") boolean deactivateLimiter,
                                LabelerRatingService labelerRatingService,
                                @Autowired(required = false) Optional<AwsS3HelperService> awsS3HelperService) {
        this.labelTaskRepository = labelTaskRepository;
        this.labelIterationRepository = labelIterationRepository;
        this.mediaRepository = mediaRepository;
        this.projectRepository = projectRepository;
        this.datasetRepository = datasetRepository;
        this.labelConfigurationService = labelConfigurationService;
        this.lcEntryValueService = lcEntryValueService;
        this.lcEntryValueRepository = lcEntryValueReposiory;
        this.limitService = limitService;
        this.deactivateLimiter = deactivateLimiter;
        this.labelerRatingService = labelerRatingService;
        this.awsS3HelperService = awsS3HelperService;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public LabelTask createLabelTask(String projectId, String mediaId, String iterationId) {
        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        Media mediaById = getMediaById(mediaId);
        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        return createLabelTaskInternal(projectById, mediaById, labelIterationById);
    }

    @NotNull
    public LabelTask createLabelTaskInternal(Project project, Media media, LabelIteration labelIteration) {
        LabelTask labelTask = createLabelTaskInternalNoSave(project, media, labelIteration);

        return labelTaskRepository.save(labelTask);
    }

    @NotNull
    public LabelTask createLabelTaskInternalNoSave(Project project, Media media, LabelIteration labelIteration) {
        LabelTask labelTask = new LabelTask();

        labelTask.setProject(project);
        labelTask.setMedia(media);
        labelTask.setLabelIteration(labelIteration);
        return labelTask;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLabelTaskByIdInternal(String taskId) {
        LabelTask labelTask = getLabelTaskById(taskId);

        Project project = labelTask.getProject();

        LabelIteration labelIteration = labelTask.getLabelIteration();
        Media media = labelTask.getMedia();

        String mediaId = media.getId();
        String iterationId = labelIteration.getId();

        //Delete Task from Database
        labelTaskRepository.delete(labelTask);

        // Delete All LcEntryValues from the current Task (by iterationId and mediaId)
        lcEntryValueRepository.deleteAllByLabelIterationIdAndMediaId(iterationId, mediaId);
    }


    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void deleteAllLabelTasksFromDataset(Project project, List<String> media) {

        //Permissions check
        String owner = project.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        String projectId = project.getId();
        String labelIterationId = project.getLabelIteration().getId();

        // Delete All LcEntryValues from the current Task (by iterationId and mediaId)
        lcEntryValueRepository.deleteLcEntryValuesByLabelIterationIdAndMediaIdIn(labelIterationId, media);

        labelTaskRepository.deleteAllLabelTasksAndData(projectId, labelIterationId, media);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LabelTaskViewModel moveTaskStateIfUserIsAdmin(String labelTaskId, LabelTaskState toTaskState) throws IOException {
        LabelTask labelTaskById = getLabelTaskById(labelTaskId);

        //Permissions check
        String owner = labelTaskById.getProject().getOwner();
        boolean isAdmin = DataGymSecurity.isAdmin(owner, false);

        LabelTaskState labelTaskState = labelTaskById.getLabelTaskState();

        LabelTask updatedLabelTask = changeLabelTaskState(labelTaskById, labelTaskState, toTaskState, isAdmin);

        return LabelTaskMapper.mapToLabelTaskViewModel(updatedLabelTask);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void moveAllTasks(LabelTaskMoveAllBindingModel labelTaskMoveAllBindingModel) {
        String projectId = labelTaskMoveAllBindingModel.getProjectId();
        Project projectById = getProjectByIdAndDeletedFalse(projectId);
        String direction = labelTaskMoveAllBindingModel.getDirection().toUpperCase();

        //Permissions check
        String projectOwner = projectById.getOwner();
        DataGymSecurity.isAdmin(projectOwner, false);

        if (!"BACKLOG".equals(direction) && !"WAITING".equals(direction)) {
            throw new GenericException(WRONG_LABEL_TASK_STATE, null, null);
        }

        String datasetId = labelTaskMoveAllBindingModel.getDatasetId();

        if (!"ALL".equals(datasetId)) {
            //Permissions check
            Dataset datasetById = getDatasetById(datasetId);
            String datasetOwner = datasetById.getOwner();
            DataGymSecurity.haveTheSameOwner(projectOwner, datasetOwner);
        }

        LabelTaskState newLabelTaskState = LabelTaskState.valueOf(direction);
        if ("ALL".equals(datasetId)) {
            if ("BACKLOG".equals(direction)) {
                labelTaskRepository.updateLabelTaskStateByProjectId(projectId,
                        LabelTaskState.WAITING, newLabelTaskState);
                labelTaskRepository.updateLabelTaskStateByProjectId(projectId,
                        LabelTaskState.WAITING_CHANGED, newLabelTaskState);
            } else {
                labelTaskRepository.updateLabelTaskStateByProjectId(projectId,
                        LabelTaskState.BACKLOG, newLabelTaskState);
            }
        } else {
            if ("BACKLOG".equals(direction)) {
                labelTaskRepository.updateLabelTaskStateByProjectIdAndDatasetId(projectId, datasetId,
                        LabelTaskState.WAITING, newLabelTaskState);
                labelTaskRepository.updateLabelTaskStateByProjectIdAndDatasetId(projectId, datasetId,
                        LabelTaskState.WAITING_CHANGED, newLabelTaskState);
            } else {
                labelTaskRepository.updateLabelTaskStateByProjectIdAndDatasetId(projectId, datasetId,
                        LabelTaskState.BACKLOG, newLabelTaskState);

            }
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LabelModeDataViewModel getTask(String taskId) {
        LabelTask labelTaskById = getLabelTaskById(taskId);
        Project project = labelTaskById.getProject();

        //Permissions check
        String projectOrganisation = project.getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, true);

        LabelConfiguration labelConfiguration = project.getLabelConfiguration();
        String configId = labelConfiguration.getId();
        LabelConfigurationViewModel labelConfigurationViewModel = labelConfigurationService.getLabelConfiguration(configId);

        String mediaId = labelTaskById.getMedia().getId();
        String iterationId = labelTaskById.getLabelIteration().getId();

        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = new LcEntryValueExtendAllBindingModel();
        lcEntryValueExtendAllBindingModel.setMediaId(mediaId);
        lcEntryValueExtendAllBindingModel.setIterationId(iterationId);
        lcEntryValueExtendAllBindingModel.setLabelTaskId(taskId);

        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();
        lcEntryValueCreateBindingModel.setIterationId(iterationId);
        lcEntryValueCreateBindingModel.setMediaId(mediaId);
        lcEntryValueCreateBindingModel.setLabelTaskId(taskId);

        // Extend All Global-Classification-Values
        lcEntryValueService.createGlobalClassificationsValuesGetRoots(configId, lcEntryValueCreateBindingModel);

        // Extend All Label-Entry-Values
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .extendAllConfigEntryValues(configId, lcEntryValueExtendAllBindingModel);

        Media media = labelTaskById.getMedia();
        MediaViewModel mediaViewModel = MediaMapper.mapToMediaViewModel(media);

        // Currently a image only has one connected dataset
        Dataset mediaDataset = media.getDatasets().stream().findFirst().orElse(null);

        // Set pre signed url to access the media file
        if (media instanceof AwsS3Video) {
            AwsS3Video s3Video = (AwsS3Video) media;
            try {
                URL awsPreSignedGetUriInternal = awsS3HelperService.orElseThrow(() -> new ServiceUnavailableException("aws")).createAwsPreSignedGetUriInternal(s3Video.getAwsKey());
                mediaViewModel.setUrl(awsPreSignedGetUriInternal.toString());
            } catch (IOException e) {
                throw new SystemException("Error by generating an aws s3 pre-signed url! TaskID: " + taskId, e);
            }
        }

        // Check if AiSegLimit is reached
        boolean isAiSegLimitReached = false;

        // Deactivate pricing limits for the Unit-Tests
        if (!deactivateLimiter) {
            isAiSegLimitReached = limitService.getLimitsByOrgId(projectOrganisation).getAiSegRemaining() == 0;
        }

        LabelModeDataViewModel labelModeDataViewModel = new LabelModeDataViewModel();
        labelModeDataViewModel.setTaskId(taskId);
        labelModeDataViewModel.setLabelTaskState(labelTaskById.getLabelTaskState().name());
        labelModeDataViewModel.setProjectName(project.getName());
        labelModeDataViewModel.setLabelConfig(labelConfigurationViewModel);
        labelModeDataViewModel.setLabelIteration(labelIterationViewModel);
        labelModeDataViewModel.setMedia(mediaViewModel);
        labelModeDataViewModel.setAiSegLimitReached(isAiSegLimitReached);
        labelModeDataViewModel.setLastChangedConfig(labelConfiguration.getTimestamp());
        labelModeDataViewModel.setReviewComment(labelTaskById.getReviewComment());
        labelModeDataViewModel.setReviewActivated(project.isReviewActivated());
        labelModeDataViewModel.setProjectType(project.getMediaType());
        if (mediaDataset != null) {
            labelModeDataViewModel.setDatasetId(mediaDataset.getId());
            labelModeDataViewModel.setDatasetName(mediaDataset.getName());
        }

        return labelModeDataViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void skipTask(String taskId) throws IOException {
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        String projectOrganisation = labelTaskById.getProject().getOwner();
        String currentTaskLabelerId = labelTaskById.getLabeler();

        DataGymSecurity.isAdminOrLabeler(projectOrganisation, currentTaskLabelerId, false);
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin(projectOrganisation, false);

        changeLabelTaskState(labelTaskById, LabelTaskState.IN_PROGRESS, LabelTaskState.SKIPPED, isAdmin);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LabelTaskCompleteViewModel completeTask(String taskId, LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException {
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        String projectOrganisation = labelTaskById.getProject().getOwner();
        String currentTaskLabelerId = labelTaskById.getLabeler();

        DataGymSecurity.isAdminOrLabeler(projectOrganisation, currentTaskLabelerId, false);
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin(projectOrganisation, false);

        // Create new LabelTaskCompleteUpdateModel
        LabelTaskCompleteViewModel labelTaskCompleteViewModel = new LabelTaskCompleteViewModel();
        labelTaskCompleteViewModel.setCurrentTaskId(taskId);
        labelTaskCompleteViewModel.setHasLabelConfigChanged(false);

        // Get the time, when the config lastly has been changed
        Long currentConfigLastChangedTimestamp = labelTaskById.getProject().getLabelConfiguration().getTimestamp();

        // Get the time, that the Client has send to the Server
        Long lastChangedConfigTimeFromClient = labelTaskCompleteBindingModel.getLastChangedConfig();

        if (currentConfigLastChangedTimestamp > lastChangedConfigTimeFromClient) {
            labelTaskCompleteViewModel.setHasLabelConfigChanged(true);
            return labelTaskCompleteViewModel;
        }

        String mediaId = labelTaskById.getMedia().getId();
        String iterationId = labelTaskById.getLabelIteration().getId();

        lcEntryValueService.validateEntryValuesBeforeTaskCompletion(iterationId, mediaId);

        changeLabelTaskState(labelTaskById, LabelTaskState.IN_PROGRESS, LabelTaskState.COMPLETED, isAdmin);

        return labelTaskCompleteViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void moveTaskToReviewed(String taskId) throws IOException {
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        String projectOrganisation = labelTaskById.getProject().getOwner();
        boolean isAdmin = DataGymSecurity.isAdmin(projectOrganisation, false);

        changeLabelTaskState(labelTaskById, LabelTaskState.COMPLETED, LabelTaskState.REVIEWED, isAdmin);
    }

    /**
     * Check if the current loggedIn User is a Reviewer for the current Project
     */
    private boolean checkIfUserIsReviewerForCurrentProject(Project projectById) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        return projectById.getReviewers().stream()
                .anyMatch(currentReviewerId -> currentReviewerId.getUserId().equals(loggedInUserId));
    }

    /**
     * Update LabelerRating and change labelTaskState
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void reviewCompletion(LabelTaskReviewBindingModel labelTaskReviewBindingModel, boolean success) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        String reviewComment = labelTaskReviewBindingModel.getReviewComment();
        String taskId = labelTaskReviewBindingModel.getTaskId();

        // Check if Task exists
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        Project project = labelTaskById.getProject();
        String projectOrganisation = project.getOwner();

        // Check if user is Admin in the current Organisation
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin(projectOrganisation, false);

        // Check if user is Reviewer in the current Organisation
        boolean isReviewerForOrg = checkIfUserIsReviewerForCurrentProject(project);

        // If user is Admin in the current Organisation, check for Tasks to be reviewed(with TaskState = COMPLETED)
        if (isAdmin || isReviewerForOrg) {
            // Check if LabelTaskState of the current Task is COMPLETED
            LabelTaskState labelTaskState = labelTaskById.getLabelTaskState();
            String labelTaskId = labelTaskById.getId();

            if (labelTaskState != LabelTaskState.COMPLETED && labelTaskState != LabelTaskState.SKIPPED) {
                throw new GenericException(WRONG_LABEL_TASK_STATE, null, null, labelTaskId);
            }

            // Set reviewComment
            labelTaskById.setReviewComment(reviewComment);

            // Update LabelerRating of the current User for this Project
            String projectId = project.getId();
            String labelerId = labelTaskById.getLabeler();
            String mediaId = labelTaskById.getMedia().getId();

            // Check if labelerId is null. If so set the labelerId to the Id of the current loggedIn user
            if (labelerId == null) {
                labelerId = DataGymSecurity.getLoggedInUserId();
            }

            LabelerRatingUpdateBindingModel ratingUpdateBindingModel = new LabelerRatingUpdateBindingModel();
            ratingUpdateBindingModel.setLabelerId(labelerId);
            ratingUpdateBindingModel.setProjectId(projectId);
            ratingUpdateBindingModel.setMediaId(mediaId);

            if (success) {
                reviewedSuccess(labelTaskById, ratingUpdateBindingModel, true);
            } else {
                reviewedFailed(labelTaskById, ratingUpdateBindingModel, true);
            }
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void activateBenchmark(String taskId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        // Check if Task exists
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        Project project = labelTaskById.getProject();
        String projectOrganisation = project.getOwner();
        DataGymSecurity.isAdmin(projectOrganisation, false);
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        labelTaskById.setBenchmark(true);
        labelTaskById.setLabelTaskType(LabelTaskType.BENCHMARK_MASTER);
        labelTaskById.setLabelTaskState(LabelTaskState.IN_PROGRESS);
        labelTaskById.setLabeler(loggedInUserId);

        labelTaskRepository.save(labelTaskById);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void deactivateBenchmark(String taskId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        // Check if Task exists
        LabelTask labelTaskById = getLabelTaskById(taskId);

        //Permissions check
        Project project = labelTaskById.getProject();
        String projectOrganisation = project.getOwner();
        DataGymSecurity.isAdmin(projectOrganisation, false);

        labelTaskById.setBenchmark(false);
        labelTaskById.setLabelTaskType(LabelTaskType.DEFAULT);
        labelTaskById.setLabeler(null);

        labelTaskRepository.save(labelTaskById);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LabelTaskViewModel resetLabeler(String taskId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        // Check if Task exists
        LabelTask labelTaskById = getLabelTaskById(taskId);
        LabelTaskState labelTaskState = labelTaskById.getLabelTaskState();

        //Permissions check
        Project project = labelTaskById.getProject();
        String projectOrganisation = project.getOwner();
        DataGymSecurity.isAdmin(projectOrganisation, false);

        if (labelTaskState == LabelTaskState.IN_PROGRESS) {
            labelTaskState = LabelTaskState.WAITING_CHANGED;
            labelTaskById.setLabelTaskState(labelTaskState);
        }

        if (labelTaskState != LabelTaskState.WAITING_CHANGED &&
                labelTaskState != LabelTaskState.COMPLETED) {
            throw new GenericException(WRONG_LABEL_TASK_STATE, null, null, labelTaskById.getId());
        }

        labelTaskById.setLabeler(null);

        labelTaskRepository.save(labelTaskById);

        return LabelTaskMapper.mapToLabelTaskViewModel(labelTaskById);
    }

    /**
     * Update LabelerRating and change State to REVIEWED
     */
    private void reviewedSuccess(LabelTask labelTaskById, LabelerRatingUpdateBindingModel ratingUpdateBindingModel, boolean isPermitted) throws IOException {
        // Update LabelerRating of the current User for this Project
        labelerRatingService.addToPositive(ratingUpdateBindingModel);

        // Get current LabelTaskState
        LabelTaskState labelTaskState = labelTaskById.getLabelTaskState();

        if (labelTaskState == LabelTaskState.COMPLETED) {
            // Change State to REVIEWED
            changeLabelTaskState(labelTaskById, labelTaskState, LabelTaskState.REVIEWED, isPermitted);
        } else if (labelTaskState == LabelTaskState.SKIPPED) {
            // Change State to REVIEWED_SKIP
            changeLabelTaskState(labelTaskById, labelTaskState, LabelTaskState.REVIEWED_SKIP, isPermitted);
        }
    }

    /**
     * Update LabelerRating and change State to WAITING_CHANGED
     */
    private void reviewedFailed(LabelTask labelTaskById, LabelerRatingUpdateBindingModel ratingUpdateBindingModel, boolean isAdmin) throws IOException {
        // Update LabelerRating of the current User for this Project
        labelerRatingService.addToNegative(ratingUpdateBindingModel);

        // Get current LabelTaskState
        LabelTaskState labelTaskState = labelTaskById.getLabelTaskState();

        // Change State to REVIEWED

        String labeler = labelTaskById.getLabeler();
        // Change to WAITING_CHANGED removes the labeler.
        changeLabelTaskState(labelTaskById, labelTaskState, LabelTaskState.WAITING_CHANGED, isAdmin);

        labelTaskById.setLabeler(labeler);
        labelTaskRepository.save(labelTaskById);
    }

    private LabelTask changeLabelTaskState(LabelTask labelTask, LabelTaskState fromTaskState, LabelTaskState toTaskState, boolean isPermitted) throws IOException {
        LabelTaskState labelTaskState = labelTask.getLabelTaskState();

        if (!LabelTaskStateMachine.isStateChangePossible(fromTaskState, toTaskState)) {
            throw new GenericException(WRONG_LABEL_TASK_STATE, null, null, labelTask.getId());
        }

        if ((labelTaskState == fromTaskState || isPermitted)) {

            // Check Pricing Plan Limits
            if (toTaskState == LabelTaskState.COMPLETED || toTaskState == LabelTaskState.SKIPPED) {
                String projectOrg = labelTask.getProject().getOwner();
                String projectName = labelTask.getProject().getName();

                // Check if Project is the "Dummy_Project"
                if (!DUMMY_PROJECT_PLACEHOLDER.equals(projectName)) {
                    limitService.increaseUsedLabelsCount(projectOrg);

                    // Metrics update - changeImagesCount
                    changeImagesCount(projectOrg, 1);
                }
            }

            labelTask.setLabelTaskState(toTaskState);

            if (toTaskState == LabelTaskState.WAITING_CHANGED) {
                labelTask.setLabeler(null);
            }

            return labelTaskRepository.saveAndFlush(labelTask);
        }

        throw new GenericException(WRONG_LABEL_TASK_STATE, null, null, labelTask.getId());
    }

    // Metrics update - changeImagesCount
    private void changeImagesCount(String organisationId, final int contToAdd) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        // Counter class stores the measurement name and the tags and their values
        Counter counter = Metrics.counter("count.images", "labeler",
                loggedInUserId, "organisation", organisationId);
        counter.increment(contToAdd);
    }

    private void moveAllFromDataset(List<LabelTask> allByProjectIdAndLabelTaskState, String direction, String datasetId) {
        Dataset datasetById = getDatasetById(datasetId);
        Set<Media> media = datasetById.getMedia();

        allByProjectIdAndLabelTaskState
                .stream()
                .filter(labelTask ->
                        media.contains(labelTask.getMedia()))
                .forEach(labelTask -> {
                    labelTask.setLabelTaskState(LabelTaskState.valueOf(direction));
                });
    }

    private LabelIteration getLabelIterationById(String iterationId) {
        return labelIterationRepository.findById(iterationId)
                .orElseThrow(() -> new NotFoundException("Label Iteration", "id", "" + iterationId));
    }

    private Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("media", "id", "" + mediaId));
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    private Project getProjectByIdAndDeletedFalse(String projectId) {
        return projectRepository
                .findByIdAndDeletedIsFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    private LabelTask getLabelTaskById(String labelTaskId) {
        return labelTaskRepository
                .findById(labelTaskId)
                .orElseThrow(() -> new NotFoundException("Label Task", "id", "" + labelTaskId));
    }

    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException("Dataset", "id", "" + datasetId));
    }
}
