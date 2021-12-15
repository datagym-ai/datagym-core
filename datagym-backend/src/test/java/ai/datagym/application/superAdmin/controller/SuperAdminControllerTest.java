package ai.datagym.application.superAdmin.controller;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.superAdmin.service.SuperAdminService;
import ai.datagym.application.testUtils.DatasetUtils;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@ActiveProfiles("test")
class SuperAdminControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private SuperAdminService superAdminServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItSuperAdminController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("superAdminController"));
    }

    @Test
    void getAllProjectFromDb_when2Projects_2Projects() throws Exception {
        List<ProjectViewModel> testProjectViewModels = ProjectUtils.createTestProjectViewModels(2);

        when(superAdminServiceMock.getAllProjectFromDb())
                .thenReturn(testProjectViewModels);

        ProjectViewModel expect = testProjectViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/superadmin/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].description").value(expect.getDescription()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andReturn();

        verify(superAdminServiceMock, times(1)).getAllProjectFromDb();
        verifyNoMoreInteractions(superAdminServiceMock);
    }

    @Test
    void getAllProjectFromDb_whenZeroProjects_emptyCollection() throws Exception {
        when(superAdminServiceMock.getAllProjectFromDb())
                .thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/superadmin/project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(superAdminServiceMock, times(1)).getAllProjectFromDb();
        verifyNoMoreInteractions(superAdminServiceMock);
    }

    @Test
    void getAllDatasetsFromDb_when2Datasets_2Datasets() throws Exception {
        List<DatasetAllViewModel> testListDatasetAllViewModel = DatasetUtils.createTestListDatasetAllViewModel(2);

        when(superAdminServiceMock.getAllDatasetsFromDb()).thenReturn(testListDatasetAllViewModel);

        DatasetAllViewModel expect = testListDatasetAllViewModel.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/superadmin/dataset"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(expect.getShortDescription()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].mediaCount").value(1))
                .andReturn();

        verify(superAdminServiceMock, times(1)).getAllDatasetsFromDb();
        verifyNoMoreInteractions(superAdminServiceMock);
    }

    @Test
    void getAllDatasetsFromDb_whenZeroDatasets_emptyCollection() throws Exception {
        when(superAdminServiceMock.getAllDatasetsFromDb()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/api/superadmin/dataset"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(superAdminServiceMock, times(1)).getAllDatasetsFromDb();
        verifyNoMoreInteractions(superAdminServiceMock);
    }
}