package ai.datagym.application.superAdmin.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.DatasetUtils;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import ai.datagym.application.user.service.UserInformationService;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class SuperAdminServiceImplTest {
    private SuperAdminService superAdminService;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private DatasetRepository datasetRepositoryMock;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private UserInformationService userInformationServiceMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @BeforeEach
    void setUp() {
        superAdminService = new SuperAdminServiceImpl(projectRepositoryMock, datasetRepositoryMock, labelTaskRepositoryMock, userInformationServiceMock, mediaRepositoryMock);
    }

    @Test
    void getAllProjectFromDb_When2Projects_2Projects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Project> testProjects = ProjectUtils.createTestProjects(2);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalse()).thenReturn(testProjects);

        List<ProjectViewModel> allProjects = superAdminService.getAllProjectFromDb();

        //Then
        Project expected = testProjects.get(0);
        ProjectViewModel actual = allProjects.get(0);

        assertEquals(2, allProjects.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.isPinned(), actual.isPinned());

        verify(projectRepositoryMock).findAllByDeletedIsFalse();
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllProjectFromDb_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(projectRepositoryMock.findAllByDeletedIsFalse()).thenReturn(new ArrayList<>());

        List<ProjectViewModel> allProjects = superAdminService.getAllProjectFromDb();

        //Then
        assertTrue(allProjects.isEmpty());

        verify(projectRepositoryMock).findAllByDeletedIsFalse();
        verify(projectRepositoryMock, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllProjectFromDb_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> superAdminService.getAllProjectFromDb()
        );
    }

    @Test
    void getAllProjectFromDb_whenUserDoesNotHaveSuperAdminScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> superAdminService.getAllProjectFromDb()
        );
    }

    @Test
    void getAllDatasetsFromDb_When2Datasets_2Datasets() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Dataset> testDatasets = DatasetUtils.createTestListDatasets(2);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalse()).thenReturn(testDatasets);

        List<DatasetAllViewModel> datasetAllViewModels = superAdminService.getAllDatasetsFromDb();

        //Then
        Dataset expected = testDatasets.get(0);
        DatasetAllViewModel actual = datasetAllViewModels.get(0);

        assertEquals(2, datasetAllViewModels.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.isDeleted(), actual.isDeleted());
        assertNull(expected.getDeleteTime());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(0, actual.getProjectCount());

        verify(datasetRepositoryMock).findAllByDeletedIsFalse();
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllDatasetsFromDb_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createSuperAdminUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalse()).thenReturn(new ArrayList<>());
        List<DatasetAllViewModel> datasetAllViewModels = superAdminService.getAllDatasetsFromDb();

        //Then
        assertTrue(datasetAllViewModels.isEmpty());

        verify(datasetRepositoryMock).findAllByDeletedIsFalse();
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllDatasetsFromDb_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> superAdminService.getAllDatasetsFromDb()
        );
    }

    @Test
    void getAllDatasetsFromDb_whenUserDoesNotHaveSuperAdminScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> superAdminService.getAllProjectFromDb()
        );
    }
}