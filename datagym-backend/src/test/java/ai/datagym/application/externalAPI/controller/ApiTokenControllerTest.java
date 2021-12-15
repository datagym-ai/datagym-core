package ai.datagym.application.externalAPI.controller;

import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;
import ai.datagym.application.externalAPI.service.ApiTokenService;
import ai.datagym.application.testUtils.ApiTokenUtils;
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
import java.util.List;

import static ai.datagym.application.testUtils.ApiTokenUtils.API_TOKEN_ID;
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
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ApiTokenControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ApiTokenService apiTokenServiceMock;

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
        Assertions.assertNotNull(webApplicationContext.getBean("apiTokenController"));
    }

    @Test
    void getApiTokenWhereUserAdmin_when2Tokens_2Tokens() throws Exception {
        // Given
        List<ApiTokenViewModel> testApiTokenViewModels = ApiTokenUtils.createTestApiTokenViewModels(2);

        // When
        when(apiTokenServiceMock
                .getApiTokenWhereUserAdmin()).thenReturn(testApiTokenViewModels);

        ApiTokenViewModel expect = testApiTokenViewModels.get(0);

        mockMvc.perform(get("/api/token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(expect.getId()))
                .andExpect(jsonPath("$[0].name").value(expect.getName()))
                .andExpect(jsonPath("$[0].owner").value(expect.getOwner()))
                .andExpect(jsonPath("$[0].createdAt").value(expect.getCreatedAt()))
                .andExpect(jsonPath("$[0].deleted").value(expect.isDeleted()))
                .andExpect(jsonPath("$[0].deleteTime").value(expect.getDeleteTime()))
                .andExpect(jsonPath("$[0].lastUsed").value(expect.getLastUsed()))
                .andReturn();

        verify(apiTokenServiceMock).getApiTokenWhereUserAdmin();
        verify(apiTokenServiceMock, times(1)).getApiTokenWhereUserAdmin();

        verifyNoMoreInteractions(apiTokenServiceMock);
    }

    @Test
    void getApiTokenWhereUserAdmin_whenZeroTokens_emptyCollection() throws Exception {
        // When
        when(apiTokenServiceMock
                .getApiTokenWhereUserAdmin()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))

                .andReturn();

        verify(apiTokenServiceMock).getApiTokenWhereUserAdmin();
        verify(apiTokenServiceMock, times(1)).getApiTokenWhereUserAdmin();

        verifyNoMoreInteractions(apiTokenServiceMock);
    }

    @Test
    void createApiToken_whenApiTokenCreateBindingModelIsValid_createApiToken() throws Exception {
        ApiTokenCreateBindingModel testApiTokenCreateBindingModel = ApiTokenUtils.createTestApiTokenCreateBindingModel();
        ApiTokenViewModel testApiTokenViewModel = ApiTokenUtils.createTestApiTokenViewModel();

        when(apiTokenServiceMock.createApiToken(any(ApiTokenCreateBindingModel.class)))
                .thenReturn(testApiTokenViewModel);

        when(apiTokenServiceMock.isApiTokenNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testApiTokenCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testApiTokenViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testApiTokenViewModel.getName()))
                .andExpect(jsonPath("$.owner").value(testApiTokenViewModel.getOwner()))
                .andExpect(jsonPath("$.createdAt").value(testApiTokenViewModel.getCreatedAt()))
                .andExpect(jsonPath("$.deleted").value(testApiTokenViewModel.isDeleted()))
                .andExpect(jsonPath("$.deleteTime").value(testApiTokenViewModel.getDeleteTime()))
                .andExpect(jsonPath("$.lastUsed").value(testApiTokenViewModel.getLastUsed()))
                .andReturn();

        verify(apiTokenServiceMock).createApiToken(any(ApiTokenCreateBindingModel.class));
        verify(apiTokenServiceMock, times(1)).createApiToken(any(ApiTokenCreateBindingModel.class));
    }

    @Test
    void createApiToken_whenApiTokenCreateBindingModelIsNotValid_throwException() throws Exception {
        ApiTokenCreateBindingModel testApiTokenCreateBindingModel = ApiTokenUtils.createTestApiTokenCreateBindingModel();
        testApiTokenCreateBindingModel.setName("");
        ApiTokenViewModel testApiTokenViewModel = ApiTokenUtils.createTestApiTokenViewModel();

        when(apiTokenServiceMock.createApiToken(any(ApiTokenCreateBindingModel.class)))
                .thenReturn(testApiTokenViewModel);

        when(apiTokenServiceMock.isApiTokenNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        String requestBody = objectMapper.writeValueAsString(testApiTokenCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void createApiToken_whenApiTokenNameAlreadyExists_throwException() throws Exception {
        ApiTokenCreateBindingModel testApiTokenCreateBindingModel = ApiTokenUtils.createTestApiTokenCreateBindingModel();
        ApiTokenViewModel testApiTokenViewModel = ApiTokenUtils.createTestApiTokenViewModel();

        when(apiTokenServiceMock.createApiToken(any(ApiTokenCreateBindingModel.class)))
                .thenReturn(testApiTokenViewModel);

        when(apiTokenServiceMock.isApiTokenNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(false);

        String requestBody = objectMapper.writeValueAsString(testApiTokenCreateBindingModel);

        Exception resolvedException = mockMvc
                .perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
    }

    @Test
    void deleteApiToken_whenProjectIdIsValid_setIsDeletedToTrue() throws Exception {
        ApiTokenViewModel testApiTokenViewModel = ApiTokenUtils.createTestApiTokenViewModel();

        when(apiTokenServiceMock.deleteApiTokenById(anyString()))
                .thenReturn(testApiTokenViewModel);

        MvcResult mvcResult = mockMvc
                .perform(delete("/api/token/{id}", API_TOKEN_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testApiTokenViewModel.getId()))
                .andExpect(jsonPath("$.name").value(testApiTokenViewModel.getName()))
                .andExpect(jsonPath("$.owner").value(testApiTokenViewModel.getOwner()))
                .andExpect(jsonPath("$.createdAt").value(testApiTokenViewModel.getCreatedAt()))
                .andExpect(jsonPath("$.deleted").value(testApiTokenViewModel.isDeleted()))
                .andExpect(jsonPath("$.deleteTime").value(testApiTokenViewModel.getDeleteTime()))
                .andExpect(jsonPath("$.lastUsed").value(testApiTokenViewModel.getLastUsed()))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testApiTokenViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(apiTokenServiceMock).deleteApiTokenById(anyString());
        verify(apiTokenServiceMock, times(1)).deleteApiTokenById(anyString());
        verifyNoMoreInteractions(apiTokenServiceMock);
    }
}