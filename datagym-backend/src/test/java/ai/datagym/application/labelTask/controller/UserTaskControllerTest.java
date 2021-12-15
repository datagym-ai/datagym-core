package ai.datagym.application.labelTask.controller;

import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;
import ai.datagym.application.labelTask.service.UserTaskService;
import ai.datagym.application.testUtils.LabelTaskUtils;
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
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UserTaskControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserTaskService userTaskServiceMock;

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
        Assertions.assertNotNull(webApplicationContext.getBean("userTaskController"));
    }

    @Test
    void getUserTasks_whenTwoProjectsWithTasks_getTwoProjectsWithTasks() throws Exception {
        List<UserTaskViewModel> testUserTaskViewModelList = LabelTaskUtils.createTestUserTaskViewModelList(2);

        when(userTaskServiceMock.getUserTasks())
                .thenReturn(testUserTaskViewModelList);

        UserTaskViewModel expected = testUserTaskViewModelList.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/taskList"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].projectId").value(expected.getProjectId()))
                .andExpect(jsonPath("$[0].projectName").value(expected.getProjectName()))
                .andExpect(jsonPath("$[0].countWaitingTasks").value(expected.getCountWaitingTasks()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testUserTaskViewModelList);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(userTaskServiceMock, times(1)).getUserTasks();
        verifyNoMoreInteractions(userTaskServiceMock);
    }

    @Test
    void getUserTasks_whenZeroProjectsWithTasks_getZeroProjectsWithTasks() throws Exception {
        when(userTaskServiceMock.getUserTasks())
                .thenReturn(new ArrayList<>());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/taskList"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(userTaskServiceMock, times(1)).getUserTasks();
        verifyNoMoreInteractions(userTaskServiceMock);
    }

    @Test
    void getNextTask_whenTaskIsFound_getNextTask() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("IN_PROGRESS");

        when(userTaskServiceMock.getNextTask(null)).thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/nextTask"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(userTaskServiceMock, times(1)).getNextTask(null);
        verifyNoMoreInteractions(userTaskServiceMock);
    }

    @Test
    void getNextTaskFromProject_whenIdIsValidTaskIsFound_getNextTaskFromProject() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("IN_PROGRESS");

        when(userTaskServiceMock.getNextTask(anyString())).thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/nextTask/{projectId}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(userTaskServiceMock, times(1)).getNextTask(anyString());
        verifyNoMoreInteractions(userTaskServiceMock);
    }

    @Test
    void getNextTaskFromProject_whenProjectIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(get("/api/user/nextTask/{projectId}", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void getNextReviewTask_whenTaskIsFound_getNextReviewTask() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("COMPLETED");

        when(userTaskServiceMock.getNextReviewTask(anyString())).thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/nextReview/{projectId}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.COMPLETED.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.reviewComment").value(labelTaskViewModel.getReviewComment()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(userTaskServiceMock, times(1)).getNextReviewTask(anyString());
        verifyNoMoreInteractions(userTaskServiceMock);
    }


    @Test
    void getNextReviewTaskFromProject_whenIdIsValidTaskIsFound_getNextReviewTaskFromProject() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("COMPLETED");
        labelTaskViewModel.setProjectId(PROJECT_ID);

        when(userTaskServiceMock.getNextReviewTask(anyString())).thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/user/nextReview/{projectId}", PROJECT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.COMPLETED.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.reviewComment").value(labelTaskViewModel.getReviewComment()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(userTaskServiceMock, times(1)).getNextReviewTask(anyString());
        verifyNoMoreInteractions(userTaskServiceMock);
    }
}