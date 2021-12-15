package ai.datagym.application.projectReviewer.controller;

import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.projectReviewer.service.ProjectReviewerService;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import ai.datagym.application.testUtils.ReviewerUtils;
import ai.datagym.application.testUtils.UserInfoUtils;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import java.util.List;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static ai.datagym.application.testUtils.ReviewerUtils.REVIEWER_ID;
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
class ProjectReviewerControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ProjectReviewerService projectReviewerServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesProjectReviewerController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("projectReviewerController"));
    }

    @Test
    void createReviewer_whenProjectReviewerCreateBindingModelIsValid_createReviewer() throws Exception {
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils
                .createTestProjectReviewerCreateBindingModel();

        ProjectReviewerViewModel reviewerViewModel = ReviewerUtils.createTestProjectReviewerViewModel();

        when(projectReviewerServiceMock.createReviewer(any(ProjectReviewerCreateBindingModel.class)))
                .thenReturn(reviewerViewModel);

        String requestBody = objectMapper.writeValueAsString(reviewerCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/reviewer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userInfo.id").value(reviewerCreateBindingModel.getUserId()))
                .andExpect(jsonPath("$.projectId").value(reviewerCreateBindingModel.getProjectId()))
                .andExpect(jsonPath("$.reviewerId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.timeStamp", Matchers.notNullValue()))
                .andReturn();

        verify(projectReviewerServiceMock, times(1)).createReviewer(any(ProjectReviewerCreateBindingModel.class));
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }

    @Test
    void createReviewer_whenProjectReviewerCreateBindingModelIsNotValid_throwException() throws Exception {
        ProjectReviewerCreateBindingModel reviewerCreateBindingModel = ReviewerUtils
                .createTestProjectReviewerCreateBindingModel();

        reviewerCreateBindingModel.setUserId("");

        ProjectReviewerViewModel reviewerViewModel = ReviewerUtils.createTestProjectReviewerViewModel();

        when(projectReviewerServiceMock.createReviewer(any(ProjectReviewerCreateBindingModel.class)))
                .thenReturn(reviewerViewModel);

        String requestBody = objectMapper.writeValueAsString(reviewerCreateBindingModel);


        Exception resolvedException = mockMvc
                .perform(post("/api/reviewer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void deleteReviewerFromProject_whenReviewerIdIsValid_deleteReviewerFromProject() throws Exception {
        mockMvc
                .perform(delete("/api/reviewer/{reviewerId}", REVIEWER_ID))
                .andDo(print())
                .andExpect(status().isOk());

        verify(projectReviewerServiceMock, times(1)).deleteReviewerFromProject(anyString());
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }

    @Test
    void getAllReviewerForProject_when2ReviewerForCurrentProject_2Reviewer() throws Exception {
        List<ProjectReviewerViewModel> testProjectReviewerViewModels = ReviewerUtils.createTestProjectReviewerViewModels(2);

        when(projectReviewerServiceMock.getAllReviewerForProject(anyString()))
                .thenReturn(testProjectReviewerViewModels);

        ProjectReviewerViewModel expect = testProjectReviewerViewModels.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/reviewer/{reviewerId}", REVIEWER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userInfo.id").value(expect.getUserInfo().getId()))
                .andExpect(jsonPath("$[0].projectId").value(expect.getProjectId()))
                .andExpect(jsonPath("$[0].reviewerId").value(expect.getReviewerId()))
                .andExpect(jsonPath("$[0].timeStamp").value(expect.getTimeStamp()))
                .andReturn();


        String expectedResponseBody = objectMapper.writeValueAsString(testProjectReviewerViewModels);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(projectReviewerServiceMock, times(1)).getAllReviewerForProject(anyString());
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }

    @Test
    void getAllReviewerForProject_whenZeroReviewerForCurrentProject_emptyCollection() throws Exception {
        when(projectReviewerServiceMock.getAllReviewerForProject(anyString()))
                .thenReturn(new ArrayList<>());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/reviewer/{reviewerId}", REVIEWER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(projectReviewerServiceMock, times(1)).getAllReviewerForProject(anyString());
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }

    @Test
    void getAllPossibleReviewerForProject_when5PossibleReviewerForCurrentProject_5UserIds() throws Exception {
        List<UserMinInfoViewModel> possibleReviewers = UserInfoUtils.createUserMinInfoViewModelList(2);

        when(projectReviewerServiceMock.getAllPossibleReviewerForProject(anyString()))
                .thenReturn(possibleReviewers);

        UserMinInfoViewModel expect = possibleReviewers.get(0);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/reviewer/{projectId}/possible", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(possibleReviewers);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(projectReviewerServiceMock, times(1)).getAllPossibleReviewerForProject(anyString());
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }

    @Test
    void getAllPossibleReviewerForProject_whenZeroPossibleReviewerForCurrentProject_emptyCollection() throws Exception {
        List<UserMinInfoViewModel> possibleReviewers =new ArrayList<>();

        when(projectReviewerServiceMock.getAllPossibleReviewerForProject(anyString()))
                .thenReturn(possibleReviewers);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/reviewer/{projectId}/possible", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();


        String expectedResponseBody = objectMapper.writeValueAsString(possibleReviewers);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(projectReviewerServiceMock, times(1)).getAllPossibleReviewerForProject(anyString());
        verifyNoMoreInteractions(projectReviewerServiceMock);
    }
}