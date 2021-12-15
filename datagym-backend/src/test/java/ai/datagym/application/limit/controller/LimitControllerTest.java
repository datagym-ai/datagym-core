package ai.datagym.application.limit.controller;

import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.testUtils.LimitsUtils;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
class LimitControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LimitService limitServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesLimitController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("limitController"));
    }

    @Test
    void getLimits_whenProjectIdIsValid_getLimits() throws Exception {
        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils
                .createTesLimitPricingPlanViewModel();

        when(limitServiceMock.getLimitsByOrgId(anyString())).thenReturn(tesLimitPricingPlanViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/limit/{orgId}", "eForce21"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tesLimitPricingPlanViewModel.getId()))
                .andExpect(jsonPath("$.organisationId").value(tesLimitPricingPlanViewModel.getOrganisationId()))
                .andExpect(jsonPath("$.pricingPlanType").value(tesLimitPricingPlanViewModel.getPricingPlanType()))
                .andExpect(jsonPath("$.projectLimit").value(tesLimitPricingPlanViewModel.getProjectLimit()))
                .andExpect(jsonPath("$.projectUsed").value(tesLimitPricingPlanViewModel.getProjectUsed()))
                .andExpect(jsonPath("$.labelLimit").value(tesLimitPricingPlanViewModel.getLabelLimit()))
                .andExpect(jsonPath("$.labelRemaining").value(tesLimitPricingPlanViewModel.getLabelRemaining()))
                .andExpect(jsonPath("$.storageLimit").value(tesLimitPricingPlanViewModel.getStorageLimit()))
                .andExpect(jsonPath("$.storageUsed").value(tesLimitPricingPlanViewModel.getStorageUsed()))
                .andExpect(jsonPath("$.aiSegLimit").value(tesLimitPricingPlanViewModel.getAiSegLimit()))
                .andExpect(jsonPath("$.aiSegRemaining").value(tesLimitPricingPlanViewModel.getAiSegRemaining()))
                .andExpect(jsonPath("$.apiAccess").value(tesLimitPricingPlanViewModel.isApiAccess()))
                .andExpect(jsonPath("$.externalStorage").value(tesLimitPricingPlanViewModel.isExternalStorage()))
                .andExpect(jsonPath("$.lastReset").value(tesLimitPricingPlanViewModel.getLastReset()))
                .andReturn();


        String expectedResponseBody = objectMapper.writeValueAsString(tesLimitPricingPlanViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(limitServiceMock, times(1)).getLimitsByOrgId(anyString());
        verifyNoMoreInteractions(limitServiceMock);
    }
}