package ai.datagym.application.datasetAwsS3.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.awsS3.AwsS3UserCredentialsService;
import ai.datagym.application.dataset.service.awsS3.AwsS3UserCredentialsServiceImpl;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.testUtils.AwsS3CredentialsUtils;
import ai.datagym.application.testUtils.DatasetUtils;
import ai.datagym.application.testUtils.LimitsUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class DatasetAwsS3UserCredentialsServiceImplTest {
    private AwsS3UserCredentialsService awsS3UserCredentialsService;

    @Mock
    private AwsS3UserCredentialsRepository awsS3UserCredentialsRepositoryMock;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private DatasetRepository datasetRepositoryMock;

    @BeforeEach
    void setUp() {
        awsS3UserCredentialsService = new AwsS3UserCredentialsServiceImpl(
                awsS3UserCredentialsRepositoryMock,
                limitServiceMock,
                datasetRepositoryMock);
    }

    @Test
    void getAwsS3Credentials_whenDatasetIdIsValidAndThereAreAnyCredentials_getAwsS3Credentials() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.findAwsS3UserCredentialsByDatasetId(anyString()))
                .thenReturn(java.util.Optional.of(testDatasetAwsS3UserCredentials));

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID);

        //Then
        assertNotNull(actualCredentials);
        assertEquals(testDatasetAwsS3UserCredentials.getId(), actualCredentials.getId());
        assertEquals(testDatasetAwsS3UserCredentials.getAccessKey(), actualCredentials.getAccessKey());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketName(), actualCredentials.getBucketName());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketRegion(), actualCredentials.getBucketRegion());
        assertEquals(testDatasetAwsS3UserCredentials.getDataset().getId(), actualCredentials.getDatasetId());
        assertEquals(testDatasetAwsS3UserCredentials.getLocationPath(), actualCredentials.getLocationPath());


        verify(awsS3UserCredentialsRepositoryMock).findAwsS3UserCredentialsByDatasetId(anyString());
        verify(awsS3UserCredentialsRepositoryMock, times(1)).findAwsS3UserCredentialsByDatasetId(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsRepositoryMock);
    }

    @Test
    void getAwsS3Credentials_whenDatasetIdIsValidAndThereAreNotAnyCredentialsForThisDataset_returnNewCredentialsObjectWithoutValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID);

        //Then
        assertNotNull(actualCredentials);
        assertNull(actualCredentials.getId());
        assertNull(actualCredentials.getAccessKey());
        assertNull(actualCredentials.getBucketName());
        assertNull(actualCredentials.getBucketRegion());
        assertNull(actualCredentials.getDatasetId());
        assertNull(actualCredentials.getLocationPath());


        verify(awsS3UserCredentialsRepositoryMock).findAwsS3UserCredentialsByDatasetId(anyString());
        verify(awsS3UserCredentialsRepositoryMock, times(1)).findAwsS3UserCredentialsByDatasetId(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsRepositoryMock);
    }

    @Test
    void getAwsS3Credentials_whenPricingPlanTypeIsNotTeamPro_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.findAwsS3UserCredentialsByDatasetId(anyString()))
                .thenReturn(java.util.Optional.of(testDatasetAwsS3UserCredentials));

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID)
        );
    }

    @Test
    void getAwsS3Credentials_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                     () -> awsS3UserCredentialsService.getAwsS3Credentials("invalid_dataset_id")
        );
    }

    @Test
    void getAwsS3Credentials_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID)
        );
    }

    @Test
    void getAwsS3Credentials_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID)
        );
    }

    @Test
    void getAwsS3Credentials_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.getAwsS3Credentials(DATASET_ID)
        );
    }

    @Test
    void updateAwsS3Credentials_whenDatasetIdIsValidAndThereAreAnyCredentials_updateAwsS3Credentials() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.findAwsS3UserCredentialsByDatasetId(anyString()))
                .thenReturn(java.util.Optional.of(testDatasetAwsS3UserCredentials));

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService.updateAwsS3Credentials(DATASET_ID,
                                                                                                        updateBindingModel);

        //Then
        assertNotNull(actualCredentials);
        assertEquals(testDatasetAwsS3UserCredentials.getId(), actualCredentials.getId());
        assertEquals(testDatasetAwsS3UserCredentials.getAccessKey(), actualCredentials.getAccessKey());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketName(), actualCredentials.getBucketName());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketRegion(), actualCredentials.getBucketRegion());
        assertEquals(testDatasetAwsS3UserCredentials.getDataset().getId(), actualCredentials.getDatasetId());
        assertEquals(testDatasetAwsS3UserCredentials.getLocationPath(), actualCredentials.getLocationPath());

        verify(awsS3UserCredentialsRepositoryMock).findAwsS3UserCredentialsByDatasetId(anyString());
        verify(awsS3UserCredentialsRepositoryMock, times(1)).findAwsS3UserCredentialsByDatasetId(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsRepositoryMock);
    }

    @Test
    void updateAwsS3Credentials_whenDatasetIdIsValidAndThereAreNotAnyCredentialsForThisDataset_createAwsS3Credentials() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.save(any(DatasetAwsS3UserCredentials.class)))
                .then(returnsFirstArg());

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService
                .updateAwsS3Credentials(DATASET_ID, updateBindingModel);

        //Then
        assertNotNull(actualCredentials);
        assertEquals(testDatasetAwsS3UserCredentials.getAccessKey(), actualCredentials.getAccessKey());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketName(), actualCredentials.getBucketName());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketRegion(), actualCredentials.getBucketRegion());
        assertEquals(testDatasetAwsS3UserCredentials.getDataset().getId(), actualCredentials.getDatasetId());
        assertEquals(testDatasetAwsS3UserCredentials.getLocationPath(), actualCredentials.getLocationPath());

        verify(awsS3UserCredentialsRepositoryMock).save(any(DatasetAwsS3UserCredentials.class));
        verify(awsS3UserCredentialsRepositoryMock, times(1)).save(any(DatasetAwsS3UserCredentials.class));
    }

    @Test
    void updateAwsS3Credentials_whenPricingPlanTypeIsNotTeamPro_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();

        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.save(any(DatasetAwsS3UserCredentials.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Credentials(DATASET_ID, updateBindingModel)
        );
    }

    @Test
    void updateAwsS3Credentials_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Credentials("invalid_dataset_id", updateBindingModel)
        );
    }

    @Test
    void updateAwsS3Credentials_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Credentials(DATASET_ID, updateBindingModel)
        );
    }

    @Test
    void updateAwsS3Credentials_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Credentials(DATASET_ID, updateBindingModel)
        );
    }

    @Test
    void updateAwsS3Credentials_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        AwsS3CredentialsUpdateBindingModel updateBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Credentials(DATASET_ID, updateBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenDatasetIdIsValidAndThereAreAnyCredentials_updateAwsS3Keys() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.findAwsS3UserCredentialsByDatasetId(anyString()))
                .thenReturn(java.util.Optional.of(testDatasetAwsS3UserCredentials));

        when(awsS3UserCredentialsRepositoryMock.save(any(DatasetAwsS3UserCredentials.class)))
                .then(returnsFirstArg());

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService
                .updateAwsS3Keys(DATASET_ID, updateKeysBindingModel);

        //Then
        assertNotNull(actualCredentials);
        assertEquals(testDatasetAwsS3UserCredentials.getId(), actualCredentials.getId());
        assertEquals(updateKeysBindingModel.getAccessKey(), actualCredentials.getAccessKey());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketName(), actualCredentials.getBucketName());
        assertEquals(testDatasetAwsS3UserCredentials.getBucketRegion(), actualCredentials.getBucketRegion());
        assertEquals(testDatasetAwsS3UserCredentials.getDataset().getId(), actualCredentials.getDatasetId());
        assertEquals(testDatasetAwsS3UserCredentials.getLocationPath(), actualCredentials.getLocationPath());

        verify(awsS3UserCredentialsRepositoryMock).save(any(DatasetAwsS3UserCredentials.class));
        verify(awsS3UserCredentialsRepositoryMock, times(1)).save(any(DatasetAwsS3UserCredentials.class));
    }

    @Test
    void updateAwsS3Keys_whenPricingPlanTypeIsNotTeamPro_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();

        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        // When
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenCredentialsAreNotFound_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }

    @Test
    void updateAwsS3Keys_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateKeysBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Keys(DATASET_ID, updateKeysBindingModel)
        );
    }


    @Test
    void updateAwsS3Bucket_whenDatasetIdIsValidAndThereAreAnyCredentials_updateAwsS3Bucket() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);
        when(awsS3UserCredentialsRepositoryMock.findAwsS3UserCredentialsByDatasetId(anyString()))
                .thenReturn(java.util.Optional.of(testDatasetAwsS3UserCredentials));

        when(awsS3UserCredentialsRepositoryMock.save(any(DatasetAwsS3UserCredentials.class)))
                .then(returnsFirstArg());

        AwsS3CredentialViewModel actualCredentials = awsS3UserCredentialsService
                .updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel);

        //Then
        assertNotNull(actualCredentials);
        assertEquals(testDatasetAwsS3UserCredentials.getId(), actualCredentials.getId());
        assertEquals(updateBucketBindingModel.getBucketName(), actualCredentials.getBucketName());
        assertEquals(updateBucketBindingModel.getBucketRegion(), actualCredentials.getBucketRegion());
        assertEquals(testDatasetAwsS3UserCredentials.getDataset().getId(), actualCredentials.getDatasetId());
        assertEquals(updateBucketBindingModel.getLocationPath(), actualCredentials.getLocationPath());

        verify(awsS3UserCredentialsRepositoryMock).save(any(DatasetAwsS3UserCredentials.class));
        verify(awsS3UserCredentialsRepositoryMock, times(1)).save(any(DatasetAwsS3UserCredentials.class));
    }

    @Test
    void updateAwsS3Bucket_whenPricingPlanTypeIsNotTeamPro_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();

        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        // When
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel)
        );
    }

    @Test
    void updateAwsS3Bucket_whenCredentialsAreNotFound_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();
        tesLimitPricingPlanViewModel.setPricingPlanType("TEAM_PRO");

        DatasetAwsS3UserCredentials testDatasetAwsS3UserCredentials = AwsS3CredentialsUtils.createTestAwsS3Credentials();

        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));
        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        //Then
        assertThrows(GenericException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel)
        );
    }

    @Test
    void updateAwsS3Bucket_whenDatasetIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //Then
        assertThrows(NotFoundException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket("invalid_dataset_id", updateBucketBindingModel)
        );
    }

    @Test
    void updateAwsS3Bucket_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel)
        );
    }

    @Test
    void updateAwsS3Bucket_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel)
        );
    }

    @Test
    void updateAwsS3Bucket_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_owner");

        AwsS3CredentialsUpdateBucketBindingModel updateBucketBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();

        //When
        when(datasetRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testDataset));

        //Then
        assertThrows(ForbiddenException.class,
                     () -> awsS3UserCredentialsService.updateAwsS3Bucket(DATASET_ID, updateBucketBindingModel)
        );
    }

    @Test
    void syncDatasetWithAws() {
    }
}