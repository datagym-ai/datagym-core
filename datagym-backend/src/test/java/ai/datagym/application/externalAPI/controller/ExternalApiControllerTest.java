package ai.datagym.application.externalAPI.controller;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.externalAPI.service.ExternalApiService;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueChangeValueClassBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;
import static ai.datagym.application.testUtils.LcEntryValueUtils.LC_ENTRY_VALUE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
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
class ExternalApiControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ExternalApiService externalApiServiceMock;

    @MockBean
    private DatasetService datasetServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesUserTaskController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("externalApiController"));
    }

    @Test
    void getAllProjects_when2Projects_2Projects() throws Exception {
        List<ExternalApiProjectViewModel> testExternalApiProjectViewModels = ExternalApiTokenUtils
                .createTestExternalApiProjectViewModels(2);

        when(externalApiServiceMock.getAllProjects()).thenReturn(testExternalApiProjectViewModels);

        ExternalApiProjectViewModel expect = testExternalApiProjectViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].description").value(expect.getDescription()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).getAllProjects();
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void getAllProjects_whenZeroProjects_emptyCollection() throws Exception {
        when(externalApiServiceMock.getAllProjects()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/v1/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(externalApiServiceMock, times(1)).getAllProjects();
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void getAllDatasets_when2Datasets_2Datasets() throws Exception {
        List<ExternalApiDatasetViewModel> testExternalApiDatasetViewModel = ExternalApiTokenUtils
                .createTestExternalApiDatasetViewModels(2);

        when(externalApiServiceMock.getAllDatasets()).thenReturn(testExternalApiDatasetViewModel);

        ExternalApiDatasetViewModel expect = testExternalApiDatasetViewModel.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/dataset"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].media", hasSize(0)))
                .andReturn();

        verify(externalApiServiceMock, times(1)).getAllDatasets();
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void getAllDatasets_whenZeroDatasets_emptyCollection() throws Exception {
        when(externalApiServiceMock.getAllDatasets()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/v1/dataset"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(externalApiServiceMock, times(1)).getAllDatasets();
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void addDataset_whenProjectIdAndDatasetIdAreValid_200OK() throws Exception {
        // When
        doNothing().when(externalApiServiceMock).addDataset(anyString(), anyString());

        mockMvc.perform(post("/api/v1/project/{projectId}/dataset/{datasetId}", PROJECT_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).addDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        Assertions.assertEquals(projectIdCapture.getValue(), PROJECT_ID);
        Assertions.assertEquals(datasetIdCapture.getValue(), DATASET_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void addDataset_whenProjectIdOrDatasetIdAreNotValid_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(post("/api/v1/project/{projectId}/dataset/{datasetId}", " ", DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void removeDataset_whenProjectIdAndDatasetIdAreValid_200OK() throws Exception {
        // When
        doNothing().when(externalApiServiceMock).removeDataset(anyString(), anyString());

        mockMvc.perform(delete("/api/v1/project/{projectId}/dataset/{datasetId}/remove", PROJECT_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).removeDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        Assertions.assertEquals(projectIdCapture.getValue(), PROJECT_ID);
        Assertions.assertEquals(datasetIdCapture.getValue(), DATASET_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void removeDataset_whenProjectIdOrDatasetIdAreNotValid_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(delete("/api/v1/project/{projectId}/dataset/{datasetId}/remove", " ", DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void createDataset_whenDatasetCreateBindingModelIsValid_createDataset() throws Exception {
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils.createTestExternalApiDatasetViewModel();

        when(externalApiServiceMock.createDataset(any(ExternalApiCreateDatasetBindingModel.class), anyBoolean()))
                .thenReturn(testExternalApiDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testExternalApiCreateDatasetBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testExternalApiCreateDatasetBindingModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testExternalApiCreateDatasetBindingModel.getShortDescription()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).createDataset(any(ExternalApiCreateDatasetBindingModel.class), anyBoolean());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void createDataset_whenDatasetCreateBindingModelIsNotValid_throwException() throws Exception {
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils.createTestExternalApiDatasetViewModel();

        testExternalApiCreateDatasetBindingModel.setShortDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et " +
                "justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
                "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea " +
                "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");

        when(externalApiServiceMock.createDataset(any(ExternalApiCreateDatasetBindingModel.class), anyBoolean()))
                .thenReturn(testExternalApiDatasetViewModel);

        String requestBody = objectMapper.writeValueAsString(testExternalApiCreateDatasetBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/v1/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void createDataset_whenDatasetNameIsInvalid_throwException() throws Exception {
        ExternalApiCreateDatasetBindingModel testExternalApiCreateDatasetBindingModel = ExternalApiTokenUtils
                .createTestExternalApiCreateDatasetBindingModel();
        testExternalApiCreateDatasetBindingModel.setName("###");

        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils
                .createTestExternalApiDatasetViewModel();

        when(externalApiServiceMock.createDataset(any(ExternalApiCreateDatasetBindingModel.class), anyBoolean()))
                .thenReturn(testExternalApiDatasetViewModel);

        String requestBody = objectMapper.writeValueAsString(testExternalApiCreateDatasetBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/v1/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void createImageUrl_when2Urls_2Urls() throws Exception {
        List<UrlImageUploadViewModel> tesUrlImageUploadViewModels = ImageUtils.createTesImageUploadViewModels(2);

        Set<String> testImageUrlSet = ImageUtils.createTestImageUrlSet(2);

        when(externalApiServiceMock.createImageUrl(anyString(), anySet(), anyBoolean()))
                .thenReturn(tesUrlImageUploadViewModels);

        String requestBody = objectMapper.writeValueAsString(testImageUrlSet);

        UrlImageUploadViewModel expected = tesUrlImageUploadViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/media/{datasetId}/url", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].imageUrl").value(expected.getImageUrl()))
                .andExpect(jsonPath("$[0].mediaUploadStatus").value(expected.getMediaUploadStatus()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).createImageUrl(anyString(), anySet(), anyBoolean());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void createImageUrl_whenZeroUrls_emptyCollection() throws Exception {
        when(externalApiServiceMock.createImageUrl(anyString(), anySet(), anyBoolean()))
                .thenReturn(new ArrayList<>());

        String requestBody = objectMapper.writeValueAsString(new HashSet<>());

        mockMvc
                .perform(post("/api/v1/media/{datasetId}/url", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(externalApiServiceMock, times(1)).createImageUrl(anyString(), anySet(), anyBoolean());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void deleteImage_whenImageIdIsValid_200OK() throws Exception {
        mockMvc.perform(delete("/api/v1/media/{imageId}", IMAGE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> deleteImageCapture = ArgumentCaptor.forClass(Boolean.class);
        verify(externalApiServiceMock, times(1)).deleteMediaFile(idCapture.capture(), deleteImageCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);
        Assertions.assertEquals(deleteImageCapture.getValue(), true);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void deleteImage_whenImageIdIsEmptyString_throwException() throws Exception {
        mockMvc.perform(delete("/api/v1/media/{imageId}", ""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void streamImageFile_whenInputsAreValid_200OK() throws Exception {
        mockMvc.perform(get("/api/v1/media/{imageId}", IMAGE_ID)
                .param("dl", "true"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock).streamMediaFile(idCapture.capture(), any(HttpServletResponse.class), anyBoolean());
        verify(externalApiServiceMock, times(1)).streamMediaFile(anyString(), any(HttpServletResponse.class), anyBoolean());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void streamImageFile_whenImageIdIsEmptyString_throwException() throws Exception {
        mockMvc.perform(get("/api/v1/media/{imageId}", "")
                .param("dl", "true"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void exportLabels_whenInputsAreValid_200OK() throws Exception {
        doNothing()
                .when(externalApiServiceMock).
                exportProjectLabels(anyString(), any(HttpServletResponse.class));

        mockMvc.perform(get("/api/v1/export/{projectId}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock).exportProjectLabels(idCapture.capture(), any(HttpServletResponse.class));
        verify(externalApiServiceMock, times(1)).exportProjectLabels(anyString(), any(HttpServletResponse.class));
        Assertions.assertEquals(idCapture.getValue(), PROJECT_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void exportLabels_whenProjectIdIsEmptyString_throwException() throws Exception {
        mockMvc.perform(get("/api/v1/export/{projectId}", ""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void clearConfig_whenConfigIdIsValid_clearConfig() throws Exception {
        // Given
        LcConfigDeleteViewModel testLcConfigDeleteViewModel = LabelConfigurationUtils.createTestLcConfigDeleteViewModel();

        // When
        when(externalApiServiceMock.clearConfig(anyString()))
                .thenReturn(testLcConfigDeleteViewModel);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/config/{configId}", LC_CONFIG_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.configId").value(testLcConfigDeleteViewModel.getConfigId()))
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).clearConfig(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), LC_CONFIG_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void uploadLabelConfiguration_whenLcEntryUpdateBindingModelIsValid_uploadLabelConfiguration() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> rootLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(2);
        List<LcEntryViewModel> childrenLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(3);

        testLabelConfigurationViewModel.setEntries(rootLcEntryViewModels);
        testLabelConfigurationViewModel.getEntries().get(0).setChildren(childrenLcEntryViewModels);

        // When
        when(externalApiServiceMock.uploadLabelConfiguration(anyString(), anyList()))
                .thenReturn(testLabelConfigurationViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/v1/config/{configId}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelConfigurationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelConfigurationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.entries[0].children", hasSize(3)))
                .andReturn();

        verify(externalApiServiceMock, times(1)).uploadLabelConfiguration(anyString(), anyList());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void uploadPredictedValues_whenInputIsValid_uploadPredictedValues() throws Exception {
        ExternalApiSchemaValidationViewModel testModel = new ExternalApiSchemaValidationViewModel();
        testModel.setErrorMessages("");


        when(externalApiServiceMock.uploadPredictedValues(anyString(), any()))
                                .thenReturn(testModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/project/{projectId}/prediction", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$['errorMessages']").isEmpty())
                .andReturn();

        verify(externalApiServiceMock, times(1)).uploadPredictedValues(anyString(), any());
    }

    @Test
    void uploadPredictedValues_returnErrorMessage_whenErrorMessageIsSet() throws Exception {
        ExternalApiSchemaValidationViewModel testModel = new ExternalApiSchemaValidationViewModel();
        testModel.setErrorMessages("errorMessage");

        when(externalApiServiceMock.uploadPredictedValues(anyString(), any()))
                .thenReturn(testModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/project/{projectId}/prediction", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$['errorMessages']").isNotEmpty())
                .andReturn();

        verify(externalApiServiceMock, times(1)).uploadPredictedValues(anyString(), any());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void uploadPredictedValues_JsonIsInvalidAgainstSchema_returnErrorMessage() throws Exception {
        ExternalApiSchemaValidationViewModel testModel = new ExternalApiSchemaValidationViewModel();
        testModel.setErrorMessages("errorMessage");

        when(externalApiServiceMock.uploadPredictedValues(anyString(), any()))
                .thenReturn(testModel);

        String requestBody = "[{\n" +
                "  \"internal_media_ID\" : \"c849f53e-b4d9-48a8-8fe9-786727c666a7\",\n" +
                "  \"global_classifications\" : {},\n" +
                "  \"keepData\" : 3,\n" +
                "  \"labels\" : {}\n" +
                "}]";

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/project/{projectId}/prediction", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$['errorMessages']").isNotEmpty())
                .andReturn();

        verify(externalApiServiceMock, times(1)).uploadPredictedValues(anyString(), any());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void uploadLabelConfiguration_whenLcEntryKeyIsTooLong_throwException() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);
        LcEntryUpdateBindingModel lcEntryUpdateBindingModel = testLcEntryUpdateBindingModels.get(0);
        lcEntryUpdateBindingModel.setEntryKey("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata ");

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        Exception resolvedException = mockMvc
                .perform(put("/api/v1/config/{configId}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void uploadLabelConfiguration_whenLcEntryValueIsTooLong_throwException() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);
        LcEntryUpdateBindingModel lcEntryUpdateBindingModel = testLcEntryUpdateBindingModels.get(0);
        lcEntryUpdateBindingModel.setEntryValue("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata ");

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        Exception resolvedException = mockMvc
                .perform(put("/api/v1/config/{configId}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void getDataset_whenDatasetIdIsValid_getDataset() throws Exception {
        ExternalApiDatasetViewModel testExternalApiDatasetViewModel = ExternalApiTokenUtils.createTestExternalApiDatasetViewModel();

        when(externalApiServiceMock.getDataset(anyString())).thenReturn(testExternalApiDatasetViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/dataset/{datasetId}", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testExternalApiDatasetViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testExternalApiDatasetViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testExternalApiDatasetViewModel.getShortDescription()))
                .andExpect(jsonPath("$.owner").value(testExternalApiDatasetViewModel.getOwner()))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testExternalApiDatasetViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(externalApiServiceMock, times(1)).getDataset(anyString());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void getTask_whenTaskIdIsValid_getTask() throws Exception {
        LabelModeDataViewModel testLabelModeDataViewModel = LabelTaskUtils.createTestLabelModeDataViewModel();

        when(externalApiServiceMock.getTask(anyString())).thenReturn(testLabelModeDataViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/task/{id}", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(testLabelModeDataViewModel.getTaskId()))
                .andExpect(jsonPath("$.media.id").value(testLabelModeDataViewModel.getMedia().getId()))
                .andExpect(jsonPath("$.labelIteration.id").value(testLabelModeDataViewModel.getLabelIteration().getId()))
                .andExpect(jsonPath("$.labelConfig.id").value(testLabelModeDataViewModel.getLabelConfig().getId()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelModeDataViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(externalApiServiceMock, times(1)).getTask(anyString());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void getTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(get("/api/v1/task/{id}", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void skipTask_whenTaskIdIsValid_skipTask() throws Exception {

        doNothing()
                .when(externalApiServiceMock)
                .skipTask(LABEL_TASK_ID);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/v1/task/{id}/skipTask", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).skipTask(idCapture.capture());

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void skipTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/v1/task/{id}/skipTask", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void completeTask_whenTaskIdIsValid_completeTask() throws Exception {
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();
        LabelTaskCompleteViewModel testLabelTaskCompleteViewModel = LabelTaskUtils.createTestLabelTaskCompleteViewModel();

        when(externalApiServiceMock.completeTask(anyString(), any(LabelTaskCompleteBindingModel.class)))
                .thenReturn(testLabelTaskCompleteViewModel);

        String requestBody = objectMapper.writeValueAsString(testLabelTaskCompleteBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/v1/task/{id}/completeTask", LABEL_TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasLabelConfigChanged").value(testLabelTaskCompleteViewModel.isHasLabelConfigChanged()))
                .andExpect(jsonPath("$.currentTaskId").value(testLabelTaskCompleteViewModel.getCurrentTaskId()))
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).completeTask(idCapture.capture(), any(LabelTaskCompleteBindingModel.class));

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void completeTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        String requestBody = objectMapper.writeValueAsString(testLabelTaskCompleteBindingModel);

        Exception resolvedException = mockMvc
                .perform(put("/api/v1/task/{id}/completeTask", " ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void getAllProjectTasks_when2TasksForCurrentProject_2Tasks() throws Exception {
        List<LabelTaskViewModel> testLabelTaskViewModels = ProjectUtils.createTestLabelTaskViewModels(2);

        when(externalApiServiceMock.getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt()))
                .thenReturn(testLabelTaskViewModels);

        LabelTaskViewModel expect = testLabelTaskViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/task/{projectId}/list", PROJECT_ID)
                        .param("search", "test")
                        .param("state", "BACKLOG")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].taskId").value(expect.getTaskId()))
                .andExpect(jsonPath("$[0].projectId").value(expect.getProjectId()))
                .andExpect(jsonPath("$[0].projectName").value(expect.getProjectName()))
                .andExpect(jsonPath("$[0].labelTaskState").value(expect.getLabelTaskState()))
                .andExpect(jsonPath("$[0].mediaId").value(expect.getMediaId()))
                .andExpect(jsonPath("$[0].mediaName").value(expect.getMediaName()))
                .andExpect(jsonPath("$[0].labeler").value(expect.getLabeler()))
                .andExpect(jsonPath("$[0].iterationId").value(expect.getIterationId()))
                .andExpect(jsonPath("$[0].iterationRun").value(expect.getIterationRun()))
                .andReturn();


        String expectedResponseBody = objectMapper.writeValueAsString(testLabelTaskViewModels);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(externalApiServiceMock, times(1))
                .getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void createLabelValuesTree_whenLcEntryValueCreateBindingModelIsValid_createLabelValueTree() throws Exception {
        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(externalApiServiceMock.createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/lcValue/{lcEntryId}", LC_ENTRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class));
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void updateSingleLabelValue_whenLcEntryValueUpdateBindingModelIsValid_updateSingleLabelValue() throws Exception {
        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(2);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(externalApiServiceMock.updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueUpdateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/v1/lcValue/{lcValueId}", LC_ENTRY_VALUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class));
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void deleteLabelValue_whenLcValueIdIsValid_200OK() throws Exception {
        // Given
        mockMvc.perform(delete("/api/v1/lcValue/{lcValueId}", LC_ENTRY_VALUE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).deleteLcValue(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), LC_ENTRY_VALUE_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void changeTypeOfSingleLabelValue_whenLcEntryValueUpdateBindingModelIsValid_changeTypeOfSingleLabelValue() throws Exception {
        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(externalApiServiceMock.changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(changeValueClassBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/v1/lcValue/{lcValueId}/changeType", LC_ENTRY_VALUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(externalApiServiceMock, times(1)).changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class));
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void prepareImage_whenImageIdIsValid_prepareImage() throws Exception {
       mockMvc
                .perform(post("/api/v1/aiseg/{imageId}/prepare", IMAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).prepare(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void calculateImage_whenAiSegCalculateModelIsValid_calculateImage() throws Exception {
        // Given
        AiSegCalculate testAiSegCalculate = AisegUtils.createTestAiSegCalculate();
        AiSegResponse testAiSegResponse = AisegUtils.createTestAiSegResponse();

        when(externalApiServiceMock.calculate(any(AiSegCalculate.class)))
                .thenReturn(testAiSegResponse);

        // Then
        String requestBody = objectMapper.writeValueAsString(testAiSegCalculate);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/v1/aiseg/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.imageId").value(testAiSegResponse.getImageId()))
                .andExpect(jsonPath("$.result", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testAiSegResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(externalApiServiceMock, times(1)).calculate( any(AiSegCalculate.class));
        verifyNoMoreInteractions(externalApiServiceMock);
    }

    @Test
    void finishImage_whenImageIdIsValid_finishImage() throws Exception {
        mockMvc
                .perform(delete("/api/v1/aiseg/{imageId}/finish", IMAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(externalApiServiceMock, times(1)).finish(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);

        verifyNoMoreInteractions(externalApiServiceMock);
    }
}
