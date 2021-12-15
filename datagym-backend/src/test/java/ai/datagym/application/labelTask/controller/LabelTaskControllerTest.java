package ai.datagym.application.labelTask.controller;

import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskMoveAllBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskReviewBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.testUtils.LabelTaskUtils;
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
import javax.validation.ConstraintViolationException;

import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class LabelTaskControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LabelTaskService labelTaskServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesLabelTaskController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("labelTaskController"));
    }

    @Test
    void getTask_whenTaskIdIsValid_getTask() throws Exception {
        LabelModeDataViewModel testLabelModeDataViewModel = LabelTaskUtils.createTestLabelModeDataViewModel();

        when(labelTaskServiceMock.getTask(anyString())).thenReturn(testLabelModeDataViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/task/{id}", LABEL_TASK_ID))
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

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelTaskServiceMock, times(1)).getTask(anyString());
        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void getTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(get("/api/task/{id}", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void moveTaskToWaiting_whenTaskIdIsValid_moveTaskToWaiting() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("WAITING");

        when(labelTaskServiceMock
                .moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.WAITING))
                .thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/toWaiting", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.WAITING.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelTaskServiceMock, times(1)).moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.WAITING);
        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void moveTaskToWaiting_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/toWaiting", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void moveTaskToBacklog_whenTaskIdIsValid_moveTaskToWaiting() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("BACKLOG");

        when(labelTaskServiceMock
                .moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.BACKLOG))
                .thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/toBacklog", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.BACKLOG.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelTaskServiceMock, times(1)).moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.BACKLOG);
        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void moveTaskToBacklog_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/toBacklog", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void skipTask_whenTaskIdIsValid_skipTask() throws Exception {

        doNothing()
                .when(labelTaskServiceMock)
                .skipTask(LABEL_TASK_ID);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/skipTask", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock, times(1)).skipTask(idCapture.capture());

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void skipTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/skipTask", " "))
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

        when(labelTaskServiceMock.completeTask(anyString(), any(LabelTaskCompleteBindingModel.class)))
                .thenReturn(testLabelTaskCompleteViewModel);

        String requestBody = objectMapper.writeValueAsString(testLabelTaskCompleteBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/completeTask", LABEL_TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasLabelConfigChanged").value(testLabelTaskCompleteViewModel.isHasLabelConfigChanged()))
                .andExpect(jsonPath("$.currentTaskId").value(testLabelTaskCompleteViewModel.getCurrentTaskId()))
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock, times(1)).completeTask(idCapture.capture(), any(LabelTaskCompleteBindingModel.class));

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void completeTask_whenTaskIdIsEmptyString_throwException() throws Exception {
        LabelTaskCompleteBindingModel testLabelTaskCompleteBindingModel = LabelTaskUtils.createTestLabelTaskCompleteBindingModel();

        String requestBody = objectMapper.writeValueAsString(testLabelTaskCompleteBindingModel);

        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/completeTask", " ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void moveAllTasks_whenProjectIdIsNull_throwException() throws Exception {
        LabelTaskMoveAllBindingModel testLabelTaskMoveAllBindingModel = LabelTaskUtils.createTestLabelTaskMoveAllBindingModel();
        testLabelTaskMoveAllBindingModel.setProjectId(null);

        String requestBody = objectMapper.writeValueAsString(testLabelTaskMoveAllBindingModel);

        Exception resolvedException = mockMvc
                .perform(put("/api/task/moveAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();


        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }


    @Test
    void reviewedSuccess_whenLabelTaskReviewBindingModelIsValid_returns200() throws Exception {
        // Given
        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        String requestBody = objectMapper.writeValueAsString(testLabelTaskReviewBindingModel);

        // When
        doNothing()
                .when(labelTaskServiceMock)
                .reviewCompletion(any(LabelTaskReviewBindingModel.class), anyBoolean());

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/reviewedSuccess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<LabelTaskReviewBindingModel> labelTaskReviewBindingModelCapture = ArgumentCaptor.forClass(LabelTaskReviewBindingModel.class);
        ArgumentCaptor<Boolean> successCapture = ArgumentCaptor.forClass(Boolean.class);

        verify(labelTaskServiceMock, times(1))
                .reviewCompletion(labelTaskReviewBindingModelCapture.capture(), successCapture.capture());

        Assertions.assertEquals(labelTaskReviewBindingModelCapture.getValue().getTaskId(), testLabelTaskReviewBindingModel.getTaskId());
        Assertions.assertEquals(labelTaskReviewBindingModelCapture.getValue().getReviewComment(), testLabelTaskReviewBindingModel.getReviewComment());
        Assertions.assertTrue(successCapture.getValue());

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void reviewedFailed_whenLabelTaskReviewBindingModelIsValid_returns200() throws Exception {
        // Given
        LabelTaskReviewBindingModel testLabelTaskReviewBindingModel = LabelTaskUtils
                .createTestLabelTaskReviewBindingModel();

        String requestBody = objectMapper.writeValueAsString(testLabelTaskReviewBindingModel);

        // When
        doNothing()
                .when(labelTaskServiceMock)
                .reviewCompletion(any(LabelTaskReviewBindingModel.class), anyBoolean());

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/reviewedFailed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<LabelTaskReviewBindingModel> labelTaskReviewBindingModelCapture = ArgumentCaptor.forClass(LabelTaskReviewBindingModel.class);
        ArgumentCaptor<Boolean> successCapture = ArgumentCaptor.forClass(Boolean.class);

        verify(labelTaskServiceMock, times(1))
                .reviewCompletion(labelTaskReviewBindingModelCapture.capture(), successCapture.capture());

        Assertions.assertEquals(labelTaskReviewBindingModelCapture.getValue().getTaskId(), testLabelTaskReviewBindingModel.getTaskId());
        Assertions.assertEquals(labelTaskReviewBindingModelCapture.getValue().getReviewComment(), testLabelTaskReviewBindingModel.getReviewComment());
        Assertions.assertFalse(successCapture.getValue());

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void moveTaskFromSkippedToWaitingChanged_whenTaskIdIsValid_moveTaskToWaiting() throws Exception {
        LabelTaskViewModel labelTaskViewModel = LabelTaskUtils.createTestLabelTaskViewModel("labeler");
        labelTaskViewModel.setLabelTaskState("WAITING_CHANGED");


        when(labelTaskServiceMock
                .moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.WAITING_CHANGED))
                .thenReturn(labelTaskViewModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/skipToWC", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskId").value(labelTaskViewModel.getTaskId()))
                .andExpect(jsonPath("$.projectId").value(labelTaskViewModel.getProjectId()))
                .andExpect(jsonPath("$.projectName").value(labelTaskViewModel.getProjectName()))
                .andExpect(jsonPath("$.labelTaskState").value(LabelTaskState.WAITING_CHANGED.name()))
                .andExpect(jsonPath("$.mediaId").value(labelTaskViewModel.getMediaId()))
                .andExpect(jsonPath("$.mediaName").value(labelTaskViewModel.getMediaName()))
                .andExpect(jsonPath("$.labeler").value(labelTaskViewModel.getLabeler()))
                .andExpect(jsonPath("$.iterationId").value(labelTaskViewModel.getIterationId()))
                .andExpect(jsonPath("$.iterationRun").value(labelTaskViewModel.getIterationRun()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(labelTaskViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelTaskServiceMock, times(1)).moveTaskStateIfUserIsAdmin(LABEL_TASK_ID, LabelTaskState.WAITING_CHANGED);
        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void moveTaskFromSkippedToWaitingChanged_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/skipToWC", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void activateBenchmark_whenTaskIdIsValid_activateBenchmark() throws Exception {
        doNothing()
                .when(labelTaskServiceMock)
                .activateBenchmark(LABEL_TASK_ID);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/activateBenchmark", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock, times(1)).activateBenchmark(idCapture.capture());

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void activateBenchmark_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/activateBenchmark", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void deactivateBenchmark_whenTaskIdIsValid_deactivateBenchmark() throws Exception {
        doNothing()
                .when(labelTaskServiceMock)
                .activateBenchmark(LABEL_TASK_ID);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/task/{id}/deactivateBenchmark", LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelTaskServiceMock, times(1)).deactivateBenchmark(idCapture.capture());

        Assertions.assertEquals(idCapture.getValue(), LABEL_TASK_ID);

        verifyNoMoreInteractions(labelTaskServiceMock);
    }

    @Test
    void deactivateBenchmark_whenTaskIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc
                .perform(put("/api/task/{id}/deactivateBenchmark", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

}