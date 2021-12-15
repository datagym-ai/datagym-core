package ai.datagym.application.externalAPI.service;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.export.service.ExportSegmentationService;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueChangeValueClassBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValidation;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.service.MediaService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.project.service.ProjectService;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ImageUtils.TEST_URL_IMAGE_URL;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;
import static ai.datagym.application.testUtils.LcEntryValueUtils.LC_ENTRY_VALUE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class ExternalApiServiceImplTest {
    private ExternalApiService externalApiService;

    @Mock
    private ProjectService projectServiceMock;

    @Mock
    private DatasetService datasetServiceMock;

    @Mock
    private LcEntryValueService lcEntryValueServiceMock;

    @Mock
    private MediaService mediaServiceMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @Mock
    private LcEntryRepository lcEntryRepositoryMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    LabelConfigurationRepository labelConfigurationRepositoryMock;

    @Mock
    LabelTaskService labelTaskServiceMock;

    @Mock
    AiSegService aiSegServiceMock;

    @Mock
    ExportSegmentationService exportSegmentationService;

    @Mock
    private LcEntryValidation lcEntryValidationMock;


    @BeforeEach
    void setUp() {
        externalApiService = new ExternalApiServiceImpl(
                projectServiceMock,
                datasetServiceMock,
                lcEntryValueServiceMock,
                mediaServiceMock,
                mediaRepositoryMock,
                projectRepositoryMock,
                lcEntryRepositoryMock,
                labelTaskRepositoryMock,
                lcEntryValueRepositoryMock,
                objectMapper,
                modelMapper,
                labelConfigurationServiceMock,
                labelConfigurationRepositoryMock,
                labelTaskServiceMock,
                Optional.of(aiSegServiceMock),
                exportSegmentationService, lcEntryValidationMock, null);
    }

    @Test
    void getAllProjects_When2Projects_2Projects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        List<ProjectViewModel> testProjectViewModels = ProjectUtils.createTestProjectViewModels(2);
        List<ExternalApiProjectViewModel> testExternalApiProjectViewModels = ExternalApiTokenUtils
                .createTestExternalApiProjectViewModels(1);

        //When
        when(projectServiceMock.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin())
                .thenReturn(testProjectViewModels);

        when(modelMapper.map(testProjectViewModels.get(0), ExternalApiProjectViewModel.class))
                .thenReturn(testExternalApiProjectViewModels.get(0));

        List<ExternalApiProjectViewModel> allProjects = externalApiService.getAllProjects();

        //Then
        ProjectViewModel expected = testProjectViewModels.get(0);
        ExternalApiProjectViewModel actual = testExternalApiProjectViewModels.get(0);

        assertEquals(2, allProjects.size());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());

        verify(projectServiceMock).getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verify(projectServiceMock, times(1)).getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjects_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //When
        when(projectServiceMock.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin())
                .thenReturn(new ArrayList<>());

        List<ExternalApiProjectViewModel> allProjects = externalApiService.getAllProjects();

        //Then
        assertTrue(allProjects.isEmpty());

        verify(projectServiceMock).getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verify(projectServiceMock, times(1)).getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjects_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getAllProjects()
        );
    }

    @Test
    void getAllProjects_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getAllProjects()
        );
    }

    @Test
    void getAllDatasets_When2Datasets_2Datasets() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);

        List<ExternalApiDatasetViewModel> testExternalApiDatasetViewModel = ExternalApiTokenUtils
                .createTestExternalApiDatasetViewModels(1);

        //When
        when(datasetServiceMock.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin())
                .thenReturn(testListDatasetViewModel);

        when(modelMapper.map(testListDatasetViewModel.get(0), ExternalApiDatasetViewModel.class))
                .thenReturn(testExternalApiDatasetViewModel.get(0));

        List<ExternalApiDatasetViewModel> allDatasets = externalApiService.getAllDatasets();

        //Then
        DatasetViewModel expected = testListDatasetViewModel.get(0);
        ExternalApiDatasetViewModel actual = allDatasets.get(0);

        assertEquals(2, allDatasets.size());
        assertEquals(0, actual.getMedia().size());
        assertEquals(1, actual.getProjectCount());

        verify(datasetServiceMock).getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
        verify(datasetServiceMock, times(1))
                .getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
    }

    @Test
    void getAllDatasets_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //When
        when(datasetServiceMock.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin())
                .thenReturn(new ArrayList<>());

        List<ExternalApiDatasetViewModel> allDatasets = externalApiService.getAllDatasets();

        //Then
        assertTrue(allDatasets.isEmpty());

        verify(datasetServiceMock).getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
        verify(datasetServiceMock, times(1))
                .getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
    }

    @Test
    void getAllDatasets_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getAllDatasets()
        );
    }

    @Test
    void getAllDatasets_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getAllDatasets()
        );
    }

    @Test
    void createDataset_whenInputIsValid_createDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils.createTestExternalApiDatasetViewModel();
        testExternalApiDatasetViewModel.setName("DatasetName");
        testExternalApiDatasetViewModel.setShortDescription("Dataset shortDescription");

        // When
        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(testDatasetViewModel);

        when(modelMapper.map(testDatasetViewModel, ExternalApiDatasetViewModel.class))
                .thenReturn(testExternalApiDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        ExternalApiDatasetViewModel createdDataset = externalApiService.createDataset(testExternalApiCreateDatasetBindingModel, false);

        // Then
        assertEquals(testExternalApiCreateDatasetBindingModel.getName(), createdDataset.getName());
        assertEquals(testExternalApiCreateDatasetBindingModel.getShortDescription(), createdDataset.getShortDescription());

        verify(datasetServiceMock).createDataset(any(DatasetCreateBindingModel.class), anyBoolean());
        verify(datasetServiceMock, times(1))
                .createDataset(any(DatasetCreateBindingModel.class), anyBoolean());
    }

    @Test
    void createDataset_whenDatasetNameAlreadyExists_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils.createTestExternalApiDatasetViewModel();
        testExternalApiDatasetViewModel.setName("DatasetName");
        testExternalApiDatasetViewModel.setShortDescription("Dataset shortDescription");

        // When
        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(testDatasetViewModel);

        when(modelMapper.map(testDatasetViewModel, ExternalApiDatasetViewModel.class))
                .thenReturn(testExternalApiDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(false);

        Assertions.assertThrows(AlreadyExistsException.class,
                () -> externalApiService.createDataset(testExternalApiCreateDatasetBindingModel, false)
        );
    }

    @Test
    void createDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.createDataset(testExternalApiCreateDatasetBindingModel, false)
        );
    }

    @Test
    void createDataset_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.createDataset(testExternalApiCreateDatasetBindingModel, false)
        );
    }

    @Test
    void createImageUrl_whenInputIsValid_returnListWithOneSuccessElement() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        List<UrlImageUploadViewModel> tesUrlImageUploadViewModels = ImageUtils.createTesImageUploadViewModels(1);
        tesUrlImageUploadViewModels.get(0).setImageUrl(TEST_URL_IMAGE_URL);

        Set<String> testImageUrlSet = new HashSet<>(Arrays.asList(TEST_URL_IMAGE_URL));

        // when
        when(datasetServiceMock.createImagesByShareableLink(anyString(), anySet(), anyBoolean()))
                .thenReturn(tesUrlImageUploadViewModels);

        List<UrlImageUploadViewModel> urlImageUploadViewModels = externalApiService.createImageUrl(DATASET_ID, testImageUrlSet, false);

        UrlImageUploadViewModel actual = urlImageUploadViewModels.get(0);
        String imageUrl = testImageUrlSet.stream().findAny().get();

        // Then
        assertEquals(1, urlImageUploadViewModels.size());
        assertEquals(imageUrl, actual.getImageUrl());
        assertEquals("SUCCESS", actual.getMediaUploadStatus());

        verify(datasetServiceMock).createImagesByShareableLink(anyString(), anySet(), anyBoolean());
        verify(datasetServiceMock, times(1)).createImagesByShareableLink(anyString(), anySet(), anyBoolean());
    }

    @Test
    void createImageUrl_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.createImageUrl(DATASET_ID, new HashSet<>(), false)
        );
    }

    @Test
    void createImageUrl_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.createImageUrl(DATASET_ID, new HashSet<>(), false)
        );
    }

    @Test
    void deleteImageFile_whenImageIdIsValidAndDeleteImageTrue_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        externalApiService.deleteMediaFile(IMAGE_ID, true);

        //Then
        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> deleteCapture = ArgumentCaptor.forClass(Boolean.class);

        verify(mediaServiceMock).deleteMediaFile(idCapture.capture(), deleteCapture.capture());
        verify(mediaServiceMock, times(1)).deleteMediaFile(anyString(), anyBoolean());

        assertThat(idCapture.getValue()).isEqualTo(IMAGE_ID);
        assertThat(deleteCapture.getValue()).isEqualTo(true);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void deleteImageFile_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.deleteMediaFile(IMAGE_ID, true)
        );
    }

    @Test
    void deleteImageFile_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.deleteMediaFile(IMAGE_ID, true)
        );
    }

    @Test
    void addDataset_whenProjectDoesNotContainThisDataset_addDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //When
        externalApiService.addDataset(PROJECT_ID, DATASET_ID);

        //Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);

        verify(projectServiceMock).addDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        verify(projectServiceMock, times(1)).addDataset(anyString(), anyString());

        assertThat(projectIdCapture.getValue()).isEqualTo(PROJECT_ID);
        assertThat(datasetIdCapture.getValue()).isEqualTo(DATASET_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void addDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void addDataset_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.addDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void removeDataset_whenProjectDoesNotContainThisDataset_addDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //When
        externalApiService.removeDataset(PROJECT_ID, DATASET_ID);

        //Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);

        verify(projectServiceMock).removeDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        verify(projectServiceMock, times(1)).removeDataset(anyString(), anyString());

        assertThat(projectIdCapture.getValue()).isEqualTo(PROJECT_ID);
        assertThat(datasetIdCapture.getValue()).isEqualTo(DATASET_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void removeDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.removeDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void removeDataset_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.removeDataset(PROJECT_ID, DATASET_ID)
        );
    }

    @Test
    void streamImageFile_whenInputIsValidAndLocalImage_streamImageFile() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        //When
        externalApiService.streamMediaFile(IMAGE_ID, httpServletResponseMock, true);

        //Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> downloadFileCapture = ArgumentCaptor.forClass(Boolean.class);

        verify(mediaServiceMock).streamMediaFile(projectIdCapture.capture(), any(HttpServletResponse.class), downloadFileCapture.capture());
        verify(mediaServiceMock, times(1))
                .streamMediaFile(anyString(), any(HttpServletResponse.class), anyBoolean());

        assertThat(projectIdCapture.getValue()).isEqualTo(IMAGE_ID);
        assertThat(downloadFileCapture.getValue()).isEqualTo(true);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void streamImageFile_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.streamMediaFile(IMAGE_ID, httpServletResponseMock, true)
        );
    }

    @Test
    void streamImageFile_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.streamMediaFile(IMAGE_ID, httpServletResponseMock, true)
        );
    }

    @Test
    void exportProjectLabels_whenInputIsValidAndLocalImage_streamImageFile() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        //When
        externalApiService.exportProjectLabels(PROJECT_ID, httpServletResponseMock);

        //Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);

        verify(projectServiceMock).exportProjectLabels(projectIdCapture.capture(), any(HttpServletResponse.class));
        verify(projectServiceMock, times(1))
                .exportProjectLabels(anyString(), any(HttpServletResponse.class));

        assertThat(projectIdCapture.getValue()).isEqualTo(PROJECT_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void exportProjectLabels_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.exportProjectLabels(PROJECT_ID, httpServletResponseMock)
        );
    }

    @Test
    void exportProjectLabels_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.exportProjectLabels(PROJECT_ID, httpServletResponseMock)
        );
    }

    @Test
    void getDataset_whenIdIsValid_getDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        DatasetViewModel testListDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils
                .createTestExternalApiDatasetViewModel();

        //When
        when(datasetServiceMock.getDataset(anyString(), anyBoolean()))
                .thenReturn(testListDatasetViewModel);

        when(modelMapper.map(testListDatasetViewModel, ExternalApiDatasetViewModel.class))
                .thenReturn(testExternalApiDatasetViewModel);

        ExternalApiDatasetViewModel actual = externalApiService.getDataset(DATASET_ID);

        //Then
        assertEquals(0, actual.getMedia().size());
        assertEquals(1, actual.getProjectCount());
        assertEquals(testListDatasetViewModel.getName(), actual.getName());
        assertEquals(testListDatasetViewModel.getOwner(), actual.getOwner());
        assertEquals(testListDatasetViewModel.getShortDescription(), actual.getShortDescription());

        verify(datasetServiceMock).getDataset(anyString(), anyBoolean());
        verify(datasetServiceMock, times(1)).getDataset(anyString(), anyBoolean());
    }

    @Test
    void getDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getDataset(DATASET_ID)
        );
    }

    @Test
    void getDataset_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getDataset(DATASET_ID)
        );
    }

    @Test
    void clearConfig_whenIdIsValid_clearConfig() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        LcConfigDeleteViewModel testLcConfigDeleteViewModel = LabelConfigurationUtils.createTestLcConfigDeleteViewModel();

        //When
        when(labelConfigurationServiceMock.clearConfig(anyString())).thenReturn(testLcConfigDeleteViewModel);

        LcConfigDeleteViewModel lcConfigDeleteViewModel = externalApiService.clearConfig(LC_CONFIG_ID);

        //Then
        assertNotNull(lcConfigDeleteViewModel);
        assertEquals(LC_CONFIG_ID, lcConfigDeleteViewModel.getConfigId());

        verify(labelConfigurationServiceMock).clearConfig(anyString());
        verify(labelConfigurationServiceMock, times(1)).clearConfig(anyString());
    }

    @Test
    void clearConfig_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.clearConfig(LC_CONFIG_ID)
        );
    }

    @Test
    void clearConfig_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.clearConfig(LC_CONFIG_ID)
        );
    }

    @Test
    void uploadLabelConfiguration_whenInputIsValid_uploadLabelConfiguration() throws NoSuchMethodException, NoSuchAlgorithmException, InstantiationException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();

        //When
        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        LabelConfigurationViewModel labelConfigurationViewModel = externalApiService
                .uploadLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels);

        //Then
        assertNotNull(labelConfigurationViewModel);
        assertEquals(testLabelConfiguration.getId(), labelConfigurationViewModel.getId());
        assertEquals(testLabelConfiguration.getProject().getId(), labelConfigurationViewModel.getProjectId());
        assertEquals(0, labelConfigurationViewModel.getEntries().size());

        verify(labelConfigurationServiceMock, times(1))
                .updateLabelConfiguration(anyString(), anyList(), anyBoolean());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void uploadLabelConfiguration_whenConfigIdIsNotValid_throwException() throws NoSuchMethodException, NoSuchAlgorithmException, InstantiationException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();

        //When
        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);


        assertThrows(NotFoundException.class,
                () -> externalApiService.uploadLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels)
        );
    }

    @Test
    void uploadLabelConfiguration_whenConfigEntriesAreNotEmptyCollection_throwException() throws NoSuchMethodException, NoSuchAlgorithmException, InstantiationException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();

        List<LcEntry> testLcEntryLineList = LcEntryUtils.createTestLcEntryGeometryList(3, LcEntryType.LINE, testLabelConfiguration);

        testLabelConfiguration.getEntries().addAll(testLcEntryLineList);
        //When
        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);


        assertThrows(GenericException.class,
                () -> externalApiService.uploadLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels)
        );
    }

    @Test
    void uploadLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // When
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.uploadLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels)
        );
    }

    @Test
    void uploadLabelConfiguration_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.uploadLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels)
        );
    }

    @Test
    void getProjectTasks_When2Datasets_2Tasks() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given

        List<LabelTaskViewModel> labelTaskViewModelList = LabelTaskUtils.createTestLabelTaskViewModelList(2, "datagym");

        //When
        when(projectServiceMock.getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt()))
                .thenReturn(labelTaskViewModelList);

        List<LabelTaskViewModel> taskList = externalApiService
                .getProjectTasks(PROJECT_ID, "testTask", LabelTaskState.WAITING, 20);

        //Then
        LabelTaskViewModel expected = labelTaskViewModelList.get(0);
        LabelTaskViewModel actual = taskList.get(0);

        assertEquals(2, taskList.size());
        assertEquals(expected.getMediaId(), actual.getMediaId());
        assertEquals(expected.getLabelTaskState(), actual.getLabelTaskState());
        assertEquals(expected.getProjectId(), actual.getProjectId());

        verify(projectServiceMock).getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
        verify(projectServiceMock, times(1))
                .getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
    }

    @Test
    void getProjectTasks_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //When
        when(projectServiceMock.getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt()))
                .thenReturn(new ArrayList<>());

        List<LabelTaskViewModel> taskList = externalApiService
                .getProjectTasks(PROJECT_ID, "testTask", LabelTaskState.WAITING, 20);

        //Then
        assertTrue(taskList.isEmpty());

        verify(projectServiceMock).getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
        verify(projectServiceMock, times(1))
                .getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
    }

    @Test
    void getProjectTasks_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getProjectTasks(PROJECT_ID, "testTask", LabelTaskState.WAITING, 20)
        );
    }

    @Test
    void getProjectTasks_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getProjectTasks(PROJECT_ID, "testTask", LabelTaskState.WAITING, 20)
        );
    }

    @Test
    void getTask_whenTaskIdIsValid_getTask() throws NoSuchMethodException, JsonProcessingException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given

        LabelModeDataViewModel testLabelModeDataViewModel = LabelTaskUtils.createTestLabelModeDataViewModel();

        //When
        when(labelTaskServiceMock.getTask(anyString()))
                .thenReturn(testLabelModeDataViewModel);

        LabelModeDataViewModel actualTask = externalApiService.getTask(LABEL_TASK_ID);

        //Then
        assertEquals(testLabelModeDataViewModel.getTaskId(), actualTask.getTaskId());
        assertEquals(testLabelModeDataViewModel.getLabelTaskState(), actualTask.getLabelTaskState());
        assertEquals(testLabelModeDataViewModel.getProjectName(), actualTask.getProjectName());
        assertEquals(testLabelModeDataViewModel.getReviewComment(), actualTask.getReviewComment());

        verify(labelTaskServiceMock).getTask(anyString());
        verify(labelTaskServiceMock, times(1)).getTask(anyString());
    }

    @Test
    void getTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getTask(LABEL_TASK_ID)
        );
    }

    @Test
    void getTask_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.getTask(LABEL_TASK_ID)
        );
    }

    @Test
    void skipTask_whenTaskIdIsValid_skipTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        doNothing()
                .when(labelTaskServiceMock)
                .skipTask(anyString());

        externalApiService.skipTask(LABEL_TASK_ID);

        // Then
        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock).skipTask(anyString());
        verify(labelTaskServiceMock, times(1)).skipTask(idCapture.capture());

        assertEquals(LABEL_TASK_ID, idCapture.getValue());

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void skipTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void skipTask_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.skipTask(LABEL_TASK_ID)
        );
    }

    @Test
    void completeTask_whenTaskIdIsValid_skipTask() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();
        LabelTaskCompleteViewModel testLabelTaskCompleteViewModel = LabelTaskUtils.createTestLabelTaskCompleteViewModel();

        when(labelTaskServiceMock.completeTask(anyString(), any(LabelTaskCompleteBindingModel.class)))
                .thenReturn(testLabelTaskCompleteViewModel);

        LabelTaskCompleteViewModel actual = externalApiService
                .completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel);

        // Then
        assertEquals(LABEL_TASK_ID, actual.getCurrentTaskId());
        assertFalse(actual.isHasLabelConfigChanged());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock, times(1))
                .completeTask(idCapture.capture(), any(LabelTaskCompleteBindingModel.class));

        org.assertj.core.api.Assertions.assertThat(idCapture.getValue()).isEqualTo(LABEL_TASK_ID);

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void completeTask_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // When
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void completeTask_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // When
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                () -> externalApiService.completeTask(LABEL_TASK_ID, testLabelTaskCompleteBindingModel)
        );
    }

    @Test
    void createLcEntryValueTree_whenInputIsValid_createLcEntryValueTree() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        LcEntryValueViewModel lcEntryValueTree = externalApiService
                .createLcEntryValueTree(LC_ENTRY_VALUE_ID, lcEntryValueCreateBindingModel);

        // Then
        assertEquals(lcEntryValueCreateBindingModel.getMediaId(), lcEntryValueTree.getMediaId());

        verify(lcEntryValueServiceMock, times(1))
                .createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void createLcEntryValueTree_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);


        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .createLcEntryValueTree(LC_ENTRY_VALUE_ID, lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValueTree_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);


        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .createLcEntryValueTree(LC_ENTRY_VALUE_ID, lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void updateSingleLcEntryValue_whenInputIsValid_updateSingleLcEntryValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(2);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        LcEntryValueViewModel lcEntryValueViewModel1 = externalApiService
                .updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, lcEntryValueUpdateBindingModel);

        // Then
        assertEquals(lcEntryValueViewModel1.getLabeler(), lcEntryValueViewModel.getLabeler());
        assertEquals(lcEntryValueViewModel1.getMediaId(), lcEntryValueViewModel.getMediaId());
        assertEquals(lcEntryValueViewModel1.getLcEntryId(), lcEntryValueViewModel.getLcEntryId());
        assertEquals(lcEntryValueViewModel1.getConfigurationId(), lcEntryValueViewModel.getConfigurationId());

        verify(lcEntryValueServiceMock, times(1))
                .updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void updateSingleLcEntryValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils
                .createTestLcEntryValueUpdateBindingModels(2);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, lcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void updateSingleLcEntryValue_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils
                .createTestLcEntryValueUpdateBindingModels(2);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);


        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, lcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void deleteLcValue_whenImageIdIsValidAndDeleteImageTrue_deleteLcValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        externalApiService.deleteLcValue(LC_ENTRY_VALUE_ID);

        //Then
        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);

        verify(lcEntryValueServiceMock).deleteLcValue(idCapture.capture());
        verify(lcEntryValueServiceMock, times(1)).deleteLcValue(anyString());

        assertEquals(LC_ENTRY_VALUE_ID, idCapture.getValue());

        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void deleteLcValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.deleteLcValue(LC_ENTRY_VALUE_ID)
        );
    }

    @Test
    void deleteLcValue_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.deleteLcValue(LC_ENTRY_VALUE_ID)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenInputIsValid_updateSingleLcEntryValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        LcEntryValueViewModel actual = externalApiService
                .changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel);

        // Then
        assertEquals(actual.getLabeler(), lcEntryValueViewModel.getLabeler());
        assertEquals(actual.getMediaId(), lcEntryValueViewModel.getMediaId());
        assertEquals(actual.getLcEntryId(), lcEntryValueViewModel.getLcEntryId());
        assertEquals(actual.getConfigurationId(), lcEntryValueViewModel.getConfigurationId());

        verify(lcEntryValueServiceMock, times(1))
                .changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void changeTypeOfSingleLabelValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils
                .createTestLcEntryValueChangeValueClassBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils
                .createTestLcEntryValueChangeValueClassBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService
                        .changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    @Test
    void prepare_whenImageIdIsValidAndDeleteImageTrue_prepare() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        externalApiService.prepare(IMAGE_ID);

        //Then
        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);

        verify(aiSegServiceMock).prepare(idCapture.capture(), isNull(), isNull());
        verify(aiSegServiceMock, times(1)).prepare(anyString(), isNull(), isNull());

        assertEquals(IMAGE_ID, idCapture.getValue());

        verifyNoMoreInteractions(aiSegServiceMock);
    }

    @Test
    void prepare_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.prepare(IMAGE_ID)
        );
    }

    @Test
    void prepare_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.prepare(IMAGE_ID)
        );
    }

    @Test
    void calculate_whenInputIsValid_calculate() {
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // Given
        AiSegCalculate testAiSegCalculate = AisegUtils.createTestAiSegCalculate();
        AiSegResponse testAiSegResponse = AisegUtils.createTestAiSegResponse();

        when(aiSegServiceMock.calculate(any(AiSegCalculate.class)))
                .thenReturn(testAiSegResponse);

        AiSegResponse actual = externalApiService.calculate(testAiSegCalculate);

        // Then
        assertEquals(actual.getImageId(), testAiSegResponse.getImageId());
        assertEquals(0, actual.getResult().size());

        verify(aiSegServiceMock, times(1)).calculate(any(AiSegCalculate.class));
        verifyNoMoreInteractions(aiSegServiceMock);
    }

    @Test
    void calculate_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        AiSegCalculate testAiSegCalculate = AisegUtils.createTestAiSegCalculate();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.calculate(testAiSegCalculate)
        );
    }

    @Test
    void calculate_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        AiSegCalculate testAiSegCalculate = AisegUtils.createTestAiSegCalculate();

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.calculate(testAiSegCalculate)
        );
    }

    @Test
    void finish_whenImageIdIsValidAndDeleteImageTrue_finish() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        // When
        externalApiService.finish(IMAGE_ID);

        //Then
        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);

        verify(aiSegServiceMock).finish(idCapture.capture());
        verify(aiSegServiceMock, times(1)).finish(anyString());

        assertEquals(IMAGE_ID, idCapture.getValue());

        verifyNoMoreInteractions(aiSegServiceMock);
    }

    @Test
    void finish_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.finish(IMAGE_ID)
        );
    }

    @Test
    void finish_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(ForbiddenException.class,
                () -> externalApiService.finish(IMAGE_ID)
        );
    }
}