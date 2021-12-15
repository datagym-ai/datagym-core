package ai.datagym.application.project.controller;

import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.externalAPI.service.ExternalApiService;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.service.ProjectService;
import ai.datagym.application.testUtils.ProjectUtils;
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
import java.util.List;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyBoolean;
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
class ProjectControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ProjectService projectServiceMock;

    @MockBean
    private ExternalApiService externalApiServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesProjectController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("projectController"));
    }

    @Test
    void createProject_whenProjectCreateBindingModelIsValid_createProject() throws Exception {
        ProjectCreateBindingModel testProjectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        when(projectServiceMock.createProject(any(), anyBoolean()))
                .thenReturn(testProjectViewModel);

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(any(), any()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testProjectCreateBindingModel);


        MvcResult mvcResult = mockMvc
                .perform(post("/api/project/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testProjectCreateBindingModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectCreateBindingModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectCreateBindingModel.getShortDescription()))
                .andReturn();

        verify(projectServiceMock, times(1)).createProject(any(), anyBoolean());
    }

    @Test
    void createProject_whenProjectCreateBindingModelIsNotValid_throwException() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        ProjectCreateBindingModel testProjectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();
        testProjectCreateBindingModel.setShortDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et " +
                "justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut " +
                "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea " +
                "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(any(), any()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testProjectCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/project/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void createProject_whenProjectNameAlreadyExists_throwException() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);
        ProjectCreateBindingModel testProjectCreateBindingModel = ProjectUtils.createTestProjectCreateBindingModel();

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(any(), any()))
                .thenReturn(false);

        String requestBody = objectMapper.writeValueAsString(testProjectCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/project/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void getProject_whenProjectIdIsValid_getProject() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        when(projectServiceMock.getProject(anyString())).thenReturn(testProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/{id}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectViewModel.getShortDescription()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).getProject(anyString());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjects_when2Projects_2Projects() throws Exception {
        List<ProjectViewModel> testProjectViewModels = ProjectUtils.createTestProjectViewModels(2);

        when(projectServiceMock.getAllProjects()).thenReturn(testProjectViewModels);

        ProjectViewModel expect = testProjectViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].description").value(expect.getDescription()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andReturn();

        verify(projectServiceMock, times(1)).getAllProjects();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjects_whenZeroProjects_emptyCollection() throws Exception {
        when(projectServiceMock.getAllProjects()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(projectServiceMock, times(1)).getAllProjects();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void updateProject_whenProjectUpdateBindingModelIsValid_updateProject() throws Exception {
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        when(projectServiceMock.updateProject(anyString(), any(ProjectUpdateBindingModel.class)))
                .thenReturn(testProjectViewModel);

        String requestBody = objectMapper.writeValueAsString(testProjectUpdateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/project/{id}", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(testProjectUpdateBindingModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectUpdateBindingModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectUpdateBindingModel.getShortDescription()))
                .andReturn();

        verify(projectServiceMock, times(1)).updateProject(anyString(), any());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void updateProject_whenProjectUpdateBindingModelIsNotValid_throwException() throws Exception {
        ProjectUpdateBindingModel testProjectUpdateBindingModel = ProjectUtils.createTestProjectUpdateBindingModel();
        testProjectUpdateBindingModel.setName("");

        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        when(projectServiceMock.updateProject(anyString(), any(ProjectUpdateBindingModel.class)))
                .thenReturn(testProjectViewModel);

        String requestBody = objectMapper.writeValueAsString(testProjectUpdateBindingModel);

        Exception resolvedException = mockMvc
                .perform(put("/api/project/{id}", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void deleteProject_whenProjectIdIsValid_setIsDeletedToTrue() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        testProjectViewModel.setDeleted(true);

        when(projectServiceMock.deleteProjectById(anyString(), anyBoolean())).thenReturn(testProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(delete("/api/project/{id}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(testProjectViewModel.isDeleted()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).deleteProjectById(anyString(), anyBoolean());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void restoreProject_whenProjectIdIsValid_setIsDeletedToFalse() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        testProjectViewModel.setDeleted(false);

        when(projectServiceMock.deleteProjectById(anyString(), anyBoolean())).thenReturn(testProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(delete("/api/project/{id}/restore", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.deleted").value(testProjectViewModel.isDeleted()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).deleteProjectById(anyString(), anyBoolean());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void permanentDeleteProjectFromDb_whenProjectIdIsValid_permanentDeleteProjectFromDb() throws Exception {
        mockMvc
                .perform(delete("/api/project/{id}/deleteFromDb", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk());

        verify(projectServiceMock, times(1)).permanentDeleteProjectFromDB(anyString());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void pinProject_whenProjectIdIsValid_setIsPinnedToTrue() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        testProjectViewModel.setPinned(true);

        when(projectServiceMock.pinProject(anyString(), anyBoolean())).thenReturn(testProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/project/{id}/pin", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.pinned").value(testProjectViewModel.isPinned()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).pinProject(anyString(), anyBoolean());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void unpinProject_whenProjectIdIsValid_setIsPinnedToFalse() throws Exception {
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        testProjectViewModel.setPinned(false);

        when(projectServiceMock.pinProject(anyString(), anyBoolean())).thenReturn(testProjectViewModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/project/{id}/unpin", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectViewModel.getShortDescription()))
                .andExpect(jsonPath("$.pinned").value(testProjectViewModel.isPinned()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).pinProject(anyString(), anyBoolean());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void addDataset_whenProjectIdAndDatasetIdAreValid_200OK() throws Exception {
        // When
        doNothing().when(projectServiceMock).addDataset(anyString(), anyString());

        mockMvc.perform(post("/api/project/{projectId}/dataset/{datasetId}", PROJECT_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);
        verify(projectServiceMock, times(1)).addDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        Assertions.assertEquals(projectIdCapture.getValue(), PROJECT_ID);
        Assertions.assertEquals(datasetIdCapture.getValue(), DATASET_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void addDataset_whenProjectIdOrDatasetIdAreNotValid_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(post("/api/project/{projectId}/dataset/{datasetId}", " ", DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void removeDataset_whenProjectIdAndDatasetIdAreValid_200OK() throws Exception {
        // When
        doNothing().when(projectServiceMock).removeDataset(anyString(), anyString());

        mockMvc.perform(delete("/api/project/{projectId}/dataset/{datasetId}/remove", PROJECT_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<String> projectIdCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> datasetIdCapture = ArgumentCaptor.forClass(String.class);
        verify(projectServiceMock, times(1)).removeDataset(projectIdCapture.capture(), datasetIdCapture.capture());
        Assertions.assertEquals(projectIdCapture.getValue(), PROJECT_ID);
        Assertions.assertEquals(datasetIdCapture.getValue(), DATASET_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void removeDataset_whenProjectIdOrDatasetIdAreNotValid_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(delete("/api/project/{projectId}/dataset/{datasetId}/remove", " ", DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void getAllProjectTasks_when2TasksForCurrentProject_2Tasks() throws Exception {
        List<LabelTaskViewModel> testLabelTaskViewModels = ProjectUtils.createTestLabelTaskViewModels(2);

        when(projectServiceMock.getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt()))
                .thenReturn(testLabelTaskViewModels);

        LabelTaskViewModel expect = testLabelTaskViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/{id}/task", PROJECT_ID)
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

        verify(projectServiceMock, times(1))
                .getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjects_whenZeroTasksForCurrentProject_emptyCollection() throws Exception {
        when(projectServiceMock.getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt()))
                .thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/project/{id}/task", PROJECT_ID)
                        .param("search", "test")
                        .param("state", "BACKLOG")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(projectServiceMock, times(1))
                .getProjectTasks(anyString(), anyString(), any(LabelTaskState.class), anyInt());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjectsFromOrganisation_when2Projects_2Projects() throws Exception {
        List<ProjectViewModel> testProjectViewModels = ProjectUtils.createTestProjectViewModels(2);

        when(projectServiceMock.getAllProjectsFromOrganisation(anyString()))
                .thenReturn(testProjectViewModels);

        ProjectViewModel expect = testProjectViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/{orgId}/org", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].description").value(expect.getDescription()))
                .andExpect(jsonPath("$[0].pinned").value(expect.isPinned()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].datasets", nullValue()))
                .andExpect(jsonPath("$[0].labelConfigurationId").value(expect.getLabelConfigurationId()))
                .andExpect(jsonPath("$[0].labelIterationId").value(expect.getLabelIterationId()))
                .andExpect(jsonPath("$[0].owner").value(expect.getOwner()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModels);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1))
                .getAllProjectsFromOrganisation(anyString());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjectsFromOrganisation_whenZeroProjects_emptyCollection() throws Exception {
        when(projectServiceMock.getAllProjectsFromOrganisation(anyString()))
                .thenReturn(new ArrayList<>());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/{orgId}/org", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(projectServiceMock, times(1)).getAllProjectsFromOrganisation(anyString());
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getAllProjectsFromOrganisationAndLoggedInUserIsAdmin_when2TasksForCurrentProject_2Tasks() throws Exception {
        List<ProjectViewModel> testProjectViewModels = ProjectUtils.createTestProjectViewModels(2);

        when(projectServiceMock.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin())
                .thenReturn(testProjectViewModels);

        ProjectViewModel expect = testProjectViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].description").value(expect.getDescription()))
                .andExpect(jsonPath("$[0].pinned").value(expect.isPinned()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].datasets", nullValue()))
                .andExpect(jsonPath("$[0].labelConfigurationId").value(expect.getLabelConfigurationId()))
                .andExpect(jsonPath("$[0].labelIterationId").value(expect.getLabelIterationId()))
                .andExpect(jsonPath("$[0].owner").value(expect.getOwner()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectViewModels);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1))
                .getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void uploadPredictedValues_whenInputIsValid_uploadPredictedValues() throws Exception {
        ExternalApiSchemaValidationViewModel testModel = new ExternalApiSchemaValidationViewModel();
        testModel.setErrorMessages("");

        when(externalApiServiceMock.uploadPredictedValues(anyString(), any()))
                .thenReturn(testModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/project/{projectId}/prediction", PROJECT_ID))
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
    void getAllProjectsFromOrganisationAndLoggedInUserIsAdmin_whenZeroTasksForCurrentProject_emptyCollection() throws Exception {
        when(projectServiceMock.getAllProjectsFromOrganisation(anyString()))
                .thenReturn(new ArrayList<>());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(projectServiceMock, times(1))
                .getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void exportLabels_whenProjectIdIsValid_200() throws Exception {

        mockMvc
                .perform(get("/api/project/{projectId}/export", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(projectServiceMock, times(1))
                .exportProjectLabels(idCapture.capture(), any(HttpServletResponse.class));
        Assertions.assertEquals(idCapture.getValue(), PROJECT_ID);

        verifyNoMoreInteractions(projectServiceMock);
    }

    @Test
    void getDashboardData_whenProjectIdIsValid_getDashboardData() throws Exception {
        ProjectDashboardViewModel testProjectDashboardViewModel = ProjectUtils.createTestProjectDashboardViewModel(PROJECT_ID);

        when(projectServiceMock.getDashboardData(anyString())).thenReturn(testProjectDashboardViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/project/{projectId}/dashboard", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProjectDashboardViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testProjectDashboardViewModel.getName()))
                .andExpect(jsonPath("$.description").value(testProjectDashboardViewModel.getDescription()))
                .andExpect(jsonPath("$.shortDescription").value(testProjectDashboardViewModel.getShortDescription()))
                .andExpect(jsonPath("$.countTasks").value(testProjectDashboardViewModel.getCountTasks()))
                .andExpect(jsonPath("$.countDatasets").value(testProjectDashboardViewModel.getCountDatasets()))
                .andExpect(jsonPath("$.approvedReviewPerformance").value(testProjectDashboardViewModel.getApprovedReviewPerformance()))
                .andExpect(jsonPath("$.declinedReviewPerformance").value(testProjectDashboardViewModel.getDeclinedReviewPerformance()))
                .andExpect(jsonPath("$.hasLabelConfiguration").value(testProjectDashboardViewModel.isHasLabelConfiguration()))
                .andExpect(jsonPath("$.currentPlan").value(testProjectDashboardViewModel.getCurrentPlan()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testProjectDashboardViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);

        verify(projectServiceMock, times(1)).getDashboardData(anyString());
        verifyNoMoreInteractions(projectServiceMock);
    }
}
