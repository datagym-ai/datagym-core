package ai.datagym.application.dataset.controller;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.media.models.viewModels.LocalImageViewModel;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.project.models.viewModels.ProjectDatasetViewModel;
import ai.datagym.application.testUtils.DatasetUtils;
import ai.datagym.application.testUtils.ImageUtils;
import ai.datagym.application.testUtils.ProjectUtils;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
class DatasetControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private DatasetService datasetServiceMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesDatasetController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("datasetController"));
    }

    @Test
    void createDataset_whenDatasetCreateBindingModelIsValid_createDataset() throws Exception {
        DatasetCreateBindingModel testDatasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(testDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testDatasetCreateBindingModel);


        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testDatasetCreateBindingModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetCreateBindingModel.getShortDescription()))
                .andReturn();

        verify(datasetServiceMock, times(1)).createDataset(any(DatasetCreateBindingModel.class), anyBoolean());
    }

    @Test
    void createDataset_whenDatasetCreateBindingModelIsNotValid_throwException() throws Exception {
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        DatasetCreateBindingModel testDatasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        testDatasetCreateBindingModel.setShortDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et " +
                "justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
                "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea " +
                "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(testDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testDatasetCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void createDataset_whenDatasetNameAlreadyExists_throwException() throws Exception {
        DatasetCreateBindingModel testDatasetCreateBindingModel = DatasetUtils.createTestDatasetCreateBindingModel();
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(testDatasetViewModel);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(false);

        String requestBody = objectMapper.writeValueAsString(testDatasetCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/dataset/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void getDataset_whenDatasetIdIsValid_getDataset() throws Exception {
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        when(datasetServiceMock.getDataset(anyString(), anyBoolean())).thenReturn(testDatasetViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/{id}", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDatasetViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testDatasetViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(testDatasetViewModel.isDeleted()))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testDatasetViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetServiceMock, times(1)).getDataset(anyString(), anyBoolean());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getDatasetWithAllProjects_whenDatasetIdIsValidAndZeroProjects_getDatasetWithAllZeroProjects() throws Exception {
        DatasetProjectViewModel testDatasetProjectViewModel = DatasetUtils.createTestDatasetProjectViewModel(DATASET_ID);

        when(datasetServiceMock.getDatasetWithProjects(anyString())).thenReturn(testDatasetProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/{datasetId}/project", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDatasetProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testDatasetProjectViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(testDatasetProjectViewModel.isDeleted()))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andExpect(jsonPath("$.projects", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testDatasetProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetServiceMock, times(1)).getDatasetWithProjects(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getDatasetWithAllProjects_whenDatasetIdIsValidAndTwoProjects_getDatasetWithAllTwoProjects() throws Exception {
        HashSet<ProjectDatasetViewModel> projectDatasetViewModels = new HashSet<>(ProjectUtils.createTestProjectDatasetViewModels(2));

        DatasetProjectViewModel testDatasetProjectViewModel = DatasetUtils.createTestDatasetProjectViewModel(DATASET_ID);
        testDatasetProjectViewModel.setProjects(projectDatasetViewModels);

        when(datasetServiceMock.getDatasetWithProjects(anyString())).thenReturn(testDatasetProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/{datasetId}/project", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDatasetProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testDatasetProjectViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(testDatasetProjectViewModel.isDeleted()))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andExpect(jsonPath("$.projects", hasSize(2)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testDatasetProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetServiceMock, times(1)).getDatasetWithProjects(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getAllDatasets_when2Datasets_2Datasets() throws Exception {
        List<DatasetAllViewModel> testListDatasetAllViewModel = DatasetUtils.createTestListDatasetAllViewModel(2);

        when(datasetServiceMock.getAllDatasets(anyString())).thenReturn(testListDatasetAllViewModel);

        DatasetAllViewModel expect = testListDatasetAllViewModel.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset")
                        .param("org", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].mediaCount").value(1))
                .andReturn();

        verify(datasetServiceMock, times(1)).getAllDatasets(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getAllDatasets_whenZeroDatasets_emptyCollection() throws Exception {
        when(datasetServiceMock.getAllDatasets(anyString())).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/dataset")
                        .param("org", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(datasetServiceMock, times(1)).getAllDatasets(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void updateDataset_whenDatasetUpdateBindingModelIsValidAndNameIsUnique_updateDataset() throws Exception {
        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        when(datasetServiceMock.updateDataset(anyString(), any(DatasetUpdateBindingModel.class)))
                .thenReturn(testDatasetViewModel);

        String requestBody = objectMapper.writeValueAsString(testDatasetUpdateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/dataset/{id}", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testDatasetUpdateBindingModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetUpdateBindingModel.getShortDescription()))
                .andReturn();

        verify(datasetServiceMock, times(1)).updateDataset(anyString(), any());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void updateDataset_whenDatasetUpdateBindingModelIsNotValid_throwException() throws Exception {
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);

        DatasetUpdateBindingModel testDatasetUpdateBindingModel = DatasetUtils.createTestDatasetUpdateBindingModel();
        testDatasetUpdateBindingModel.setName("");

        when(datasetServiceMock.updateDataset(anyString(), any(DatasetUpdateBindingModel.class)))
                .thenReturn(testDatasetViewModel);

        String requestBody = objectMapper.writeValueAsString(testDatasetUpdateBindingModel);

        Exception resolvedException = mockMvc
                .perform(put("/api/dataset/{id}", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void deleteDataset_whenDatasetIdIsValid_setIsDeletedToTrue() throws Exception {
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);
        testDatasetViewModel.setDeleted(true);

        when(datasetServiceMock.deleteDatasetById(anyString(), eq(true))).thenReturn(testDatasetViewModel);

        MvcResult mvcResult = mockMvc
                .perform(delete("/api/dataset/{id}", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDatasetViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testDatasetViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testDatasetViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetServiceMock, times(1)).deleteDatasetById(anyString(), eq(true));
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void restore_whenDatasetIdIsValid_setIsDeletedToFalse() throws Exception {
        DatasetViewModel testDatasetViewModel = DatasetUtils.createTestDatasetViewModel(DATASET_ID);
        testDatasetViewModel.setDeleted(false);

        when(datasetServiceMock.deleteDatasetById(anyString(), eq(false))).thenReturn(testDatasetViewModel);

        MvcResult mvcResult = mockMvc
                .perform(delete("/api/dataset/{id}/restore", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDatasetViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testDatasetViewModel.getName()))
                .andExpect(jsonPath("$.shortDescription").value(testDatasetViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.media", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testDatasetViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(datasetServiceMock, times(1)).deleteDatasetById(anyString(), eq(false));
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void permanentDeleteDatasetFromDb_whenDatasetIdIsValid_permanentDeleteDatasetFromDb() throws Exception {
        mockMvc
                .perform(delete("/api/dataset/{id}/deleteFromDb", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk());

        verify(datasetServiceMock, times(1)).permanentDeleteDatasetFromDB(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void createImageFile_whenInputIsValid_createImageFile() throws Exception {
        LocalImageViewModel testLocalImageViewModel = ImageUtils.createTestLocalImageViewModel();

        when(datasetServiceMock.createImageFile(anyString(), anyString(), any()))
                .thenReturn(testLocalImageViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/{datasetId}/file", DATASET_ID)
                        .with(request -> {
                            request.addHeader("X-filename", "aW1hZ2VOYW1l");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLocalImageViewModel.getId()))
                .andExpect(jsonPath("$.mediaName").value(testLocalImageViewModel.getMediaName()))
                .andExpect(jsonPath("$.mediaSourceType").value(testLocalImageViewModel.getMediaSourceType()))
                .andExpect(jsonPath("$.height").value(testLocalImageViewModel.getHeight()))
                .andExpect(jsonPath("$.width").value(testLocalImageViewModel.getWidth()))
                .andReturn();

        verify(datasetServiceMock, times(1)).createImageFile(anyString(), anyString(), any());
    }

    @Test
    void getAllImages_when2Images_2Images() throws Exception {
        List<MediaViewModel> testListMediaViewModel = ImageUtils.createTestListImageViewModel(2);

        when(datasetServiceMock.getAllMedia(anyString())).thenReturn(testListMediaViewModel);

        MediaViewModel expect = testListMediaViewModel.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/{datasetId}/file", DATASET_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].mediaName").value(expect.getMediaName()))
                .andExpect(jsonPath("$[0].mediaSourceType").value(expect.getMediaSourceType()))
                .andReturn();

        verify(datasetServiceMock, times(1)).getAllMedia(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getAllImages_whenZeroImages_emptyCollection() throws Exception {
        when(datasetServiceMock.getAllMedia(DATASET_ID)).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/dataset/{datasetId}/file", DATASET_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(datasetServiceMock, times(1)).getAllMedia(anyString());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin_when2Datasets_2Datasets() throws Exception {
        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);

        when(datasetServiceMock.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin()).thenReturn(testListDatasetViewModel);

        DatasetViewModel expect = testListDatasetViewModel.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/dataset/admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].media", hasSize(0)))
                .andReturn();

        verify(datasetServiceMock, times(1))
                .getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin_whenZeroDatasets_emptyCollection() throws Exception {
        when(datasetServiceMock.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/dataset/admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(datasetServiceMock, times(1))
                .getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void createImageUrl_when2Urls_2Urls() throws Exception {
        List<UrlImageUploadViewModel> tesUrlImageUploadViewModels = ImageUtils.createTesImageUploadViewModels(2);

        Set<String> testImageUrlSet = ImageUtils.createTestImageUrlSet(2);

        when(datasetServiceMock.createImagesByShareableLink(anyString(), anySet(), anyBoolean()))
                .thenReturn(tesUrlImageUploadViewModels);

        String requestBody = objectMapper.writeValueAsString(testImageUrlSet);

        UrlImageUploadViewModel expected = tesUrlImageUploadViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/dataset/{datasetId}/url", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].imageUrl").value(expected.getImageUrl()))
                .andExpect(jsonPath("$[0].mediaUploadStatus").value(expected.getMediaUploadStatus()))
                .andReturn();

        verify(datasetServiceMock, times(1)).createImagesByShareableLink(anyString(), anySet(), anyBoolean());
        verifyNoMoreInteractions(datasetServiceMock);
    }

    @Test
    void createImageUrl_whenZeroUrls_emptyCollection() throws Exception {
        when(datasetServiceMock.createImagesByShareableLink(anyString(), anySet(), anyBoolean()))
                .thenReturn(new ArrayList<>());

        String requestBody = objectMapper.writeValueAsString(new HashSet<>());

        mockMvc
                .perform(post("/api/dataset/{datasetId}/url", DATASET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(datasetServiceMock, times(1)).createImagesByShareableLink(anyString(), anySet(), anyBoolean());
        verifyNoMoreInteractions(datasetServiceMock);
    }
}