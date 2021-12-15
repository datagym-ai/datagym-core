package ai.datagym.application.lablerRating.controller;

import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.service.LabelerRatingService;
import ai.datagym.application.testUtils.LabelRatingUtils;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LabelerRatingControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LabelerRatingService labelerRatingServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesLabelerRatingController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("labelerRatingController"));
    }

    @Test
    void addToPositive_whenLabelerRatingUpdateBindingModelIsValid_addToPositive() throws Exception {
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        // When
        doNothing().when(labelerRatingServiceMock).addToPositive(ratingUpdateBindingModel);

        String requestBody = objectMapper.writeValueAsString(ratingUpdateBindingModel);

        mockMvc.perform(put("/api/rating/positive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<LabelerRatingUpdateBindingModel> labelRatingCapture = ArgumentCaptor.forClass(LabelerRatingUpdateBindingModel.class);
        verify(labelerRatingServiceMock, times(1)).addToPositive(labelRatingCapture.capture());
        Assertions.assertEquals(labelRatingCapture.getValue().getLabelerId(), ratingUpdateBindingModel.getLabelerId());
        Assertions.assertEquals(labelRatingCapture.getValue().getProjectId(), ratingUpdateBindingModel.getProjectId());
        Assertions.assertEquals(labelRatingCapture.getValue().getMediaId(), ratingUpdateBindingModel.getMediaId());

        verifyNoMoreInteractions(labelerRatingServiceMock);
    }

    @Test
    void addToPositive_whenLabelerRatingUpdateBindingModelNotIsValid_throwException() throws Exception {
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        ratingUpdateBindingModel.setProjectId(null);

        // When
        doNothing().when(labelerRatingServiceMock).addToPositive(ratingUpdateBindingModel);

        String requestBody = objectMapper.writeValueAsString(ratingUpdateBindingModel);

        Exception resolvedException = mockMvc.perform(put("/api/rating/positive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void addToNegative_whenLabelerRatingUpdateBindingModelIsValid_addToNegative() throws Exception {
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        // When
        doNothing().when(labelerRatingServiceMock).addToNegative(ratingUpdateBindingModel);

        String requestBody = objectMapper.writeValueAsString(ratingUpdateBindingModel);

        mockMvc.perform(put("/api/rating/negative")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        // Then
        ArgumentCaptor<LabelerRatingUpdateBindingModel> labelRatingCapture = ArgumentCaptor.forClass(LabelerRatingUpdateBindingModel.class);
        verify(labelerRatingServiceMock, times(1)).addToNegative(labelRatingCapture.capture());
        Assertions.assertEquals(labelRatingCapture.getValue().getLabelerId(), ratingUpdateBindingModel.getLabelerId());
        Assertions.assertEquals(labelRatingCapture.getValue().getProjectId(), ratingUpdateBindingModel.getProjectId());
        Assertions.assertEquals(labelRatingCapture.getValue().getMediaId(), ratingUpdateBindingModel.getMediaId());

        verifyNoMoreInteractions(labelerRatingServiceMock);
    }

    @Test
    void addToNegative_whenLabelerRatingUpdateBindingModelNotIsValid_throwException() throws Exception {
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        ratingUpdateBindingModel.setProjectId(null);

        // When
        doNothing().when(labelerRatingServiceMock).addToNegative(ratingUpdateBindingModel);

        String requestBody = objectMapper.writeValueAsString(ratingUpdateBindingModel);

        Exception resolvedException = mockMvc.perform(put("/api/rating/negative")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }
}