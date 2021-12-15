package ai.datagym.application.aiseg.controller;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.testUtils.AisegUtils;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
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
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AiSegControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiSegService aiSegServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesAiSegController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("aiSegController"));
    }

    @Test
    void prepareImage_whenImageIdIsValid_prepareImage() throws Exception {
        mockMvc
                .perform(post("/api/aiseg/prepare/{imageId}", IMAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(aiSegServiceMock, times(1)).prepare(idCapture.capture(), isNull(), isNull());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);

        verifyNoMoreInteractions(aiSegServiceMock);
    }

    @Test
    void calculateImage_whenAiSegCalculateModelIsValid_calculateImage() throws Exception {
        // Given
        AiSegCalculate testAiSegCalculate = AisegUtils.createTestAiSegCalculate();
        AiSegResponse testAiSegResponse = AisegUtils.createTestAiSegResponse();

        when(aiSegServiceMock.calculate(any(AiSegCalculate.class)))
                .thenReturn(testAiSegResponse);

        // Then
        String requestBody = objectMapper.writeValueAsString(testAiSegCalculate);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/aiseg/calculate")
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

        AssertionsForClassTypes.assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(aiSegServiceMock, times(1)).calculate( any(AiSegCalculate.class));
        verifyNoMoreInteractions(aiSegServiceMock);
    }

    @Test
    void finishImage_whenImageIdIsValid_finishImage() throws Exception {
        mockMvc
                .perform(delete("/api/aiseg/finish/{imageId}", IMAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(aiSegServiceMock, times(1)).finish(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), IMAGE_ID);

        verifyNoMoreInteractions(aiSegServiceMock);
    }
}