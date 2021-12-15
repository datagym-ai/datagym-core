package ai.datagym.application.dataset.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.dataset.service.dataset.DatasetServiceImpl;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.LocalImage;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.entity.UrlImage;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.media.repo.MediaCustomRepository;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.DatasetUtils.DATASET_NAME;
import static ai.datagym.application.testUtils.ImageUtils.TEST_URL_IMAGE_URL;
import static ai.datagym.application.testUtils.ImageUtils.UNSUPPORTED_FORMAT_URL_2;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class DatasetServiceImplTest {
    @Value(value = "${datagym.deactivate-limiter}")
    private boolean deactivateLimiter;

    @Mock
    private DatasetRepository datasetRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private BinFileService binFileServiceMock;

    @Mock
    MediaCustomRepository mediaCustomRepositoryMock;
    @Mock
    private MediaRepository mediaRepositoryMock;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    private LabelTaskService labelTaskServiceMock;

    @Mock
    private Tika tikaMock;

    private DatasetService datasetService;

    @BeforeEach
    void setUp() {
        datasetService = spy(new DatasetServiceImpl(
                datasetRepositoryMock,
                binFileServiceMock,
                mediaRepositoryMock,
                mediaCustomRepositoryMock,
                labelTaskServiceMock,
                tikaMock,
                limitServiceMock,
                labelConfigurationServiceMock,
                deactivateLimiter,
                projectRepositoryMock)
        );
    }

    @Test
    void createDataset_whenInputIsValid_createDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        DatasetCreateBindingModel datasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTestLimitPricingPlanProViewModel();

        // When
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        DatasetViewModel createdDataset = datasetService.createDataset(datasetCreateBindingModel, false);


        // Then
        assertEquals(datasetCreateBindingModel.getName(), createdDataset.getName());
        assertEquals(datasetCreateBindingModel.getShortDescription(), createdDataset.getShortDescription());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verifyNoMoreInteractions(datasetRepositoryMock);
    }

    @Test
    void createDataset_whenInputIsNull_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(NullPointerException.class,
                () -> datasetService.createDataset(null, false)
        );
    }

    @Test
    void createDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        DatasetCreateBindingModel datasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();

        Assertions.assertThrows(ForbiddenException.class,
                () -> datasetService.createDataset(datasetCreateBindingModel, false)
        );
    }

    @Test
    void createDataset_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);


        // Given
        DatasetCreateBindingModel datasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        datasetCreateBindingModel.setOwner("test_owner");

        Assertions.assertThrows(ForbiddenException.class,
                () -> datasetService.createDataset(datasetCreateBindingModel, false)
        );
    }

    @Test
    void createDataset_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        DatasetCreateBindingModel datasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        datasetCreateBindingModel.setOwner("datagym");

        Assertions.assertThrows(ForbiddenException.class,
                () -> datasetService.createDataset(datasetCreateBindingModel, false)
        );
    }

    @Test
    void getDataset_whenIdIsValid_getDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        DatasetViewModel dataset = datasetService.getDataset(DATASET_ID, false);

        //Then
        assertNotNull(dataset);
        assertEquals(testDataset.getId(), dataset.getId());
        assertEquals(testDataset.getName(), dataset.getName());
        assertEquals(testDataset.getShortDescription(), dataset.getShortDescription());
        assertEquals(testDataset.isDeleted(), dataset.isDeleted());
        assertNull(dataset.getDeleteTime());
        assertEquals(0, dataset.getProjectCount());

        verify(datasetRepositoryMock).findById(anyString());
        verify(datasetRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(datasetRepositoryMock);
    }

    @Test
    void getDataset_whenIdIsNotValid_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(NotFoundException.class,
                () -> datasetService.getDataset("invalid_dataset_id", false)
        );
    }

    @Test
    void getDataset_whenIdIsValidAndIsDeletedIsTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(NotFoundException.class,
                () -> datasetService.getDataset("invalid_dataset_id", false)
        );
    }

    @Test
    void getDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDataset(DATASET_ID, false)
        );
    }

    @Test
    void getDataset_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDataset(DATASET_ID, false)
        );
    }

    @Test
    void getDataset_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);
        testDataset.setOwner("test_owner");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDataset(DATASET_ID, false)
        );
    }

    @Test
    void getDatasetWithProjects_whenIdIsValidAndZeroProjects_getDatasetWithZeroProjects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        DatasetProjectViewModel datasetWithProjects = datasetService.getDatasetWithProjects(DATASET_ID);

        //Then
        assertEquals(0, datasetWithProjects.getProjects().size());
        assertEquals(0, testDataset.getMedia().size());

        assertNotNull(datasetWithProjects);
        assertEquals(testDataset.getId(), datasetWithProjects.getId());
        assertEquals(testDataset.getName(), datasetWithProjects.getName());
        assertEquals(testDataset.getShortDescription(), datasetWithProjects.getShortDescription());
        assertEquals(testDataset.isDeleted(), datasetWithProjects.isDeleted());
        assertNull(datasetWithProjects.getDeleteTime());

        verify(datasetRepositoryMock).findById(anyString());
        verify(datasetRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(datasetRepositoryMock);
    }

    @Test
    void getDatasetWithProjects_whenIdIsValidAndTwoProjects_getDatasetWithTwoProjects() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        List<Project> testProjects = ProjectUtils.createTestProjects(2);
        HashSet<Project> projects = new HashSet<>(testProjects);
        testDataset.setProjects(projects);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        DatasetProjectViewModel datasetWithProjects = datasetService.getDatasetWithProjects(DATASET_ID);

        //Then
        assertEquals(2, datasetWithProjects.getProjects().size());
        assertEquals(0, testDataset.getMedia().size());

        assertNotNull(datasetWithProjects);
        assertEquals(testDataset.getId(), datasetWithProjects.getId());
        assertEquals(testDataset.getName(), datasetWithProjects.getName());
        assertEquals(testDataset.getShortDescription(), datasetWithProjects.getShortDescription());
        assertEquals(testDataset.isDeleted(), datasetWithProjects.isDeleted());
        assertNull(datasetWithProjects.getDeleteTime());

        verify(datasetRepositoryMock).findById(anyString());
        verify(datasetRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(datasetRepositoryMock);
    }

    @Test
    void getDatasetWithProjects_whenIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> datasetService.getDatasetWithProjects("invalid_dataset_id")
        );
    }

    @Test
    void getDatasetWithProjects_whenIdIsValidAndIsDeletedIsTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(NotFoundException.class,
                () -> datasetService.getDatasetWithProjects("invalid_dataset_id")
        );
    }

    @Test
    void getDatasetWithProjects_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDatasetWithProjects(DATASET_ID)
        );
    }

    @Test
    void getDatasetWithProjects_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDatasetWithProjects(DATASET_ID)
        );
    }

    @Test
    void getDatasetWithProjects_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);
        testDataset.setOwner("test_owner");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        assertThrows(ForbiddenException.class,
                () -> datasetService.getDatasetWithProjects(DATASET_ID)
        );
    }

    @Test
    void getAllDatasets_When2Datasets_2Datasets() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Dataset> testDatasets = DatasetUtils.createTestListDatasets(2);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString())).thenReturn(testDatasets);

        List<DatasetAllViewModel> datasetAllViewModels = datasetService.getAllDatasets("eforce21");

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

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllDatasets_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner("eforce21")).thenReturn(new ArrayList<>());
        List<DatasetAllViewModel> datasetAllViewModels = datasetService.getAllDatasets("eforce21");

        //Then
        assertTrue(datasetAllViewModels.isEmpty());

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllDatasets_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllDatasets(anyString())
        );
    }

    @Test
    void updateDataset_whenInputDatasetIdValid_updateDataset() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("DatasetName updated");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        DatasetViewModel actual = datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel);

        //Then
        assertNotNull(actual);
        assertEquals(testDatasetUpdateBindingModel.getName(), actual.getName());
        assertEquals(testDatasetUpdateBindingModel.getShortDescription(), actual.getShortDescription());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verify(datasetRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void updateDataset_whenDatasetNameIsDummyDatasetOne_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("Dummy_Dataset_One");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenDatasetNameIsDummyDatasetTwo_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("Dummy_Dataset_Two");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenInputDatasetIdNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.updateDataset("invalid_dataset_id", testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenInputDatasetNameAlreadyExists_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("DatasetName updated");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        when(datasetRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(AlreadyExistsException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("DatasetName updated");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("DatasetName updated");
        testDataset.setShortDescription("Dataset shortDescription updated");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void updateDataset_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("DatasetName updated");
        testDataset.setShortDescription("Dataset shortDescription updated");
        testDataset.setOwner("test_owner");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.updateDataset(DATASET_ID, testDatasetUpdateBindingModel)
        );
    }

    @Test
    void deleteDatasetById_whenDatasetIdIsValidAndDeleteDatasetTrue_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        DatasetViewModel actual = datasetService.deleteDatasetById(DATASET_ID, true);

        //Then
        assertNotNull(actual);

        assertTrue(actual.isDeleted());
        assertEquals(testDataset.isDeleted(), actual.isDeleted());
        assertNotNull(testDataset.getDeleteTime());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verify(datasetRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteDatasetById_whenDatasetIdIsValidAndDeleteDatasetFalse_setDeletedToFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        DatasetViewModel actual = datasetService.deleteDatasetById(DATASET_ID, false);

        //Then
        assertNotNull(actual);

        assertFalse(actual.isDeleted());
        assertEquals(testDataset.isDeleted(), actual.isDeleted());
        assertNull(testDataset.getDeleteTime());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verify(datasetRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteDatasetById_whenDatasetIdIsValidAndDeleteDatasetFalseAndDatasetNameIsNotUnique_setDeletedToFalseAndAddUUIDToName() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        String testDatasetName = testDataset.getName();
        testDataset.setDeleted(true);

        List<Dataset> testListDatasets = DatasetUtils.createTestListDatasets(2);

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());
        when(datasetRepositoryMock.findAllByName(anyString())).thenReturn(testListDatasets);

        DatasetViewModel actual = datasetService.deleteDatasetById(DATASET_ID, false);

        //Then
        assertNotNull(actual);

        assertFalse(actual.isDeleted());
        assertEquals(testDataset.isDeleted(), actual.isDeleted());
        assertNull(testDataset.getDeleteTime());
        assertEquals(testDatasetName.length() + 37, actual.getName().length());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verify(datasetRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteDatasetById_whenDatasetIdIsValidAndDeleteDatasetFalseAndDatasetNameIsNotUniqueAndNameIsMoreThan89Chars_setDeletedToFalseAndCutTheNameTo89CharsAddUUIDToName() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setName("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua");
        testDataset.setDeleted(true);

        List<Dataset> testListDatasets = DatasetUtils.createTestListDatasets(2);

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.saveAndFlush(any(Dataset.class))).then(returnsFirstArg());

        when(datasetRepositoryMock.findAllByName(anyString())).thenReturn(testListDatasets);

        DatasetViewModel actual = datasetService.deleteDatasetById(DATASET_ID, false);

        //Then
        assertNotNull(actual);

        assertFalse(actual.isDeleted());
        assertEquals(testDataset.isDeleted(), actual.isDeleted());
        assertNull(testDataset.getDeleteTime());
        assertEquals(126, actual.getName().length());

        verify(datasetRepositoryMock).saveAndFlush(any());
        verify(datasetRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteDatasetById_whenDatasetIdIsNotValidAndDeleteDatasetTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.deleteDatasetById("invalid_dataset_id", true)
        );
    }

    @Test
    void deleteProjectById_whenDatasetIdIsNotValidAndDeleteDatasetFalse_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.deleteDatasetById("invalid_dataset_id", false)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.deleteDatasetById(DATASET_ID, false)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);
        testDataset.setOwner("datagym");

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.deleteDatasetById(DATASET_ID, false)
        );
    }

    @Test
    void deleteProjectById_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setDeleted(true);
        testDataset.setOwner("test_org");

        //when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.deleteDatasetById(DATASET_ID, false)
        );
    }

    @Test
    void permanentDeleteDatasetFromDB_whenDatasetIdIsValid_permanentDeleteDatasetFromDB() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        datasetService.permanentDeleteDatasetFromDB(DATASET_ID);

        //Then
        verify(datasetRepositoryMock).delete(any(Dataset.class));
        verify(datasetRepositoryMock, times(1)).delete(any(Dataset.class));
    }

    @Test
    void permanentDeleteDatasetFromDB_whenDatasetObjectIsValid_permanentDeleteDatasetFromDB() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Set<Dataset> testListDatasets = DatasetUtils.createTestSetsDatasets(2);
        List<Media> testMedia = ImageUtils.createTestListImages(2);

        testMedia.get(0).getDatasets().add(testDataset);
        testMedia.get(1).setDatasets(testListDatasets);

        testMedia.forEach(image -> image.getDatasets().add(testDataset));

        testDataset.setMedia(new HashSet<>(testMedia));

        //When
        datasetService.permanentDeleteDatasetFromDB(testDataset, false);

        //Then

        verify(datasetRepositoryMock).delete(any(Dataset.class));
        verify(datasetRepositoryMock, times(1)).delete(any(Dataset.class));
    }

    @Test
    void permanentDeleteDatasetFromDB_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.permanentDeleteDatasetFromDB("invalid_dataset_id")
        );
    }

    @Test
    void permanentDeleteDatasetFromDB_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.permanentDeleteDatasetFromDB(DATASET_ID
                )
        );
    }

    @Test
    void permanentDeleteDatasetFromDB_whenUserIsNotRoot_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.permanentDeleteDatasetFromDB(DATASET_ID)
        );
    }

    @Test
    void permanentDeleteDatasetFromDB__whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_org");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.permanentDeleteDatasetFromDB(DATASET_ID)
        );
    }

    @Test
    void isDatasetNameUnique_whenDatasetNameIsUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findByName(anyString())).thenReturn(java.util.Optional.of(testDataset));

        boolean actual = datasetService.isDatasetNameUnique(DATASET_NAME);

        //Then
        Assertions.assertFalse(actual);
        verify(datasetRepositoryMock).findByName(anyString());
        verify(datasetRepositoryMock, times(1)).findByName(anyString());
    }

    @Test
    void isDatasetNameUnique_whenDatasetNameIsNotUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findByName(anyString())).thenReturn(java.util.Optional.empty());

        boolean actual = datasetService.isDatasetNameUnique(DATASET_NAME);

        //Then
        Assertions.assertTrue(actual);
        verify(datasetRepositoryMock).findByName(anyString());
        verify(datasetRepositoryMock, times(1)).findByName(anyString());
    }

    @Test
    void isDatasetNameUniqueAndDeletedFalse_whenDatasetNameIsUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.of(testDataset));

        boolean actual = datasetService.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString());

        //Then
        Assertions.assertFalse(actual);
        verify(datasetRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(datasetRepositoryMock, times(1)).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
    }

    @Test
    void isDatasetNameUniqueAndDeletedFalse_whenDatasetNameIsNotUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString())).thenReturn(java.util.Optional.empty());

        boolean actual = datasetService.isDatasetNameUniqueAndDeletedFalse(DATASET_NAME, "eforce");

        //Then
        Assertions.assertTrue(actual);
        verify(datasetRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(datasetRepositoryMock, times(1)).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
    }

    @Test
    void createImageFile_whenInputIsValid_createImageFile() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        testProject.getDatasets().add(testDataset);
        testDataset.getProjects().add(testProject);

        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();
        BufferedImage fakeBufferedImage = new BufferedImage(500, 500, 1);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");


        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        when(binFileServiceMock.create(anyString(), any(), any()))
                .thenReturn(testBinFileEntity);

        when(mediaRepositoryMock.saveAndFlush(any(Media.class)))
                .then(returnsFirstArg());

        doReturn(fakeBufferedImage)
                .when(datasetService).getBufferedImageFromEntity(any());

        when(labelTaskServiceMock.createLabelTask(anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(labelTask);

        MediaViewModel actual = datasetService.createImageFile(testDataset.getId(), testBinFileEntity.getName(), null);

        // Then
        assertNotNull(actual);
        assertEquals(testBinFileEntity.getName(), actual.getMediaName());

        verify(mediaRepositoryMock).saveAndFlush(any());
        verify(mediaRepositoryMock, times(1)).saveAndFlush(any());
    }

    @Test
    void createImageFile_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.createImageFile("invalid_project_id", "Binfileentity_Name", null)
        );
    }

    @Test
    void createImageFile_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(null);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImageFile(testDataset.getId(), testBinFileEntity.getName(), null)
        );
    }

    @Test
    void createImageFile_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("datagym");

        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImageFile(testDataset.getId(), testBinFileEntity.getName(), null)
        );
    }

    @Test
    void createImageFile_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_org");

        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImageFile(testDataset.getId(), testBinFileEntity.getName(), null)
        );
    }

    @Test
    void getAllImages_When2Images_2Images() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        List<LocalImage> testListLocalImages = ImageUtils.createTestListLocalImages(2);
        List<BinFileEntity> testBinFileEntityList = BinfileEntityUtils.createTestBinFileEntityList(2);
        testListLocalImages.get(0).setBinFileEntity(testBinFileEntityList.get(0));
        testListLocalImages.get(1).setBinFileEntity(testBinFileEntityList.get(1));

        List<Media> testListMedia = new ArrayList<>(testListLocalImages);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        when(datasetRepositoryMock.getAllMediasByDatasetId(DATASET_ID)).thenReturn(testListMedia);

        List<MediaViewModel> allImages = datasetService.getAllMedia(DATASET_ID);

        //Then
        Media expected = testListMedia.get(0);
        MediaViewModel actual = allImages.get(0);

        assertEquals(2, allImages.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getMediaName(), actual.getMediaName());
        assertEquals(expected.getMediaSourceType().name(), actual.getMediaSourceType());

        verify(datasetRepositoryMock).getAllMediasByDatasetId(anyString());
        verify(datasetRepositoryMock, times(1)).getAllMediasByDatasetId(anyString());
    }

    @Test
    void getAllImages_WhenZeroImages_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(datasetRepositoryMock.getAllMediasByDatasetId(DATASET_ID)).thenReturn(new ArrayList<>());
        List<MediaViewModel> allImages = datasetService.getAllMedia(DATASET_ID);

        //Then
        assertTrue(allImages.isEmpty());

        verify(datasetRepositoryMock).getAllMediasByDatasetId(anyString());
        verify(datasetRepositoryMock, times(1)).getAllMediasByDatasetId(anyString());
    }

    @Test
    void getAllImages_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllMedia(DATASET_ID)
        );
    }

    @Test
    void getAllImages_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllMedia(DATASET_ID)
        );
    }

    @Test
    void getAllImages_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllMedia(DATASET_ID)
        );
    }

    @Test
    void getAllDatasetsWithoutOrg__When2Datasets_2Datasets() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Dataset> testDatasets = DatasetUtils.createTestListDatasets(2);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner("datagym")).thenReturn(testDatasets);

        List<DatasetAllViewModel> allDatasets = datasetService.getAllDatasetsWithoutOrg();

        //Then
        Dataset expected = testDatasets.get(0);
        DatasetAllViewModel actual = allDatasets.get(0);

        assertEquals(2, allDatasets.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.isDeleted(), actual.isDeleted());
        assertNull(expected.getDeleteTime());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(0, actual.getProjectCount());

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("datagym");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("datagym");
    }

    @Test
    void getAllDatasetsWithoutOrg_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner("eforce21")).thenReturn(new ArrayList<>());
        List<DatasetAllViewModel> allDatasets = datasetService.getAllDatasetsWithoutOrg();

        //Then
        assertTrue(allDatasets.isEmpty());

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllDatasetsWithoutOrg_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllDatasetsWithoutOrg()
        );
    }

    @Test
    void getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin__When2Datasets_2Datasets() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<Dataset> testDatasets = DatasetUtils.createTestListDatasets(2);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner("eforce21")).thenReturn(testDatasets);

        List<DatasetViewModel> allDatasets = datasetService.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();

        //Then
        Dataset expected = testDatasets.get(0);
        DatasetViewModel actual = allDatasets.get(0);

        assertEquals(2, allDatasets.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.isDeleted(), actual.isDeleted());
        assertNull(expected.getDeleteTime());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(0, actual.getMedia().size());
        assertEquals(0, actual.getProjectCount());

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin_WhenNoProjects_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(datasetRepositoryMock.findAllByDeletedIsFalseAndOwner("datagym")).thenReturn(new ArrayList<>());
        List<DatasetViewModel> allDatasets = datasetService.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();

        //Then
        assertTrue(allDatasets.isEmpty());

        verify(datasetRepositoryMock).findAllByDeletedIsFalseAndOwner("eforce21");
        verify(datasetRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner("eforce21");
    }

    @Test
    void getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin()
        );
    }

    @Test
    void createImageUrl_whenInputIsValid_returnListWithOneSuccessElement() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDatasets(new HashSet<>());

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setProjects(new HashSet<>());

        testDataset.getProjects().add(testProject);
        testProject.getDatasets().add(testDataset);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        Set<String> testImageUrlSet = new HashSet<>(Arrays.asList(TEST_URL_IMAGE_URL));

        // when
        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(tikaMock.detect(any(URL.class))).thenReturn("image/png");

        when(labelTaskServiceMock.createLabelTask(anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(labelTask);

        when(mediaRepositoryMock.save(any(Media.class)))
                .then(returnsFirstArg());

        List<UrlImageUploadViewModel> urlImageUploadViewModels = datasetService.createImagesByShareableLink(testDataset.getId(), testImageUrlSet, false);

        UrlImageUploadViewModel actual = urlImageUploadViewModels.get(0);
        String imageUrl = testImageUrlSet.stream().findAny().get();

        // Then
        assertEquals(1, urlImageUploadViewModels.size());
        assertEquals(imageUrl, actual.getImageUrl());
        assertEquals("SUCCESS", actual.getMediaUploadStatus());

        verify(mediaRepositoryMock).save(any());
        verify(mediaRepositoryMock, times(1)).save(any());
    }

    @Test
    void createImageUrl_whenUrlIsInvalid_returnListWithOneFailedElement() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDatasets(new HashSet<>());

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setProjects(new HashSet<>());

        testDataset.getProjects().add(testProject);
        testProject.getDatasets().add(testDataset);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        Set<String> testImageUrlSet = new HashSet<>(Arrays.asList("Invalid_URl"));

        // when
        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(tikaMock.detect(any(URL.class))).thenReturn("image/png");

        when(labelTaskServiceMock.createLabelTask(anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(labelTask);

        when(mediaRepositoryMock.save(any(Media.class)))
                .then(returnsFirstArg());

        List<UrlImageUploadViewModel> urlImageUploadViewModels = datasetService.createImagesByShareableLink(testDataset.getId(), testImageUrlSet, false);

        UrlImageUploadViewModel actual = urlImageUploadViewModels.get(0);
        String imageUrl = testImageUrlSet.stream().findAny().get();

        // Then
        assertEquals(1, urlImageUploadViewModels.size());
        assertEquals(imageUrl, actual.getImageUrl());
        assertEquals("FAILED", actual.getMediaUploadStatus());
    }

    @Test
    void createImageUrl_whenMimeTypeUnsupported_returnListWithOneMimeTypeUnsupportedElement() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDatasets(new HashSet<>());

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setProjects(new HashSet<>());

        testDataset.getProjects().add(testProject);
        testProject.getDatasets().add(testDataset);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        Set<String> testImageUrlSet = new HashSet<>(Arrays.asList(UNSUPPORTED_FORMAT_URL_2));

        // when
        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(tikaMock.detect(any(URL.class))).thenReturn("unsupported_mime_type");

        when(labelTaskServiceMock.createLabelTask(anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(labelTask);

        when(mediaRepositoryMock.save(any(Media.class)))
                .then(returnsFirstArg());

        List<UrlImageUploadViewModel> urlImageUploadViewModels = datasetService.createImagesByShareableLink(testDataset.getId(), testImageUrlSet, false);

        UrlImageUploadViewModel actual = urlImageUploadViewModels.get(0);
        String imageUrl = testImageUrlSet.stream().findAny().get();

        // Then
        assertEquals(1, urlImageUploadViewModels.size());
        assertEquals(imageUrl, actual.getImageUrl());
        assertEquals("UNSUPPORTED_MIME_TYPE", actual.getMediaUploadStatus());
    }

    @Test
    void createImageUrl_whenImageUrlAlreadyExists_returnListWithDuplicateElement() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setDatasets(new HashSet<>());

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setProjects(new HashSet<>());

        testDataset.getProjects().add(testProject);
        testProject.getDatasets().add(testDataset);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        Set<String> testImageUrlSet = new HashSet<>(Arrays.asList(TEST_URL_IMAGE_URL));

        UrlImage testUrlImage = ImageUtils.createTestUrlImage();
        UrlImage testUrlImage2 = ImageUtils.createTestUrlImage();

        testUrlImage.setUrl(TEST_URL_IMAGE_URL);
        testUrlImage2.setUrl(TEST_URL_IMAGE_URL);

        List<Media> testListMedia = new ArrayList<>();
        testListMedia.add(testUrlImage);


        // when
        when(datasetRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testDataset));

        when(tikaMock.detect(any(URL.class))).thenReturn("unsupported_mime_type");

        when(labelTaskServiceMock.createLabelTask(anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(labelTask);

        when(mediaRepositoryMock.save(any(Media.class)))
                .then(returnsFirstArg());

        when(mediaRepositoryMock.findAllByDatasetsIdAndMediaSourceType(anyString(), any(MediaSourceType.class)))
                .thenReturn(testListMedia);


        List<UrlImageUploadViewModel> urlImageUploadViewModels = datasetService.createImagesByShareableLink(testDataset.getId(), testImageUrlSet, false);

        UrlImageUploadViewModel actual = urlImageUploadViewModels.get(0);
        String imageUrl = testImageUrlSet.stream().findAny().get();

        // Then
        assertEquals(1, urlImageUploadViewModels.size());
        assertEquals(imageUrl, actual.getImageUrl());
        assertEquals("DUPLICATE", actual.getMediaUploadStatus());
    }

    @Test
    void createImageUrl_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> datasetService.createImagesByShareableLink(PROJECT_ID, new HashSet<>(), false)
        );
    }

    @Test
    void createImageUrl_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(null);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImagesByShareableLink(PROJECT_ID, new HashSet<>(), false)
        );
    }

    @Test
    void createImageUrl_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("datagym");

        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImagesByShareableLink(PROJECT_ID, new HashSet<>(), false)
        );
    }

    @Test
    void createImageUrl_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_org");

        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        // when
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        // Then
        assertThrows(ForbiddenException.class,
                () -> datasetService.createImagesByShareableLink(PROJECT_ID, new HashSet<>(), false)
        );
    }
}