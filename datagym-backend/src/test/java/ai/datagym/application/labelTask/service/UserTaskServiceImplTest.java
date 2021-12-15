package ai.datagym.application.labelTask.service;

import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.errorHandling.NoContentException;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.LabelTaskUtils;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class UserTaskServiceImplTest {
    private UserTaskService userTaskService;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        userTaskService = new UserTaskServiceImpl(
                labelTaskRepositoryMock,
                projectRepositoryMock,
                datasetRepository,
                limitServiceMock,
                applicationEventPublisher,
                applicationContext
        );
    }

    @Test
    void getUserTasks_whenNoTasksWithStateWaitingOrWaitingChangedOrInProgress_getUserTasksWithZeroOpenTasks() throws NoSuchMethodException, NoSuchAlgorithmException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithOneOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);
        Project firstProject = testProjects.get(0);
        firstProject.setOwner("eforce21");

        Project secondProject = testProjects.get(1);
        secondProject.setOwner("datagym");

        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(2, currentLoggedInUserId);
        LabelTask firstLabelTask = testLabelTaskList.get(0);
        LabelTask secondLabelTask = testLabelTaskList.get(0);

        firstLabelTask.setProject(firstProject);
        secondLabelTask.setProject(secondProject);

        firstProject.getLabelTasks().add(firstLabelTask);
        secondProject.getLabelTasks().add(secondLabelTask);

        List<Project> eForceProjects = new ArrayList<>() {{
            add(firstProject);
        }};

        List<Project> datagymProjects = new ArrayList<>() {{
            add(secondProject);
        }};

        //When
        when(projectRepositoryMock.findAllByOwnerAndDeletedIsFalse("eforce21"))
                .thenReturn(eForceProjects);

        when(projectRepositoryMock.findAllByOwnerAndDeletedIsFalse("datagym"))
                .thenReturn(datagymProjects);

        List<UserTaskViewModel> userTaskViewModelList = userTaskService.getUserTasks();

        UserTaskViewModel actual = userTaskViewModelList.get(0);

        //Then
        assertEquals(1, userTaskViewModelList.size());
        assertEquals(0, actual.getCountWaitingTasks());
    }

    @Test
    void getUserTasks_whenTwoTasksWithStateWaitingOrWaitingChangedOrInProgress_getUserTasksWithTwoOpenTasks() throws NoSuchMethodException, NoSuchAlgorithmException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValuesAndNoAdminRole();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);
        Project firstProject = testProjects.get(0);
        firstProject.setOwner("eforce21");

        Project secondProject = testProjects.get(1);
        secondProject.setOwner("datagym");

        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(3, currentLoggedInUserId);
        LabelTask firstLabelTask = testLabelTaskList.get(0);
        firstLabelTask.setLabelTaskState(LabelTaskState.WAITING);

        LabelTask secondLabelTask = testLabelTaskList.get(0);
        secondLabelTask.setLabelTaskState(LabelTaskState.WAITING_CHANGED);

        LabelTask thirdLabelTask = testLabelTaskList.get(2);
        thirdLabelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);
        thirdLabelTask.setLabeler(currentLoggedInUserId);

        firstLabelTask.setProject(firstProject);
        secondLabelTask.setProject(secondProject);
        thirdLabelTask.setProject(firstProject);

        firstProject.getLabelTasks().add(firstLabelTask);
        firstProject.getLabelTasks().add(thirdLabelTask);

        secondProject.getLabelTasks().add(secondLabelTask);

        List<Project> eForceProjects = new ArrayList<>() {{
            add(firstProject);
        }};

        List<Project> datagymProjects = new ArrayList<>() {{
            add(secondProject);
        }};

        //When
        when(projectRepositoryMock.findAllByOwnerAndDeletedIsFalse("eforce21"))
                .thenReturn(eForceProjects);

        when(projectRepositoryMock.findAllByOwnerAndDeletedIsFalse("test_org"))
                .thenReturn(datagymProjects);

        List<UserTaskViewModel> userTaskViewModelList = userTaskService.getUserTasks();

        UserTaskViewModel actual = userTaskViewModelList.get(0);

        //Then
        assertEquals(1, userTaskViewModelList.size());
        verify(labelTaskRepositoryMock, times(1))
                .countPossibleTasksToLabelForLabeler(anyString(), anyString());
    }

    @Test
    void createLabelTask_whenNoProjectsInUserOrganisations_returnEmptyCollection() throws NoSuchMethodException, NoSuchAlgorithmException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithOneOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        List<UserTaskViewModel> userTaskViewModelList = userTaskService.getUserTasks();

        //Then
        assertEquals(0, userTaskViewModelList.size());
    }

    @Test
    void getUserTasks_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> userTaskService.getUserTasks()
        );
    }

    @Test
    void getNextTask_whenProjectIdIsValidAndTaskWihStatusWaitingOrWaitingChangedOrInProgressAndCurrentUserIsLabeler_getNextTaskFromCurrentProject() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);

        testLabelTask.setProject(testProject);
        testProject.getLabelTasks().add(testLabelTask);

        //When
        when(labelTaskRepositoryMock.findByOwnerAndStateAndProjectIdAndMediaDeletedFalse(anyString(), anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock.saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskViewModel nextTask = userTaskService.getNextTask(PROJECT_ID);

        //Then
        assertEquals(LABEL_TASK_ID, nextTask.getTaskId());
        assertEquals(IMAGE_ID, nextTask.getMediaId());
        assertEquals(testLabelTask.getMedia().getMediaName(), nextTask.getMediaName());
        assertEquals(testLabelTask.getLabelIteration().getId(), nextTask.getIterationId());
        assertEquals(testLabelTask.getLabeler(), nextTask.getLabeler());
        assertEquals(LabelTaskState.IN_PROGRESS.name(), nextTask.getLabelTaskState());
        assertEquals(testLabelTask.getProject().getId(), nextTask.getProjectId());
        assertEquals(testLabelTask.getProject().getName(), nextTask.getProjectName());
        assertEquals(testLabelTask.getLabelIteration().getRun(), nextTask.getIterationRun());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void getNextTask_whenProjectIdIsNull_getNextTaskFromAllUserProjects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.WAITING);

        testLabelTask.setProject(testProject);
        testProject.getLabelTasks().add(testLabelTask);

        //When
        when(labelTaskRepositoryMock.findByOwnerAndStateAndMediaDeletedFalse(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock.saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskViewModel nextTask = userTaskService.getNextTask(null);

        //Then
        assertEquals(LABEL_TASK_ID, nextTask.getTaskId());
        assertEquals(IMAGE_ID, nextTask.getMediaId());
        assertEquals(testLabelTask.getMedia().getMediaName(), nextTask.getMediaName());
        assertEquals(testLabelTask.getLabelIteration().getId(), nextTask.getIterationId());
        assertEquals(testLabelTask.getLabeler(), nextTask.getLabeler());
        assertEquals(LabelTaskState.IN_PROGRESS.name(), nextTask.getLabelTaskState());
        assertEquals(testLabelTask.getProject().getId(), nextTask.getProjectId());
        assertEquals(testLabelTask.getProject().getName(), nextTask.getProjectName());
        assertEquals(testLabelTask.getLabelIteration().getRun(), nextTask.getIterationRun());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void getNextTask_whenNoNextTask_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(NoContentException.class,
                () -> userTaskService.getNextTask(PROJECT_ID)
        );
    }

    @Test
    void getNextTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> userTaskService.getNextTask(PROJECT_ID)
        );
    }

    @Test
    void getNextTask_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> userTaskService.getNextTask(PROJECT_ID)
        );
    }

    @Test
    void changeTaskStateAfterLabelConfigurationUpdate_whenConfigIdIsValidAndThereAreTaskWithStatusCompletedOrReviewed_changeTaskState() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(2, currentLoggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.COMPLETED);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.REVIEWED);

        //When
        when(labelTaskRepositoryMock
                .findTasksByLabelConfigurationIdAndTaskState(
                        anyString(),
                        anyList()))
                .thenReturn(testLabelTaskList);

        when(labelTaskRepositoryMock.save(any(LabelTask.class)))
                .then(returnsFirstArg());

        userTaskService.changeTaskStateAfterLabelConfigurationUpdate(LC_CONFIG_ID);

        //Then
        verify(labelTaskRepositoryMock, times(2)).save(any(LabelTask.class));
    }

    @Test
    void changeTaskStateAfterLabelConfigurationUpdate_whenConfigIdIsValidAndThereAreNOTaskWithStatusCompletedOrReviewed_nothingHappens() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(labelTaskRepositoryMock
                .findTasksByLabelConfigurationIdAndTaskState(
                        anyString(), anyList()))
                .thenReturn(new ArrayList<>());

        when(labelTaskRepositoryMock.save(any(LabelTask.class)))
                .then(returnsFirstArg());

        userTaskService.changeTaskStateAfterLabelConfigurationUpdate(LC_CONFIG_ID);

        //Then
        verify(labelTaskRepositoryMock, times(0)).save(any(LabelTask.class));
    }

    @Test
    void changeTaskStateAfterLabelConfigurationUpdate_whenTasksWithOtherStatesAsCompletedOrReviewed_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(2, currentLoggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.IN_PROGRESS);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.REVIEWED);

        //When
        when(labelTaskRepositoryMock
                .findTasksByLabelConfigurationIdAndTaskState(
                        anyString(), anyList()))
                .thenReturn(testLabelTaskList);

        when(labelTaskRepositoryMock.save(any(LabelTask.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> userTaskService.changeTaskStateAfterLabelConfigurationUpdate(LC_CONFIG_ID)
        );
    }

    @Test
    void changeTaskStateAfterLabelConfigurationUpdate_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> userTaskService.changeTaskStateAfterLabelConfigurationUpdate(LC_CONFIG_ID)
        );
    }

    @Test
    void getNextReviewTask_whenProjectIdIsValidAndTaskWihStatusCompleted_getNextReviewTask() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);

        testLabelTask.setProject(testProject);
        testProject.getLabelTasks().add(testLabelTask);

        //When
        when(labelTaskRepositoryMock.findNextTaskByProjectIdAndCompletedOrSkippedTaskState(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        LabelTaskViewModel nextTask = userTaskService.getNextReviewTask(PROJECT_ID);

        //Then
        assertEquals(LABEL_TASK_ID, nextTask.getTaskId());
        assertEquals(IMAGE_ID, nextTask.getMediaId());
        assertEquals(testLabelTask.getMedia().getMediaName(), nextTask.getMediaName());
        assertEquals(testLabelTask.getLabelIteration().getId(), nextTask.getIterationId());
        assertEquals(testLabelTask.getLabeler(), nextTask.getLabeler());
        assertEquals(LabelTaskState.COMPLETED.name(), nextTask.getLabelTaskState());
        assertEquals(testLabelTask.getProject().getId(), nextTask.getProjectId());
        assertEquals(testLabelTask.getProject().getName(), nextTask.getProjectName());
        assertEquals(testLabelTask.getLabelIteration().getRun(), nextTask.getIterationRun());

        verify(labelTaskRepositoryMock).findNextTaskByProjectIdAndCompletedOrSkippedTaskState(anyString(), anyString());
        verify(labelTaskRepositoryMock, times(1))
                .findNextTaskByProjectIdAndCompletedOrSkippedTaskState(anyString(), anyString());
    }

    @Test
    void getNextReviewTask_whenNoNextTask_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //When
        assertThrows(NoContentException.class,
                () -> userTaskService.getNextReviewTask(PROJECT_ID)
        );
    }

    @Test
    void getNextReviewTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> userTaskService.getNextReviewTask(PROJECT_ID)
        );
    }

    @Test
    void getNextReviewTask_whenUserIsNotAdminOrNoTasksToReview_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(NoContentException.class,
                () -> userTaskService.getNextReviewTask(PROJECT_ID)
        );
    }

    @Test
    void getNextReviewTask_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> userTaskService.getNextReviewTask(PROJECT_ID)
        );
    }
}
