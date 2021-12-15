package ai.datagym.application.dummy.controller;

import ai.datagym.application.dummy.service.DummyService;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class DummyControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private DummyService dummyServiceMock;

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
        Assertions.assertNotNull(webApplicationContext.getBean("dummyController"));
    }

    @Test
    void createDummyDataForOrg_whenOrgIdIsValid_200() throws Exception {
        // Given
        String orgId = "dummy_org_id";

        doNothing()
                .when(dummyServiceMock).
                createDummyDataForOrg(anyString());

        mockMvc
                .perform(get("/api/dummy/{orgId}", orgId))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(dummyServiceMock).createDummyDataForOrg(idCapture.capture());
        verify(dummyServiceMock, times(1)).createDummyDataForOrg(anyString());
        assertThat(idCapture.getValue()).isEqualTo(orgId);

        verifyNoMoreInteractions(dummyServiceMock);
    }
}