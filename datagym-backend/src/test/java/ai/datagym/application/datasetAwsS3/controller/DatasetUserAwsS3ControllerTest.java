package ai.datagym.application.datasetAwsS3.controller;

import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;
import ai.datagym.application.dataset.service.awsS3.AwsS3UserCredentialsService;
import ai.datagym.application.dataset.service.awsS3.DatasetAwsS3UserSyncService;
import ai.datagym.application.media.entity.MediaUploadStatus;
import ai.datagym.application.media.models.viewModels.AwsS3ImageUploadViewModel;
import ai.datagym.application.testUtils.AwsS3CredentialsUtils;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.List;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class DatasetUserAwsS3ControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AwsS3UserCredentialsService awsS3UserCredentialsServiceMock;

    @MockBean
    private DatasetAwsS3UserSyncService datasetAwsS3UserSyncServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesAwsS3Controller() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("datasetUserAwsS3Controller"));
    }

    @Test
    void getAwsS3CredentialsByDatasetId_whenDatasetIdIsValid_getAwsS3CredentialsByDatasetId() throws Exception {
        AwsS3CredentialViewModel testAwsS3CredentialViewModel = AwsS3CredentialsUtils.createTestAwsS3CredentialViewModel();

        when(awsS3UserCredentialsServiceMock.getAwsS3Credentials(anyString()))
                .thenReturn(testAwsS3CredentialViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/{datasetId}/aws", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testAwsS3CredentialViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testAwsS3CredentialViewModel.getName()))
                .andExpect(jsonPath("$.locationPath").value(testAwsS3CredentialViewModel.getLocationPath()))
                .andExpect(jsonPath("$.bucketName").value(testAwsS3CredentialViewModel.getBucketName()))
                .andExpect(jsonPath("$.bucketRegion").value(testAwsS3CredentialViewModel.getBucketRegion()))
                .andExpect(jsonPath("$.lastError").value(testAwsS3CredentialViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3CredentialViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3CredentialViewModel.getLastSynchronized()))
                .andExpect(jsonPath("$.datasetId").value(testAwsS3CredentialViewModel.getDatasetId()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testAwsS3CredentialViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(awsS3UserCredentialsServiceMock, times(1)).getAwsS3Credentials(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

    @Test
    void createOrUpdateAwsS3Credentials_whenDatasetIdIsValid_createOrUpdateAwsS3Credentials() throws Exception {
        AwsS3CredentialsUpdateBindingModel testAwsS3CredentialsUpdateBindingModel = AwsS3CredentialsUtils.createTestAwsS3CredentialsUpdateBindingModel();
        AwsS3CredentialViewModel testAwsS3CredentialViewModel = AwsS3CredentialsUtils.createTestAwsS3CredentialViewModel();

        String content = objectMapper.writeValueAsString(testAwsS3CredentialsUpdateBindingModel);

        when(awsS3UserCredentialsServiceMock.updateAwsS3Credentials(anyString(),
                                                                    any(AwsS3CredentialsUpdateBindingModel.class)))
                .thenReturn(testAwsS3CredentialViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/{datasetId}/aws", DATASET_ID)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testAwsS3CredentialViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testAwsS3CredentialViewModel.getName()))
                .andExpect(jsonPath("$.locationPath").value(testAwsS3CredentialViewModel.getLocationPath()))
                .andExpect(jsonPath("$.bucketName").value(testAwsS3CredentialViewModel.getBucketName()))
                .andExpect(jsonPath("$.bucketRegion").value(testAwsS3CredentialViewModel.getBucketRegion()))
                .andExpect(jsonPath("$.lastError").value(testAwsS3CredentialViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3CredentialViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3CredentialViewModel.getLastSynchronized()))
                .andExpect(jsonPath("$.datasetId").value(testAwsS3CredentialViewModel.getDatasetId()))
                .andReturn();

        verify(awsS3UserCredentialsServiceMock, times(1))
                .updateAwsS3Credentials(anyString(), any(AwsS3CredentialsUpdateBindingModel.class));
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

    @Test
    void syncDatasetWithAws_whenDatasetIdIsValidAndZeroNewImages_syncDatasetWithAws() throws Exception {
        AwsS3SyncViewModel testAwsS3SyncViewModel = AwsS3CredentialsUtils.createTestAwsS3CredentialSyncViewModel();

        when(datasetAwsS3UserSyncServiceMock.syncDatasetWithAws(anyString()))
                .thenReturn(testAwsS3SyncViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/{datasetId}/aws/sync", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.addedS3Images", hasSize(0)))
                .andExpect(jsonPath("$.deletedS3Images", hasSize(0)))
                .andExpect(jsonPath("$.uploadFailedS3Images", hasSize(0)))
                .andExpect(jsonPath("$.lastError").value(testAwsS3SyncViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3SyncViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3SyncViewModel.getLastSynchronized()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testAwsS3SyncViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetAwsS3UserSyncServiceMock, times(1)).syncDatasetWithAws(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

    @Test
    void syncDatasetWithAws_whenDatasetIdIsValidAndTwoAddedImagesAndTwoDeleted_syncDatasetWithAws() throws Exception {
        AwsS3SyncViewModel testAwsS3SyncViewModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialSyncViewModel();

        List<AwsS3ImageUploadViewModel> testAwsS3CredentialSyncViewModels = AwsS3CredentialsUtils
                .createTestAwsS3CredentialSyncViewModels(4);

        AwsS3ImageUploadViewModel exceptedAddedUploadViewModel = testAwsS3CredentialSyncViewModels.get(0);

        testAwsS3SyncViewModel
                .getAddedS3Images()
                .addAll(Arrays.asList(exceptedAddedUploadViewModel,
                        testAwsS3CredentialSyncViewModels.get(1)));

        AwsS3ImageUploadViewModel exceptedDeletedUploadViewModel = testAwsS3CredentialSyncViewModels.get(2);
        exceptedDeletedUploadViewModel.setMediaUploadStatus(MediaUploadStatus.DELETED.name());

        testAwsS3SyncViewModel
                .getDeletedS3Images()
                .addAll(Arrays.asList(exceptedDeletedUploadViewModel,
                        testAwsS3CredentialSyncViewModels.get(3)));

        when(datasetAwsS3UserSyncServiceMock.syncDatasetWithAws(anyString()))
                .thenReturn(testAwsS3SyncViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/{datasetId}/aws/sync", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.addedS3Images", hasSize(2)))
                .andExpect(jsonPath("$.addedS3Images[0].awsKey").value(exceptedAddedUploadViewModel.getAwsKey()))
                .andExpect(jsonPath("$.addedS3Images[0].mediaUploadStatus").value(exceptedAddedUploadViewModel.getMediaUploadStatus()))
                .andExpect(jsonPath("$.deletedS3Images", hasSize(2)))
                .andExpect(jsonPath("$.deletedS3Images[0].awsKey").value(exceptedDeletedUploadViewModel.getAwsKey()))
                .andExpect(jsonPath("$.deletedS3Images[0].mediaUploadStatus").value(exceptedDeletedUploadViewModel.getMediaUploadStatus()))
                .andExpect(jsonPath("$.uploadFailedS3Images", hasSize(0)))
                .andExpect(jsonPath("$.lastError").value(testAwsS3SyncViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3SyncViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3SyncViewModel.getLastSynchronized()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testAwsS3SyncViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetAwsS3UserSyncServiceMock, times(1)).syncDatasetWithAws(anyString());
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

    @Test
    void updateAwsS3Keys_whenDatasetIdIsValid_updateAwsS3Keys() throws Exception {
        AwsS3CredentialsUpdateKeysBindingModel updateKeysBindingModel = AwsS3CredentialsUtils.createTestAwsS3CredentialsUpdateKeysBindingModel();
        AwsS3CredentialViewModel testAwsS3CredentialViewModel = AwsS3CredentialsUtils.createTestAwsS3CredentialViewModel();

        String content = objectMapper.writeValueAsString(updateKeysBindingModel);

        when(awsS3UserCredentialsServiceMock.updateAwsS3Keys(anyString(),
                                                             any(AwsS3CredentialsUpdateKeysBindingModel.class)))
                .thenReturn(testAwsS3CredentialViewModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/dataset/{datasetId}/aws/keys", DATASET_ID)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testAwsS3CredentialViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testAwsS3CredentialViewModel.getName()))
                .andExpect(jsonPath("$.locationPath").value(testAwsS3CredentialViewModel.getLocationPath()))
                .andExpect(jsonPath("$.bucketName").value(testAwsS3CredentialViewModel.getBucketName()))
                .andExpect(jsonPath("$.bucketRegion").value(testAwsS3CredentialViewModel.getBucketRegion()))
                .andExpect(jsonPath("$.lastError").value(testAwsS3CredentialViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3CredentialViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3CredentialViewModel.getLastSynchronized()))
                .andExpect(jsonPath("$.datasetId").value(testAwsS3CredentialViewModel.getDatasetId()))
                .andExpect(jsonPath("$.accessKey").value(updateKeysBindingModel.getAccessKey()))
                .andReturn();

        verify(awsS3UserCredentialsServiceMock, times(1))
                .updateAwsS3Keys(anyString(), any(AwsS3CredentialsUpdateKeysBindingModel.class));
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

    @Test
    void updateAwsS3Bucket_whenDatasetIdIsValid_updateAwsS3Bucket() throws Exception {
        AwsS3CredentialsUpdateBucketBindingModel updateKeysBindingModel = AwsS3CredentialsUtils
                .createTestAwsS3CredentialsUpdateBucketBindingModel();
        AwsS3CredentialViewModel testAwsS3CredentialViewModel = AwsS3CredentialsUtils.createTestAwsS3CredentialViewModel();

        String content = objectMapper.writeValueAsString(updateKeysBindingModel);

        when(awsS3UserCredentialsServiceMock.updateAwsS3Bucket(anyString(),
                                                               any(AwsS3CredentialsUpdateBucketBindingModel.class)))
                .thenReturn(testAwsS3CredentialViewModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/dataset/{datasetId}/aws/bucket", DATASET_ID)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testAwsS3CredentialViewModel.getId()))
                .andExpect(jsonPath("$.name").value(updateKeysBindingModel.getName()))
                .andExpect(jsonPath("$.locationPath").value(updateKeysBindingModel.getLocationPath()))
                .andExpect(jsonPath("$.bucketName").value(updateKeysBindingModel.getBucketName()))
                .andExpect(jsonPath("$.bucketRegion").value(updateKeysBindingModel.getBucketRegion()))
                .andExpect(jsonPath("$.lastError").value(testAwsS3CredentialViewModel.getLastError()))
                .andExpect(jsonPath("$.lastErrorTimeStamp").value(testAwsS3CredentialViewModel.getLastErrorTimeStamp()))
                .andExpect(jsonPath("$.lastSynchronized").value(testAwsS3CredentialViewModel.getLastSynchronized()))
                .andExpect(jsonPath("$.datasetId").value(testAwsS3CredentialViewModel.getDatasetId()))
                .andReturn();

        verify(awsS3UserCredentialsServiceMock, times(1))
                .updateAwsS3Bucket(anyString(), any(AwsS3CredentialsUpdateBucketBindingModel.class));
        verifyNoMoreInteractions(awsS3UserCredentialsServiceMock);
    }

}