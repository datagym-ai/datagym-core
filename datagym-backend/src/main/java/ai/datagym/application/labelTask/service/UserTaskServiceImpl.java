package ai.datagym.application.labelTask.service;

import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.errorHandling.NoContentException;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.entity.PreLabelState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service()
@Transactional(propagation = Propagation.REQUIRED)
public class UserTaskServiceImpl implements UserTaskService {
    private final ApplicationContext applicationContext;
    private final LabelTaskRepository labelTaskRepository;
    private final ProjectRepository projectRepository;
    private final DatasetRepository datasetRepository;
    private final LimitService limitService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public UserTaskServiceImpl(LabelTaskRepository labelTaskRepository,
                               ProjectRepository projectRepository,
                               DatasetRepository datasetRepository,
                               LimitService limitService,
                               ApplicationEventPublisher applicationEventPublisher,
                               ApplicationContext applicationContext) {
        this.labelTaskRepository = labelTaskRepository;
        this.projectRepository = projectRepository;
        this.datasetRepository = datasetRepository;
        this.limitService = limitService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationContext = applicationContext;
    }

    /**
     * Fetching the Spring Proxy of the class in the class itself and use it to call methods on it rather than "this".
     * This Proxy will be used to create a new Transaction (similar to the Propagation Mode - Propagation.REQUIRES_NEW)
     * within a class annotated with @Transactional(propagation = Propagation.REQUIRED)
     */
    private UserTaskServiceImpl getSpringProxy() {
        return applicationContext.getBean(this.getClass());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<UserTaskViewModel> getUserTasks() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        List<UserTaskViewModel> userTaskViewModelList = new ArrayList<>();

        OauthUser oauthUser = SecurityContext.get();
        Map<String, String> orgs = oauthUser.orgs();

        for (Map.Entry<String, String> userOrg : orgs.entrySet()) {
            //Permissions check
            String owner = userOrg.getKey();
            DataGymSecurity.isAdminOrUser(owner, false);

            boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin(owner, false);

            if (isAdmin) {
                // Create Dummy_Project for the current Organisation, if it doesn't exist
                getSpringProxy().checkIfDummyProjectIsCreated(owner);
            }

            List<Project> allByOwner = projectRepository.findAllByOwnerAndDeletedIsFalse(owner);

            allByOwner.forEach(project -> {
                UserTaskViewModel userTaskViewModel = new UserTaskViewModel();

                userTaskViewModel.setProjectId(project.getId());
                userTaskViewModel.setProjectName(project.getName());
                userTaskViewModel.setOwner(project.getOwner());

                // Get count of WAITING-, WAITING_CHANGED- or IN_PROGRESS-Tasks
                long countOfWaitingTasks = labelTaskRepository
                        .countPossibleTasksToLabelForLabeler(project.getId(), DataGymSecurity.getLoggedInUserId());

                // Get count of Tasks to Review for the current Project and the loggedIn User
                long countTasksToReview = 0;
                if (project.isReviewActivated() && checkIfUserIsReviewerForCurrentProject(project)) {
                    countTasksToReview = labelTaskRepository.countPossibleTasksToReview(project.getId());
                }

                userTaskViewModel.setCountWaitingTasks(countOfWaitingTasks);
                userTaskViewModel.setCountTasksToReview(countTasksToReview);

                userTaskViewModelList.add(userTaskViewModel);
            });
        }

        return userTaskViewModelList;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LabelTaskViewModel getNextTask(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        OauthUser oauthUser = SecurityContext.get();
        Map<String, String> orgs = oauthUser.orgs();

        for (Map.Entry<String, String> userOrg : orgs.entrySet()) {
            //Permissions check
            String owner = userOrg.getKey();
            DataGymSecurity.isAdminOrUser(owner, false);

            Optional<LabelTask> nextTask = Optional.empty();

            String loggedInUserId = DataGymSecurity.getLoggedInUserId();

            if (projectId == null) {
                nextTask = labelTaskRepository.findByOwnerAndStateAndMediaDeletedFalse(owner, loggedInUserId);
            } else {
                nextTask = labelTaskRepository
                        .findByOwnerAndStateAndProjectIdAndMediaDeletedFalse(owner, loggedInUserId, projectId);
            }

            if (nextTask.isPresent()) {
                LabelTask labelTask = nextTask.get();
                labelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);
                labelTask.setLabeler(loggedInUserId);

                LabelTask updatedLabelTask = labelTaskRepository.saveAndFlush(labelTask);

                return LabelTaskMapper.mapToLabelTaskViewModel(updatedLabelTask);
            }
        }

        if (projectId != null) {
            Project projectById = getProjectById(projectId);
            String projectName = projectById.getName();

            throw new NoContentException("project", "name", projectName);
        } else {
            throw new GenericException("no_tasks_for_user", null, null);
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LabelTaskViewModel getNextReviewTask(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Project projectById = getProjectById(projectId);
        String currentOrg = projectById.getOwner();

        // Check if user is Admin in the current Organisation
        boolean isAdmin = DataGymSecurity.checkIfUserIsAdmin(currentOrg, false);

        // Check if user is Reviewer in the current Organisation
        boolean isReviewerForOrg = checkIfUserIsReviewerForCurrentProject(projectById);

        // If user is Admin in the current Organisation, check for Tasks to be reviewed(with TaskState = COMPLETED)
        if (isAdmin || isReviewerForOrg) {
            Optional<LabelTask> nextTask = Optional.empty();

            // Get the next Completed-Task from the current Project
            nextTask = labelTaskRepository
                    .findNextTaskByProjectIdAndCompletedOrSkippedTaskState(currentOrg, projectId);

            if (nextTask.isPresent()) {
                LabelTask labelTask = nextTask.get();
                return LabelTaskMapper.mapToLabelTaskViewModel(labelTask);
            }
        }

        String projectName = projectById.getName();

        throw new NoContentException("project", "name", projectName);
    }

    private List<LabelTask> getAllCompletedAndReviewedTasks(String configId){
        List<LabelTaskState> labelTaskStateList = new ArrayList<>();
        labelTaskStateList.add(LabelTaskState.COMPLETED);
        labelTaskStateList.add(LabelTaskState.REVIEWED);

        // Find all Tasks with LabelTaskState COMPLETED or REVIEWED
        return labelTaskRepository.findTasksByLabelConfigurationIdAndTaskState(configId, labelTaskStateList);
    }

    private void isTaskCompletedOrReviewed(LabelTask labelTask){
        if (!labelTask.getLabelTaskState().equals(LabelTaskState.COMPLETED) &&
                !labelTask.getLabelTaskState().equals(LabelTaskState.REVIEWED)) {
            throw new GenericException("wrong_label_task_state", null, null, labelTask.getId());
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public Integer getNumberOfCompletedTasks(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        List<LabelTask> labelTasks = getAllCompletedAndReviewedTasks(configId);

        Integer counter = 0;

        for (LabelTask labelTask : labelTasks) {
            isTaskCompletedOrReviewed(labelTask);
            if (labelTask.getLabelTaskState().equals(LabelTaskState.COMPLETED)) {
                counter++;
            }
        }

        return counter;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public Integer getNumberOfReviewedTasks(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        List<LabelTask> labelTasks = getAllCompletedAndReviewedTasks(configId);

        Integer counter = 0;

        for (LabelTask labelTask : labelTasks) {
            isTaskCompletedOrReviewed(labelTask);
            if (labelTask.getLabelTaskState().equals(LabelTaskState.REVIEWED)) {
                counter++;
            }
        }
        return counter;
    }


    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void changeTaskStateAfterLabelConfigurationUpdate(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        List<LabelTask> labelTasks = getAllCompletedAndReviewedTasks(configId);

        for (LabelTask labelTask : labelTasks) {
            isTaskCompletedOrReviewed(labelTask);

            if (labelTask.getLabelTaskType() == LabelTaskType.BENCHMARK_MASTER ||
                    labelTask.getLabelTaskType() == LabelTaskType.BENCHMARK_SLAVE) {

                //Change LabelTaskState of the current BENCHMARK_MASTER or BENCHMARK_SLAVE Task to IN_PROGRESS
                labelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);

            } else if (labelTask.getLabelTaskType() == LabelTaskType.DEFAULT) {

                //Change LabelTaskState of the current DEFAULT Task to WAITING_CHANGED
                labelTask.setLabelTaskState(LabelTaskState.WAITING_CHANGED);
            }

            labelTaskRepository.save(labelTask);

            // Check Pricing Plan Limits
            String projectOrg = labelTask.getProject().getOwner();
            String projectName = labelTask.getProject().getName();

            // Check if Project is the "Dummy_Project"
            if (!"Dummy_Project".equals(projectName)) {
                limitService.decreaseUsedLabelsCount(projectOrg);

                // Metrics update - changeImagesCount
                changeImagesCount(projectOrg, -1);
            }
        }
    }

    /**
     * Checks if the Dummy Project is created and emit an CustomUserTaskDummyEvent with an owner(organisation) as parameter.
     *
     * @param owner - the OrganisationsId
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkIfDummyProjectIsCreated(String owner) {
        boolean isDummyProjectCreated = projectRepository
                .findByNameAndDeletedFalseAndOwner(DUMMY_PROJECT_PLACEHOLDER, owner).isPresent();

        boolean isDummyDatasetCreated = datasetRepository
                .findByNameAndDeletedFalseAndOwner(DUMMY_DATASET_ONE_PLACEHOLDER, owner).isPresent();

        if (!isDummyProjectCreated && !isDummyDatasetCreated) {
            // Emit an CustomUserTaskDummyEvent with an owner(organisation) as parameter
            CustomUserTaskDummyEvent customEvent = new CustomUserTaskDummyEvent(this, owner);
            applicationEventPublisher.publishEvent(customEvent);
        }
    }

    /**
     * Check if the current loggedIn User is a Reviewer for the current Project
     */
    private boolean checkIfUserIsReviewerForCurrentProject(Project projectById) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        return projectById.getReviewers().stream()
                .anyMatch(currentReviewerId -> currentReviewerId.getUserId().equals(loggedInUserId));
    }

    // Metrics update - changeImagesCount
    private void changeImagesCount(String organisationId, int contToAdd) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        // Counter class stores the measurement name and the tags and their values
        Counter counter = Metrics.counter("count.images", "labeler",
                loggedInUserId, "organisation", organisationId);
        counter.increment(contToAdd);
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    private boolean isCurrentTaskToAddToCountOfWaitingTasks(LabelTask labelTask) {
        boolean benchmarkTaskToBeLabeledFromCurrentUser = isBenchmarkTaskToBeLabeledFromCurrentUser(labelTask);
        boolean isNotDeleted = !labelTask.getMedia().isDeleted();
        boolean isNotPreLabelStateInProgress = labelTask.getPreLabelState() != PreLabelState.IN_PROGRESS;

        return isNotDeleted && benchmarkTaskToBeLabeledFromCurrentUser && isNotPreLabelStateInProgress;
    }

    private boolean isBenchmarkTaskToBeLabeledFromCurrentUser(LabelTask labelTask) {
        boolean benchmark = labelTask.isBenchmark();
        String benchmarkMasterLabeler = labelTask.getLabeler();
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        if (benchmark && benchmarkMasterLabeler != null && !benchmarkMasterLabeler.equals(loggedInUserId)) {
            Media media = labelTask.getMedia();
            Project project = labelTask.getProject();

            Optional<LabelTask> optionalSlaveTask = labelTaskRepository
                    .findLabelTaskByMediaAndProjectAndLabelerAndLabelTaskType(media, project, loggedInUserId, LabelTaskType.BENCHMARK_SLAVE);

            if (optionalSlaveTask.isEmpty()) {

                // Create new BENCHMARK_SLAVE Task for the current benchmark Task
                createNewSlaveTask(labelTask, media, project, loggedInUserId);

                return true;
            }
        } else if (labelTask.getLabelTaskState().equals(LabelTaskState.WAITING) ||
                labelTask.getLabelTaskState().equals(LabelTaskState.WAITING_CHANGED) ||
                (labelTask.getLabelTaskState().equals(LabelTaskState.IN_PROGRESS) && loggedInUserId.equals(labelTask.getLabeler()))) {
            return true;
        }

        return false;
    }

    private void createNewSlaveTask(LabelTask labelTask, Media media, Project project, String loggedInUserId) {
        LabelTask slaveBenchMarkTask = new LabelTask();
        slaveBenchMarkTask.setProject(project);
        slaveBenchMarkTask.setMedia(media);
        slaveBenchMarkTask.setLabelIteration(labelTask.getLabelIteration());
        slaveBenchMarkTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);
        slaveBenchMarkTask.setLabeler(loggedInUserId);
        slaveBenchMarkTask.setLabelTaskType(LabelTaskType.BENCHMARK_SLAVE);

        labelTaskRepository.save(slaveBenchMarkTask);
    }
}
