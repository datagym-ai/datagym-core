package ai.datagym.application.project.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.errorHandling.AlreadyContainsException;
import ai.datagym.application.export.service.ExportService;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.lablerRating.repo.LabelerRatingRepository;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.prelLabeling.repo.PreLabelConfigRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.*;
import ai.datagym.application.user.service.UserInformationService;
import ai.datagym.application.utils.GoogleString;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class ProjectServiceImplTest {
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private DatasetRepository datasetRepositoryMock;

    @Mock
    private LabelConfigurationRepository labelConfigurationRepositoryMock;

    @Mock
    private LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    private LabelIterationRepository labelIterationRepositoryMock;

    @Mock
    private LabelTaskService labelTaskServiceMock;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private ExportService exportServiceMock;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private LabelerRatingRepository labelerRatingRepositoryMock;

    @Mock
    private PreLabelConfigRepository preLabelConfigurationRepositoryMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private UserInformationService userInformationServiceMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @BeforeEach
    void setUp() {
        projectRepositoryMock = mock(ProjectRepository.class);
        projectService = new ProjectServiceImpl(
                projectRepositoryMock,
                datasetRepositoryMock,
                labelConfigurationRepositoryMock,
                labelConfigurationServiceMock,
                labelIterationRepositoryMock,
                labelTaskServiceMock,
                labelTaskRepositoryMock,
                exportServiceMock,
                limitServiceMock,
                labelerRatingRepositoryMock,
                preLabelConfigurationRepositoryMock,
                lcEntryValueRepositoryMock,
                userInformationServiceMock,
                null, mediaRepositoryMock);
    }

    @Test
    void createProject_whenInputIsValidAndUserIsAuthorized_createProject() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ProjectCreateBindingModel projectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils.createTesLimitPricingPlanViewModel();

        when(projectRepositoryMock.saveAndFlush(any(Project.class))).thenReturn(testProject);
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        ProjectViewModel createdProject = projectService.createProject(projectCreateBindingModel, false);

        // Then
        assertEquals(projectCreateBindingModel.getName(), createdProject.getName());
        assertEquals(projectCreateBindingModel.getDescription(), createdProject.getDescription());
        assertEquals(projectCreateBindingModel.getShortDescription(), createdProject.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verifyNoMoreInteractions(projectRepositoryMock);
    }

    @Test
    void createProject_whenInputIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NullPointerException.class,
                () -> projectService.createProject(null, false)
        );
    }

    @Test
    void createProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        ProjectCreateBindingModel projectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectService.createProject(projectCreateBindingModel, false)
        );
    }

    @Test
    void createProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ProjectCreateBindingModel projectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();
        projectCreateBindingModel.setOwner("test_org");

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectService.createProject(projectCreateBindingModel, false)
        );
    }

    @Test
    void createProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ProjectCreateBindingModel projectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();
        projectCreateBindingModel.setOwner("datagym");


        Assertions.assertThrows(ForbiddenException.class,
                () -> projectService.createProject(projectCreateBindingModel, false)
        );
    }

    @Test
    void getProject_whenIdIsValidAndUserIsAuthorized_getProject() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        ProjectViewModel project = projectService.getProject(PROJECT_ID);

        //Then
        assertNotNull(project);
        assertEquals(testProject.getId(), project.getId());
        assertEquals(testProject.getName(), project.getName());
        assertEquals(testProject.getDescription(), project.getDescription());
        assertEquals(testProject.getShortDescription(), project.getShortDescription());
        assertEquals(testProject.isPinned(), project.isPinned());
        assertEquals(testProject.getOwner(), project.getOwner());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(projectRepositoryMock);
    }

    @Test
    void getProject_whenIdIsNotValid_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(NotFoundException.class,
                () -> projectService.getProject("invalid_project_id")
        );
    }

    @Test
    void getProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getProject(PROJECT_ID)
        );
    }

    @Test
    void getProject_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getProject(PROJECT_ID)
        );
    }

    @Test
    void getProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_owner");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getProject(PROJECT_ID)
        );
    }

    @Test
    void getProject_whenIdIsValidAndIsDeletedIsTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDeleted(true);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(NotFoundException.class,
                () -> projectService.getProject("invalid_dataset_id")
        );
    }

    @Test
    void getAllProjects_When2Projects_2Projects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner("eforce21")).thenReturn(testProjects);

        List<ProjectViewModel> allProjects = projectService.getAllProjects();

        //Then
        Project expected = testProjects.get(0);
        ProjectViewModel actual = allProjects.get(0);

        assertEquals(2, allProjects.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.isPinned(), actual.isPinned());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllProjects_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner("eforce21")).thenReturn(new ArrayList<>());
        List<ProjectViewModel> allProjects = projectService.getAllProjects();

        //Then
        assertTrue(allProjects.isEmpty());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllProjects_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(NullPointerException.class,
                () -> projectService.getAllProjects()
        );
    }

    @Test
    void updateProject_whenInputProjectIdValid_updateProject() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        ProjectViewModel actual = projectService.updateProject(PROJECT_ID, testProjectUpdateBindingModel);

        //Then
        assertNotNull(actual);
        assertEquals(testProjectUpdateBindingModel.getName(), actual.getName());
        assertEquals(testProjectUpdateBindingModel.getDescription(), actual.getDescription());
        assertEquals(testProjectUpdateBindingModel.getShortDescription(), actual.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void updateProject_whenInputProjectIdNotValid_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.updateProject("invalid_project_id", testProjectUpdateBindingModel)
        );
    }

    @Test
    void updateProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.updateProject(PROJECT_ID, testProjectUpdateBindingModel)
        );
    }

    @Test
    void updateProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.updateProject(PROJECT_ID, testProjectUpdateBindingModel)
        );
    }

    @Test
    void updateProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.updateProject(PROJECT_ID, testProjectUpdateBindingModel)
        );
    }

    @Test
    void updateProject_whenInputProjectIdNameAlreadyExists_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setName("test_name");
        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        when(projectRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.of(testProject));
        //Then
        assertThrows(AlreadyExistsException.class,
                () -> projectService.updateProject(PROJECT_ID, testProjectUpdateBindingModel)
        );
    }

    @Test
    void deleteProjectById_whenProjectIdIsValidAndDeleteProjectTrue_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        ProjectViewModel actual = projectService.deleteProjectById(PROJECT_ID, true);

        //Then
        assertTrue(actual.isDeleted());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteProjectById_whenProjectIdIsValidAndDeleteProjectFalse_setDeletedToFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDeleted(true);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        ProjectViewModel actual = projectService.deleteProjectById(PROJECT_ID, false);

        //Then
        assertFalse(actual.isDeleted());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteProjectById_whenProjectIdIsValidAndDeleteProjectFalseAndDatasetNameIsNotUnique_setDeletedToFalseAndAddUUIDToName() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        String testProjectName = testProject.getName();
        testProject.setDeleted(true);

        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());
        when(projectRepositoryMock.findAllByName(anyString())).thenReturn(testProjects);

        ProjectViewModel actual = projectService.deleteProjectById(PROJECT_ID, false);

        //Then
        assertFalse(actual.isDeleted());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());
        assertEquals(testProjectName.length() + 37, actual.getName().length());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteProjectById_whenProjectIdIsValidAndDeleteProjectFalseAndDatasetNameIsNotUniqueAndNameIsMoreThan89Chars_setDeletedToFalseAndCutTheNameTo89CharsAddUUIDToName() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setName("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua");
        testProject.setDeleted(true);

        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());
        when(projectRepositoryMock.findAllByName(anyString())).thenReturn(testProjects);

        ProjectViewModel actual = projectService.deleteProjectById(PROJECT_ID, false);

        //Then
        assertFalse(actual.isDeleted());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());
        assertEquals(126, actual.getName().length());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteProjectById_whenProjectIdIsNotValidAndDeleteProjectTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);


        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.deleteProjectById("invalid_project_id", true)
        );
    }

    @Test
    void deleteProjectById_whenProjectIdIsNotValidAndDeleteProjectFalse_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.deleteProjectById("invalid_project_id", false)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotAuthenticated_throwException() {
        SecurityContext.set(null);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.deleteProjectById(PROJECT_ID, true)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.deleteProjectById(PROJECT_ID, true)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.deleteProjectById(PROJECT_ID, true)
        );
    }

    @Test
    void pinProject_whenProjectIdIsValidAndSetToPinnedTrue_setPinnedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        ProjectViewModel actual = projectService.pinProject(PROJECT_ID, true);

        //Then
        assertTrue(actual.isPinned());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void pinProject_whenProjectIdIsValidAndSetToPinnedFalse_setPinnedToFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setPinned(true);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(projectRepositoryMock.saveAndFlush(any(Project.class))).then(returnsFirstArg());

        ProjectViewModel actual = projectService.pinProject(PROJECT_ID, false);

        //Then
        assertFalse(actual.isPinned());

        assertNotNull(actual);
        assertEquals(testProject.getName(), actual.getName());
        assertEquals(testProject.getDescription(), actual.getDescription());
        assertEquals(testProject.getShortDescription(), actual.getShortDescription());

        verify(projectRepositoryMock).saveAndFlush(any());
        verify(projectRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void pinProject_whenProjectIdIsNotValidAndSetToPinnedTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.pinProject("invalid_project_id", true)
        );
    }

    @Test
    void pinProject_whenProjectIdIsNotValidAndSetToPinnedFalse_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.pinProject("invalid_project_id", false)
        );
    }

    @Test
    void pinProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.pinProject(PROJECT_ID, true)
        );
    }

    @Test
    void pinProject_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.pinProject(PROJECT_ID, true)
        );
    }

    @Test
    void pinProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //when
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.pinProject(PROJECT_ID, true)
        );
    }

    @Test
    void permanentDeleteProjectFromDB_whenProjectIdIsValid_permanentDeleteProjectFromDB() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        projectService.permanentDeleteProjectFromDB(PROJECT_ID);

        //Then
        verify(projectRepositoryMock).delete(any(Project.class));
        verify(projectRepositoryMock, times(1)).delete(any(Project.class));
    }

    @Test
    void permanentDeleteProjectFromDB_whenProjectIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.permanentDeleteProjectFromDB("invalid_project_id")
        );
    }

    @Test
    void permanentDeleteProjectFromDB_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.permanentDeleteProjectFromDB(PROJECT_ID)
        );
    }

    @Test
    void permanentDeleteProjectFromDB_whenUserIsNotRoot_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.permanentDeleteProjectFromDB(PROJECT_ID)
        );
    }

    @Test
    void permanentDeleteProjectFromDB_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.permanentDeleteProjectFromDB(PROJECT_ID)
        );
    }

    @Test
    void isProjectNameUniqueAndDeletedFalse_whenUsernameIsUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.of(testProject));

        boolean actual = projectService.isProjectNameUniqueAndDeletedFalse(PROJECT_NAME, "eforce");

        //Then
        Assertions.assertFalse(actual);
        verify(projectRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(projectRepositoryMock, times(1)).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
    }

    @Test
    void isProjectNameUniqueAndDeletedFalse_whenUsernameIsNotUnique_returnFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.empty());

        boolean actual = projectService.isProjectNameUniqueAndDeletedFalse(PROJECT_NAME, "eforce");

        //Then
        Assertions.assertTrue(actual);
        verify(projectRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(projectRepositoryMock, times(1)).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
    }

    @Test
    void addDataset_whenProjectDoesNotContainThisDataset_addDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.getLabelIteration().setId(LC_ITERATION_ID);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        testDataset.getMedia().add(testMedia);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(labelTaskServiceMock.createLabelTask(anyString(), anyString(), anyString()))
                .thenReturn(labelTask);

        projectService.addDataset(PROJECT_ID, DATASET_ID);

        //Then
        Assertions.assertEquals(1, testProject.getDatasets().size());
    }

    @Test
    void addDataset_whenProjectNameIsDummyProject_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.getLabelIteration().setId(LC_ITERATION_ID);
        testProject.setName("Dummy_Project");

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenDatasetNameIsDummyDatasetOne_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        testDataset.setName("Dummy_Dataset_One");

        testProject.getDatasets().add(testDataset);

        // When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenDatasetNameIsDummyDatasetTwo_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        testDataset.setName("Dummy_Dataset_Two");

        testProject.getDatasets().add(testDataset);

        // When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenProjectContainsThisDataset_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        testProject.getDatasets().add(testDataset);

        // When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(AlreadyContainsException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenProjectAndDatasetDontHaveTheSameOwner_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        testProject.getDatasets().add(testDataset);

        // When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("datagym");

        testProject.getDatasets().add(testDataset);

        // When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void removeDataset_whenProjectContainsThisDataset_removeDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.getLabelIteration().setId(LC_ITERATION_ID);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        testDataset.getMedia().add(testMedia);

        testProject.getDatasets().add(testDataset);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        projectService.removeDataset(PROJECT_ID, DATASET_ID);

        //Then
        Assertions.assertEquals(0, testProject.getDatasets().size());
    }

    @Test
    void removeDataset_whenProjectDoesNotContainThisDataset_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                () -> projectService.removeDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void removeDataset_whenProjectAndDatasetDontHaveTheSameOwner_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_owner");
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.removeDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void removeDataset_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("datagym");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.removeDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void getProjectTasks_When2Tasks_2Tasks() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        List<LabelTask> labelTasks = LabelTaskUtils.createTestLabelTaskList(2, "eforce21");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelTaskRepositoryMock
                .search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt()))
                .thenReturn(labelTasks);

        List<LabelTaskViewModel> labelTaskViewModelList = projectService.getProjectTasks(PROJECT_ID, "test", LabelTaskState.BACKLOG, 2);

        //Then
        LabelTask expected = labelTasks.get(0);
        LabelTaskViewModel actual = labelTaskViewModelList.get(0);

        assertEquals(2, labelTaskViewModelList.size());
        assertEquals(expected.getId(), actual.getTaskId());
        assertEquals(expected.getProject().getId(), actual.getProjectId());
        assertEquals(expected.getProject().getName(), actual.getProjectName());
        assertEquals(expected.getLabelTaskState().name(), actual.getLabelTaskState());
        assertEquals(expected.getMedia().getId(), actual.getMediaId());
        assertEquals(expected.getMedia().getMediaName(), actual.getMediaName());
        assertEquals(expected.getLabelIteration().getId(), actual.getIterationId());
        assertEquals(expected.getLabelIteration().getRun(), actual.getIterationRun());

        verify(labelTaskRepositoryMock).search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt());
        verify(labelTaskRepositoryMock, times(1)).search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt());
    }

    @Test
    void getProjectTasks_WhenZeroTasks_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        List<LabelTask> labelTasks = LabelTaskUtils.createTestLabelTaskList(2, "eforce21");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelTaskRepositoryMock
                .search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt()))
                .thenReturn(new ArrayList<>());

        List<LabelTaskViewModel> labelTaskViewModelList = projectService
                .getProjectTasks(PROJECT_ID, "test", LabelTaskState.BACKLOG, 2);

        //Then
        LabelTask expected = labelTasks.get(0);

        assertEquals(0, labelTaskViewModelList.size());

        verify(labelTaskRepositoryMock).search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt());
        verify(labelTaskRepositoryMock, times(1)).search(anyString(), any(GoogleString.class), any(LabelTaskState.class), anyBoolean(), anyInt());

    }

    @Test
    void getProjectTasks_whenProjectIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.getProjectTasks("invalid_project_id", "test", LabelTaskState.BACKLOG, 2)
        );
    }

    @Test
    void getProjectTasks_whenProjectIsDeleted_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDeleted(true);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        //Then
        assertThrows(NotFoundException.class,
                () -> projectService.getProjectTasks(PROJECT_ID, "test", LabelTaskState.BACKLOG, 2)
        );
    }

    @Test
    void getProjectTasks_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.getProjectTasks(PROJECT_ID, "test", LabelTaskState.BACKLOG, 2)
        );
    }

    @Test
    void getAllProjectsFromOrganisation_When2Projects_2Projects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString())).thenReturn(testProjects);

        List<ProjectViewModel> allProjects = projectService.getAllProjectsFromOrganisation("eforce21");

        //Then
        Project expected = testProjects.get(0);
        ProjectViewModel actual = allProjects.get(0);

        assertEquals(2, allProjects.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.isPinned(), actual.isPinned());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
    }

    @Test
    void getAllProjectsFromOrganisation_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString())).thenReturn(new ArrayList<>());
        List<ProjectViewModel> allProjects = projectService.getAllProjectsFromOrganisation("eforce21");

        //Then
        assertTrue(allProjects.isEmpty());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
    }

    @Test
    void getAllProjectsFromOrganisation_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.getAllProjectsFromOrganisation("eforce21")
        );
    }

    @Test
    void getAllProjectsFromOrganisationAndLoggedInUserIsAdmin_When2Projects_2Projects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString())).thenReturn(testProjects);

        List<ProjectViewModel> allProjects = projectService
                .getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();

        //Then
        Project expected = testProjects.get(0);
        ProjectViewModel actual = allProjects.get(0);

        assertEquals(2, allProjects.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.isPinned(), actual.isPinned());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
    }

    @Test
    void getAllProjectsFromOrganisationAndLoggedInUserIsAdmin_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString())).thenReturn(new ArrayList<>());
        List<ProjectViewModel> allProjects = projectService
                .getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();

        //Then
        assertTrue(allProjects.isEmpty());

        verify(projectRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
    }

    @Test
    void getAllProjectsFromOrganisationAndLoggedInUserIsAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> projectService.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin()
        );
    }

    @Test
    void getDashboardData_whenIdIsValidAndUserIsAuthorized_getDashboardData() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils.createTesLimitPricingPlanViewModel();

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        when(labelTaskRepositoryMock.countProjectTasksWhereMediasNotDeleted(anyString())).thenReturn(0);
        when(labelTaskRepositoryMock.countProjectTasksByStateWhereMediasNotDeleted(anyString(), any())).thenReturn(0L);
        when(labelTaskRepositoryMock.countProjectTasksByMediaTypeWhereMediasNotDeleted(anyString(), any())).thenReturn(0L);

        ProjectDashboardViewModel dashboardData = projectService.getDashboardData(PROJECT_ID);

        //Then
        assertNotNull(dashboardData);
        assertEquals(testProject.getId(), dashboardData.getId());
        assertEquals(testProject.getName(), dashboardData.getName());
        assertEquals(testProject.getDescription(), dashboardData.getDescription());
        assertEquals(testProject.getShortDescription(), dashboardData.getShortDescription());
        assertEquals(testProject.isPinned(), dashboardData.isPinned());
        assertEquals(testProject.getOwner(), dashboardData.getOwner());
        assertEquals("FREE_DEVELOPER", dashboardData.getCurrentPlan());
        assertEquals(0, dashboardData.getCountTasks());
        assertEquals(0, dashboardData.getCountDatasets());
        assertEquals(0, dashboardData.getApprovedReviewPerformance());
        assertEquals(0, dashboardData.getDeclinedReviewPerformance());
        assertEquals(LabelTaskState.values().length, dashboardData.getTaskStatus().size());
        assertEquals(MediaSourceType.values().length, dashboardData.getTaskMediaDetail().size());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(projectRepositoryMock);
    }

    @Test
    void getDashboardData_whenIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> projectService.getDashboardData("invalid_project_id")
        );
    }

    @Test
    void getDashboardData_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getDashboardData(PROJECT_ID)
        );
    }

    @Test
    void getDashboardData_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getDashboardData(PROJECT_ID)
        );
    }

    @Test
    void getDashboardData_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_owner");

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getDashboardData(PROJECT_ID)
        );
    }

    @Test
    void getDashboardData_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testProject));

        assertThrows(ForbiddenException.class,
                () -> projectService.getDashboardData(PROJECT_ID)
        );
    }
}

