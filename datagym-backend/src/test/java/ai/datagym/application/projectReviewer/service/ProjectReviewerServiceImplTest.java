package ai.datagym.application.projectReviewer.service;

import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.projectReviewer.repo.ProjectReviewerRepository;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import ai.datagym.application.security.service.UserInfoService;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.ReviewerUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import ai.datagym.application.testUtils.UserInfoUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static ai.datagym.application.testUtils.ReviewerUtils.REVIEWER_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class ProjectReviewerServiceImplTest {
    private ProjectReviewerService projectReviewerService;

    @Mock
    private ProjectReviewerRepository projectReviewerRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private UserInfoService userInfoServiceMock;

    @BeforeEach
    void setUp() {
        projectReviewerService = new ProjectReviewerServiceImpl(projectReviewerRepositoryMock, projectRepositoryMock, userInfoServiceMock);
    }

    @Test
    void createReviewer_whenInputIsValidAndUserIsAuthorized_createReviewer() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        List<UserMinInfoViewModel> userMinInfoViewModelList = UserInfoUtils.createUserMinInfoViewModelList(2);

        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();
        reviewerCreateBindingModel.setUserId(userMinInfoViewModelList.get(0).getId());

        ProjectReviewer testProjectReviewer = ReviewerUtils.createTestProjectReviewer();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(projectReviewerRepositoryMock.save(any(ProjectReviewer.class)))
                .thenReturn(testProjectReviewer);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(userInfoServiceMock.getAllUsersFromOrg(anyString(), any()))
                .thenReturn(userMinInfoViewModelList);

        ProjectReviewerViewModel reviewerViewModel = projectReviewerService.createReviewer(reviewerCreateBindingModel);

        // Then
        assertEquals(reviewerCreateBindingModel.getUserId(), reviewerViewModel.getUserInfo().getId());
        assertEquals(reviewerCreateBindingModel.getProjectId(), reviewerViewModel.getProjectId());
        assertNotNull(reviewerViewModel.getReviewerId());
        assertNotNull(reviewerViewModel.getTimeStamp());

        verify(projectReviewerRepositoryMock).save(any());
        verifyNoMoreInteractions(projectReviewerRepositoryMock);
    }

    @Test
    void createReviewer_whenUserIsAlreadyReviewerForCurrentProject_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        List<UserMinInfoViewModel> userMinInfoViewModelList = UserInfoUtils.createUserMinInfoViewModelList(2);

        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();
        reviewerCreateBindingModel.setUserId(userMinInfoViewModelList.get(0).getId());

        ProjectReviewer testProjectReviewer = ReviewerUtils.createTestProjectReviewer();
        testProjectReviewer.setUserId(userMinInfoViewModelList.get(0).getId());

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.getReviewers().add(testProjectReviewer);

        when(projectReviewerRepositoryMock.save(any(ProjectReviewer.class)))
                .thenReturn(testProjectReviewer);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(userInfoServiceMock.getAllUsersFromOrg(anyString(), any()))
                .thenReturn(userMinInfoViewModelList);

        Assertions.assertThrows(GenericException.class,
                () -> projectReviewerService.createReviewer(reviewerCreateBindingModel)
        );
    }

    @Test
    void createReviewer_whenReviewerCreateBindingModelIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NullPointerException.class,
                () -> projectReviewerService.createReviewer(null)
        );
    }

    @Test
    void createReviewer_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.createReviewer(reviewerCreateBindingModel)
        );
    }

    @Test
    void createReviewer_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.createReviewer(reviewerCreateBindingModel)
        );
    }

    @Test
    void createReviewer_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.createReviewer(reviewerCreateBindingModel)
        );
    }

    @Test
    void createReviewer_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils.createTestProjectReviewerCreateBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.createReviewer(reviewerCreateBindingModel)
        );
    }

    @Test
    void deleteReviewerFromProject_whenReviewerIdIsValid_deleteReviewerFromProjectAndDb() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectReviewer testProjectReviewer = ReviewerUtils.createTestProjectReviewer();
        testProjectReviewer.getProject().setOwner("eforce21");

        //When
        when(projectReviewerRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProjectReviewer));

        projectReviewerService.deleteReviewerFromProject(REVIEWER_ID);

        //Then
        ArgumentCaptor<ProjectReviewer> projectReviewer = ArgumentCaptor.forClass(ProjectReviewer.class);
        verify(projectReviewerRepositoryMock, times(1)).delete(projectReviewer.capture());
        assertThat(projectReviewer.getValue()).isEqualTo(testProjectReviewer);

        verify(projectReviewerRepositoryMock).delete(any(ProjectReviewer.class));
    }

    @Test
    void deleteReviewerFromProject_whenReviewerIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.deleteReviewerFromProject("invalid_reviewer_id")
        );
    }

    @Test
    void deleteReviewerFromProject_whenReviewerIdIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.deleteReviewerFromProject(null)
        );
    }

    @Test
    void deleteReviewerFromProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.deleteReviewerFromProject(REVIEWER_ID)
        );
    }

    @Test
    void deleteReviewerFromProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectReviewer testProjectReviewer = ReviewerUtils.createTestProjectReviewer();
        testProjectReviewer.getProject().setOwner("test_org");

        //When
        when(projectReviewerRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProjectReviewer));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.deleteReviewerFromProject(REVIEWER_ID)
        );
    }

    @Test
    void deleteReviewerFromProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ProjectReviewer testProjectReviewer = ReviewerUtils.createTestProjectReviewer();
        testProjectReviewer.getProject().setOwner("datagym");

        //When
        when(projectReviewerRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProjectReviewer));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.deleteReviewerFromProject(REVIEWER_ID)
        );
    }

    @Test
    void deleteReviewerFromProject_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.deleteReviewerFromProject(REVIEWER_ID)
        );
    }

    @Test
    void getAllReviewerForProject_When2Reviewer_2Reviewer() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        List<ProjectReviewer> testProjectReviewers = ReviewerUtils.createTestProjectReviewers(2);

        List<UserMinInfoViewModel> userMinInfoViewModelList = UserInfoUtils.createUserMinInfoViewModelList(2);

        testProject.getReviewers().addAll(testProjectReviewers);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(userInfoServiceMock.getAllUsersFromOrg(anyString(), any()))
                .thenReturn(userMinInfoViewModelList);

        List<ProjectReviewerViewModel> allReviewerForProject = projectReviewerService.getAllReviewerForProject(PROJECT_ID);

        //Then
        ProjectReviewer expected = testProjectReviewers.get(0);
        ProjectReviewerViewModel actual = allReviewerForProject.get(0);

        assertEquals(2, allReviewerForProject.size());
        assertEquals(expected.getUserId(), actual.getUserInfo().getId());
        assertEquals(expected.getProject().getId(), actual.getProjectId());
        assertEquals(expected.getId(), actual.getReviewerId());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
    }

    @Test
    void getAllReviewerForProject_WhenZeroReviewer_emptyCollection() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        List<ProjectReviewerViewModel> allReviewerForProject = projectReviewerService.getAllReviewerForProject(PROJECT_ID);

        //Then
        assertEquals(0, allReviewerForProject.size());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
    }

    @Test
    void getAllReviewerForProject_whenProjectIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.getAllReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllReviewerForProject_whenProjectIdIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.getAllReviewerForProject(null)
        );
    }

    @Test
    void getAllReviewerForProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllReviewerForProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");
        List<ProjectReviewer> testProjectReviewers = ReviewerUtils.createTestProjectReviewers(2);

        testProject.getReviewers().addAll(testProjectReviewers);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllReviewerForProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");
        List<ProjectReviewer> testProjectReviewers = ReviewerUtils.createTestProjectReviewers(2);

        testProject.getReviewers().addAll(testProjectReviewers);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllReviewerForProject_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_when5PossibleReviewer_5UserIds() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<UserMinInfoViewModel> userMinInfoViewModelList = UserInfoUtils.createUserMinInfoViewModelList(2);
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(userInfoServiceMock.getAllUsersFromOrg(anyString(), any()))
                .thenReturn(userMinInfoViewModelList);

        List<UserMinInfoViewModel> possibleReviewerForProject = projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID);

        UserMinInfoViewModel actual = possibleReviewerForProject.get(0);
        UserMinInfoViewModel expected = userMinInfoViewModelList.get(0);

        //Then
        assertEquals(2, possibleReviewerForProject.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
    }

    @Test
    void getAllPossibleReviewerForProject_whenZeroPossibleReviewer_emptyCollection() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        List<UserMinInfoViewModel> allPossibleReviewerForProject = projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID);

        //Then
        assertEquals(0, allPossibleReviewerForProject.size());

        verify(projectRepositoryMock).findById(anyString());
        verify(projectRepositoryMock, times(1)).findById(anyString());
    }

    @Test
    void getAllPossibleReviewerForProject_whenProjectIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_whenProjectIdIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NotFoundException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(null)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID)
        );
    }

    @Test
    void getAllPossibleReviewerForProject_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> projectReviewerService.getAllPossibleReviewerForProject(PROJECT_ID)
        );
    }
}