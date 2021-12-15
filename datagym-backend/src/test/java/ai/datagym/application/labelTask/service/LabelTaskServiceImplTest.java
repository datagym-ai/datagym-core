package ai.datagym.application.labelTask.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.awsS3.AwsS3HelperService;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueExtendAllBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
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
import ai.datagym.application.media.entity.LocalImage;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LabelTaskServiceImplTest {
    private LabelTaskService labelTaskService;

    @Value(value = "${datagym.deactivate-limiter}")
    private boolean deactivateLimiter;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private LabelIterationRepository labelIterationRepositoryMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private DatasetRepository datasetRepositoryMock;

    @Mock
    private LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    private LcEntryValueService lcEntryValueServiceMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private LabelerRatingService labelerRatingServiceMock;

    @Mock
    private AwsS3HelperService awsS3HelperServiceMock;

    LabelTaskServiceImplTest() {
    }

    @BeforeEach
    void setUp() {
        labelTaskService = new LabelTaskServiceImpl(
                labelTaskRepositoryMock,
                labelIterationRepositoryMock,
                mediaRepositoryMock,
                projectRepositoryMock,
                datasetRepositoryMock,
                labelConfigurationServiceMock,
                lcEntryValueServiceMock,
                lcEntryValueRepositoryMock,
                limitServiceMock,
                deactivateLimiter,
                labelerRatingServiceMock,
                Optional.of(awsS3HelperServiceMock));
    }

    @Test
    void createLabelTask_whenInputIsValidAndUserIsAuthorized_createLabelTask() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(labelTaskRepositoryMock.save(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTask createdLabelTask = labelTaskService.createLabelTask(PROJECT_ID, IMAGE_ID, LC_ITERATION_ID);

        //Then
        assertEquals(testMedia.getId(), createdLabelTask.getMedia().getId());
        assertEquals(testLabelIteration.getId(), createdLabelTask.getLabelIteration().getId());
        assertEquals(testProject.getId(), createdLabelTask.getProject().getId());
        assertEquals("BACKLOG", createdLabelTask.getLabelTaskState().name());
        assertNull(createdLabelTask.getLabeler());

        verify(labelTaskRepositoryMock).save(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).save(any(LabelTask.class));
        verifyNoMoreInteractions(labelTaskRepositoryMock);
    }

    @Test
    void createLabelTask_whenInputIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        Assertions.assertThrows(NotFoundException.class,
                () -> labelTaskService.createLabelTask(
                        "invalid_projectId",
                        "invalid_imageId",
                        "invalid_iterationId")
        );
    }

    @Test
    void createLabelTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.createLabelTask(
                        PROJECT_ID, IMAGE_ID, LC_ITERATION_ID)
        );
    }

    @Test
    void createLabelTask_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.createLabelTask(
                        PROJECT_ID, IMAGE_ID, LC_ITERATION_ID)
        );
    }

    @Test
    void createProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.createLabelTask(
                        PROJECT_ID, IMAGE_ID, LC_ITERATION_ID)
        );
    }

    @Test
    void deleteLabelTaskById_inputsAreValid_deleteLabelTask() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        testLabelTask.setMedia(testMedia);
        testMedia.getLabelTasks().add(testLabelTask);

        testLabelTask.setProject(testProject);
        testProject.getLabelTasks().add(testLabelTask);

        testLabelTask.setLabelIteration(testLabelIteration);
        testLabelIteration.getLabelTasks().add(testLabelTask);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));


        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        labelTaskService.deleteLabelTaskByIdInternal(LABEL_TASK_ID);


        verify(labelTaskRepositoryMock).delete(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).delete(any(LabelTask.class));
    }



    @Test
    void deleteAllLabelTasksFromDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        List<String> tesSetImages = ImageUtils.createTesSetImages(2);
        testProject.setLabelIteration(testLabelIteration);


        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deleteAllLabelTasksFromDataset(testProject, tesSetImages)
        );
    }

    @Test
    void deleteAllLabelTasksFromDataset_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        List<String> tesSetImages = ImageUtils.createTesSetImages(2);
        testProject.setLabelIteration(testLabelIteration);

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deleteAllLabelTasksFromDataset(testProject, tesSetImages)
        );
    }

    @Test
    void deleteAllLabelTasksFromDataset_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        List<String> tesSetImages = ImageUtils.createTesSetImages(2);
        testProject.setLabelIteration(testLabelIteration);

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deleteAllLabelTasksFromDataset(testProject, tesSetImages)
        );
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenInputsAreValidAndUserIsAdmin_moveTaskState() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskViewModel labelTaskViewModel = labelTaskService
                .moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.WAITING);

        //Then
        assertEquals(LABEL_TASK_ID, labelTaskViewModel.getTaskId());
        assertEquals(IMAGE_ID, labelTaskViewModel.getMediaId());
        assertEquals(testLabelTask.getMedia().getMediaName(), labelTaskViewModel.getMediaName());
        assertEquals(testLabelTask.getLabelIteration().getId(), labelTaskViewModel.getIterationId());
        assertEquals(testLabelTask.getLabeler(), labelTaskViewModel.getLabeler());
        assertEquals(LabelTaskState.WAITING.name(), labelTaskViewModel.getLabelTaskState());
        assertEquals(testLabelTask.getProject().getId(), labelTaskViewModel.getProjectId());
        assertEquals(testLabelTask.getProject().getName(), labelTaskViewModel.getProjectName());
        assertEquals(testLabelTask.getLabelIteration().getRun(), labelTaskViewModel.getIterationRun());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService
                        .moveTaskStateIfUserIsAdmin(
                                "invalid_task_id",
                                LabelTaskState.WAITING)
        );
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenLabelTaskStateIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(GenericException.class,
                () -> labelTaskService
                        .moveTaskStateIfUserIsAdmin(
                                LABEL_TASK_ID,
                                null)
        );
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveTaskStateIfUserIsAdmin(
                                LABEL_TASK_ID,
                                LabelTaskState.WAITING)

        );
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveTaskStateIfUserIsAdmin(
                                LABEL_TASK_ID,
                                LabelTaskState.WAITING)
        );
    }

    @Test
    void moveTaskStateIfUserIsAdmin_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveTaskStateIfUserIsAdmin(
                                LABEL_TASK_ID,
                                LabelTaskState.WAITING)
        );
    }

    @Test
    void skipTask_whenTaskIdIsValidAndUserIsAdmin_skipTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.skipTask(LABEL_TASK_ID);

        //Then
        assertEquals(LabelTaskState.SKIPPED, testLabelTask.getLabelTaskState());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void skipTask_whenTaskIdIsValidAndUserIsLabelerAndNotAdminAndTaskStateIsInProgress_skipTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");
        testLabelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.skipTask(LABEL_TASK_ID);

        //Then
        assertEquals(LabelTaskState.SKIPPED, testLabelTask.getLabelTaskState());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void skipTask_whenUserIsLabelerAndNotAdminAndTaskStateIsNotInProgress_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");
        testLabelTask.setLabelTaskState(LabelTaskState.BACKLOG);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> labelTaskService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void skipTask_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void skipTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void skipTask_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void moveTaskToReviewed_whenTaskIdIsValidAndUserIsAdmin_moveTaskToReviewed() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.moveTaskToReviewed(LABEL_TASK_ID);

        //Then
        assertEquals(LabelTaskState.REVIEWED, testLabelTask.getLabelTaskState());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void moveTaskToReviewed_whenTaskIdIsValidAndUserIsAdminAndTaskStateIsNotCOMPLETED_moveTaskToReviewed() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.WAITING);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.moveTaskToReviewed(LABEL_TASK_ID);

        //Then
        assertEquals(LabelTaskState.REVIEWED, testLabelTask.getLabelTaskState());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void moveTaskToReviewed_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.moveTaskToReviewed(LABEL_TASK_ID)
        );
    }

    @Test
    void moveTaskToReviewed_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.moveTaskToReviewed(LABEL_TASK_ID)
        );
    }

    @Test
    void moveTaskToReviewed_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.moveTaskToReviewed(LABEL_TASK_ID)
        );
    }

    @Test
    void moveAllTasks_whenInputsAreValidAndDatasetIdIsNotEqualToAll_moveAllTasks() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(1, currentLoggedInUserId);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        Media media = testLabelTaskList.get(0).getMedia();
        testDataset.getMedia().add(media);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(labelTaskRepositoryMock
                .findAllByProjectIdAndLabelTaskStateAndMediaDeleted(anyString(), any(LabelTaskState.class), anyBoolean()))
                .thenReturn(testLabelTaskList);

        labelTaskService.moveAllTasks(testLabelTaskMoveAllBindingModel);

        //Then
        verify(labelTaskRepositoryMock, times(1))
                .updateLabelTaskStateByProjectIdAndDatasetId(anyString(), anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
        verify(labelTaskRepositoryMock, never()).updateLabelTaskStateByProjectId(anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
    }

    @Test
    void moveAllTasks_whenInputsAreValidAndDatasetIdIsEqualToAll_moveAllTasks() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDatasetId("ALL");
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(1, currentLoggedInUserId);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        Media media = testLabelTaskList.get(0).getMedia();
        testDataset.getMedia().add(media);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(labelTaskRepositoryMock
                .findAllByProjectIdAndLabelTaskStateAndMediaDeleted(anyString(), any(LabelTaskState.class), anyBoolean()))
                .thenReturn(testLabelTaskList);

        labelTaskService.moveAllTasks(testLabelTaskMoveAllBindingModel);

        //Then
        verify(labelTaskRepositoryMock, times(1))
                .updateLabelTaskStateByProjectId(anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
        verify(labelTaskRepositoryMock, never()).updateLabelTaskStateByProjectIdAndDatasetId(anyString(), anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
    }

    @Test
    void moveAllTasks_whenInputsAreValidAndDatasetIdIsNotEqualToAllAndDirectionIsBACKLOG_moveAllTasks() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(1, currentLoggedInUserId);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        Media media = testLabelTaskList.get(0).getMedia();
        testDataset.getMedia().add(media);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(labelTaskRepositoryMock
                .findAllByProjectIdAndLabelTaskStateAndMediaDeleted(anyString(), any(LabelTaskState.class), anyBoolean()))
                .thenReturn(testLabelTaskList);

        labelTaskService.moveAllTasks(testLabelTaskMoveAllBindingModel);


        //Then

        verify(labelTaskRepositoryMock, times(2))
                .updateLabelTaskStateByProjectIdAndDatasetId(anyString(), anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
        verify(labelTaskRepositoryMock, never()).updateLabelTaskStateByProjectId(anyString(), any(LabelTaskState.class), any(LabelTaskState.class));
    }

    @Test
    void moveAllTasks_whenDirectionIsNotBacklogOrWaiting_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("completed");

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(GenericException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void moveAllTasks_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void moveAllTasks_whenProjectIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void moveAllTasks_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void moveAllTasks_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void moveAllTasks_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setDirection("backlog");

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock.findByIdAndDeletedIsFalse(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService
                        .moveAllTasks(testLabelTaskMoveAllBindingModel)
        );
    }

    @Test
    void getTask_whenTaskIdIsValid_getTask() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        LocalImage testLocalImage = ImageUtils.createTestLocalImage();

        testLabelTask.setMedia(testLocalImage);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils.createTestLabelIterationViewModel();

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelConfigurationServiceMock
                .getLabelConfiguration(LC_CONFIG_ID))
                .thenReturn(testLabelConfigurationViewModel);

        when(lcEntryValueServiceMock
                .extendAllConfigEntryValues(anyString(), any(LcEntryValueExtendAllBindingModel.class)))
                .thenReturn(testLabelIterationViewModel);

        LabelModeDataViewModel actual = labelTaskService.getTask(LABEL_TASK_ID);

        //Then
        assertEquals(testLabelTask.getId(), actual.getTaskId());
        assertEquals(testLabelTask.getMedia().getId(), actual.getMedia().getId());
        assertEquals(testLabelConfigurationViewModel.getId(), actual.getLabelConfig().getId());
        assertEquals(testLabelIterationViewModel.getId(), actual.getLabelIteration().getId());

        verify(labelTaskRepositoryMock).findById(anyString());
        verify(labelTaskRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelTaskRepositoryMock);
    }

    @Test
    void getTask_whenTaskIdIsNotValid_throwException() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.getTask("invalid_task_id")
        );
    }

    @Test
    void getTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask("invalid_user");

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.getTask(LABEL_TASK_ID)
        );
    }

    @Test
    void getTask_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.getTask(LABEL_TASK_ID)
        );
    }

    @Test
    void getTask_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.getTask(LABEL_TASK_ID)
        );
    }

    @Test
    void completeTask_whenTaskIdIsValidAndConfigHasNotChanged_completeTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskCompleteViewModel labelTaskCompleteViewModel = labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel);

        //Then
        assertEquals(LabelTaskState.COMPLETED, testLabelTask.getLabelTaskState());
        assertEquals(LABEL_TASK_ID, labelTaskCompleteViewModel.getCurrentTaskId());
        assertFalse(labelTaskCompleteViewModel.isHasLabelConfigChanged());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void completeTask_whenTaskIdIsValidAndConfigHasChanged_completeTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();
        testLabelTaskCompleteBindingModel.setLastChangedConfig(500L);

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskCompleteViewModel labelTaskCompleteViewModel = labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel);

        //Then
        assertEquals(LabelTaskState.BACKLOG, testLabelTask.getLabelTaskState());
        assertEquals(LABEL_TASK_ID, labelTaskCompleteViewModel.getCurrentTaskId());
        assertTrue(labelTaskCompleteViewModel.isHasLabelConfigChanged());
    }

    @Test
    void completeTask_whenTaskIdIsValidAndUserIsLabelerAndNotAdminAndTaskStateIsInProgress_completeTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");
        testLabelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        LabelTaskCompleteViewModel labelTaskCompleteViewModel = labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel);

        //Then
        assertEquals(LabelTaskState.COMPLETED, testLabelTask.getLabelTaskState());
        assertEquals(LABEL_TASK_ID, labelTaskCompleteViewModel.getCurrentTaskId());
        assertFalse(labelTaskCompleteViewModel.isHasLabelConfigChanged());

        verify(labelTaskRepositoryMock).saveAndFlush(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).saveAndFlush(any(LabelTask.class));
    }

    @Test
    void completeTask_whenUserIsLabelerAndNotAdminAndTaskStateIsNotInProgress_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");
        testLabelTask.setLabelTaskState(LabelTaskState.BACKLOG);

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .saveAndFlush(any(LabelTask.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void completeTask_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void completeTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void completeTask_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        LabelConfiguration labelConfiguration = testLabelTask.getProject().getLabelConfiguration();
        labelConfiguration.setTimestamp(1000L);

        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void reviewCompletion_inputsAreValidAndSuccessIsTrue_addToPositiveRatings() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);

        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, true);

        //Then
        ArgumentCaptor<LabelerRatingUpdateBindingModel> labelerRatingUpdateBindingModelCapture = ArgumentCaptor
                .forClass(LabelerRatingUpdateBindingModel.class);

        verify(labelerRatingServiceMock).addToPositive(labelerRatingUpdateBindingModelCapture.capture());
        assertThat(labelerRatingUpdateBindingModelCapture.getValue().getLabelerId())
                .isEqualTo(testLabelTask.getLabeler());
        assertThat(labelerRatingUpdateBindingModelCapture.getValue().getProjectId())
                .isEqualTo(testLabelTask.getProject().getId());

        verify(labelerRatingServiceMock, times(1)).addToPositive(any());
    }

    @Test
    void reviewCompletion_inputsAreValidAndSuccessIsFasle_addToNegativeRatings() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);

        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, false);

        //Then
        ArgumentCaptor<LabelerRatingUpdateBindingModel> labelerRatingUpdateBindingModelCapture = ArgumentCaptor
                .forClass(LabelerRatingUpdateBindingModel.class);

        verify(labelerRatingServiceMock).addToNegative(labelerRatingUpdateBindingModelCapture.capture());
        assertThat(labelerRatingUpdateBindingModelCapture.getValue().getLabelerId())
                .isEqualTo(testLabelTask.getLabeler());
        assertThat(labelerRatingUpdateBindingModelCapture.getValue().getProjectId())
                .isEqualTo(testLabelTask.getProject().getId());

        verify(labelerRatingServiceMock, times(1)).addToNegative(any());
    }

    @Test
    void reviewCompletion_whenLabelTaskStateIsNotCompletedOrSkipped_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.WAITING);

        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(GenericException.class,
                () -> labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, true)
        );
    }

    @Test
    void reviewCompletion_whenLabelTaskIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Given
        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, true)
        );
    }

    @Test
    void reviewCompletion_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, true)
        );
    }


    @Test
    void reviewCompletion_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        testLabelTask.setProject(testProject);

        // Given
        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        // When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        //Then
        assertThrows(ForbiddenException.class,
                () -> labelTaskService.reviewCompletion(testLabelTaskReviewBindingModel, true)
        );
    }

    @Test
    void activateBenchmark_whenTaskIdIsValidAndUserIsAdmin_activateBenchmark() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .save(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.activateBenchmark(LABEL_TASK_ID);

        //Then
        assertTrue(testLabelTask.isBenchmark());

        verify(labelTaskRepositoryMock).save(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).save(any(LabelTask.class));
    }

    @Test
    void activateBenchmark_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.activateBenchmark("invalid_task_id")
        );
    }

    @Test
    void activateBenchmark_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.activateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void activateBenchmark_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.activateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void activateBenchmark_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.activateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void deactivateBenchmark_whenTaskIdIsValidAndUserIsAdmin_deactivateBenchmark() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .save(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.deactivateBenchmark(LABEL_TASK_ID);

        //Then
        assertFalse(testLabelTask.isBenchmark());

        verify(labelTaskRepositoryMock).save(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).save(any(LabelTask.class));
    }

    @Test
    void deactivateBenchmark_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.deactivateBenchmark("invalid_task_id")
        );
    }

    @Test
    void deactivateBenchmark_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deactivateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void deactivateBenchmark_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deactivateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void deactivateBenchmark_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.deactivateBenchmark(LABEL_TASK_ID)
        );
    }

    @Test
    void resetLabeler_whenTaskIdIsValidAndUserIsAdmin_resetLabeler() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.COMPLETED);
        LabelTaskState taskStateBefore = testLabelTask.getLabelTaskState();


        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .save(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.resetLabeler(LABEL_TASK_ID);

        //Then
        assertNull(testLabelTask.getLabeler());
        assertEquals(testLabelTask.getLabelTaskState(), taskStateBefore);

        verify(labelTaskRepositoryMock).save(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).save(any(LabelTask.class));
    }

    @Test
    void resetLabeler_whenTaskIdIsValidAndUserIsAdmin_taskInProgress_resetLabeler() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.setLabelTaskState(LabelTaskState.IN_PROGRESS);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .save(any(LabelTask.class)))
                .then(returnsFirstArg());

        labelTaskService.resetLabeler(LABEL_TASK_ID);

        //Then
        assertNull(testLabelTask.getLabeler());
        assertEquals(testLabelTask.getLabelTaskState(), LabelTaskState.WAITING_CHANGED);

        verify(labelTaskRepositoryMock).save(any(LabelTask.class));
        verify(labelTaskRepositoryMock, times(1)).save(any(LabelTask.class));
    }

    @Test
    void resetLabeler_illegalTaskStates_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);

        //When
        when(labelTaskRepositoryMock
                .findById(LABEL_TASK_ID))
                .thenReturn(java.util.Optional.of(testLabelTask));

        when(labelTaskRepositoryMock
                .save(any(LabelTask.class)))
                .then(returnsFirstArg());

        //Then
        testLabelTask.setLabelTaskState(LabelTaskState.WAITING);
        assertThrows(GenericException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );

        testLabelTask.setLabelTaskState(LabelTaskState.BACKLOG);
        assertThrows(GenericException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );

        testLabelTask.setLabelTaskState(LabelTaskState.REVIEWED);
        assertThrows(GenericException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );

        testLabelTask.setLabelTaskState(LabelTaskState.REVIEWED_SKIP);
        assertThrows(GenericException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );
    }

    @Test
    void resetLabeler_whenLabelTaskIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> labelTaskService.resetLabeler("invalid_task_id")
        );
    }

    @Test
    void resetLabeler_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(null);

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );
    }


    @Test
    void resetLabeler_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("datagym");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );
    }

    @Test
    void resetLabeler_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String currentLoggedInUserId = oauthUser.id();

        //Given
        LabelTask testLabelTask = LabelTaskUtils.createTestLabelTask(currentLoggedInUserId);
        testLabelTask.getProject().setOwner("test_org");

        //When
        when(labelTaskRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelTask));

        assertThrows(ForbiddenException.class,
                () -> labelTaskService.resetLabeler(LABEL_TASK_ID)
        );
    }
}