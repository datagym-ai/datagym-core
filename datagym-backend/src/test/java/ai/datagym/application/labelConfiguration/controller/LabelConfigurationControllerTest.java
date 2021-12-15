package ai.datagym.application.labelConfiguration.controller;

import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.testUtils.LabelConfigurationUtils;
import ai.datagym.application.testUtils.LcEntryUtils;
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
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
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
class LabelConfigurationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LabelConfigurationService labelConfigurationServiceMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesLabelConfigurationController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("labelConfigurationController"));
    }

    @Test
    void getLabelConfiguration_whenProjectIdIsValid_getLabelConfiguration() throws Exception {
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();

        when(labelConfigurationServiceMock.getLabelConfiguration(anyString())).thenReturn(testLabelConfigurationViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/{id}", LC_CONFIG_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelConfigurationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelConfigurationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entries", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelConfigurationViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1)).getLabelConfiguration(anyString());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void getLabelConfiguration_whenLabelConfigIdIsValidAndHas2RootEntries_getLabelConfigurationWith2RootEntries() throws Exception {
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> testLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(2);
        testLabelConfigurationViewModel.setEntries(testLcEntryViewModels);

        when(labelConfigurationServiceMock.getLabelConfiguration(anyString())).thenReturn(testLabelConfigurationViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/{id}", LC_CONFIG_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelConfigurationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelConfigurationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelConfigurationViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1)).getLabelConfiguration(anyString());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void getLabelConfiguration_whenLabelConfigIdIsValidAndHasRootEntryWith2Children_getLabelConfigWithRootEntryWith2Children() throws Exception {
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> rootLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(1);
        List<LcEntryViewModel> childrenLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(2);

        testLabelConfigurationViewModel.setEntries(rootLcEntryViewModels);
        testLabelConfigurationViewModel.getEntries().get(0).setChildren(childrenLcEntryViewModels);

        when(labelConfigurationServiceMock.getLabelConfiguration(anyString())).thenReturn(testLabelConfigurationViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/{id}", LC_CONFIG_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelConfigurationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelConfigurationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].children", hasSize(2)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelConfigurationViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1)).getLabelConfiguration(anyString());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void updateLabelConfiguration_whenLcEntryUpdateBindingModelIsValid_updateLabelConfiguration() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);

        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> rootLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(2);
        List<LcEntryViewModel> childrenLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(3);

        testLabelConfigurationViewModel.setEntries(rootLcEntryViewModels);
        testLabelConfigurationViewModel.getEntries().get(0).setChildren(childrenLcEntryViewModels);

        // When
        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/lconfig/{id}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelConfigurationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelConfigurationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.entries[0].children", hasSize(3)))
                .andReturn();

        verify(labelConfigurationServiceMock, times(1)).updateLabelConfiguration(anyString(), anyList(), anyBoolean());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void updateLabelConfiguration_whenLcEntryKeyIsTooLong_throwException() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);
        LcEntryUpdateBindingModel lcEntryUpdateBindingModel = testLcEntryUpdateBindingModels.get(0);
        lcEntryUpdateBindingModel.setEntryKey("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata ");

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        Exception resolvedException = mockMvc
                .perform(put("/api/lconfig/{id}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void updateLabelConfiguration_whenLcEntryValueIsTooLong_throwException() throws Exception {
        // Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(2);
        LcEntryUpdateBindingModel lcEntryUpdateBindingModel = testLcEntryUpdateBindingModels.get(0);
        lcEntryUpdateBindingModel.setEntryValue("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata ");

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryUpdateBindingModels);

        Exception resolvedException = mockMvc
                .perform(put("/api/lconfig/{id}", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void hasConfigChanged_whenConfigHasNotChanged_ReturnFalse() throws Exception {
        LcConfigHasConfigChangedViewModel testLcConfigHasConfigChangedViewModel = LabelConfigurationUtils
                .createTestLcConfigHasConfigChangedViewModel();

        when(labelConfigurationServiceMock.hasConfigChanged(anyLong(), anyString()))
                .thenReturn(testLcConfigHasConfigChangedViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/hasConfigChanged", PROJECT_ID)
                        .param("lastChangedConfig", "1")
                        .param("iterationId", LC_ITERATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasLabelConfigChanged").value(false))
                .andReturn();


        String expectedResponseBody = objectMapper.writeValueAsString(testLcConfigHasConfigChangedViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1))
                .hasConfigChanged(anyLong(), anyString());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void hasConfigChanged_whenConfigHasChanged_ReturnTrue() throws Exception {
        LcConfigHasConfigChangedViewModel testLcConfigHasConfigChangedViewModel = LabelConfigurationUtils
                .createTestLcConfigHasConfigChangedViewModel();
        testLcConfigHasConfigChangedViewModel.setHasLabelConfigChanged(true);

        when(labelConfigurationServiceMock.hasConfigChanged(anyLong(), anyString()))
                .thenReturn(testLcConfigHasConfigChangedViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/hasConfigChanged", PROJECT_ID)
                        .param("lastChangedConfig", "1")
                        .param("iterationId", LC_ITERATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasLabelConfigChanged").value(true))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLcConfigHasConfigChangedViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1))
                .hasConfigChanged(anyLong(), anyString());
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void getForbiddenKeyWords_when2ForbiddenKeyWords_2Tasks() throws Exception {
        List<String> testForbiddenKeyWordsList = LabelConfigurationUtils.createTestForbiddenKeyWordsList();

        when(labelConfigurationServiceMock.getForbiddenKeyWords())
                .thenReturn(testForbiddenKeyWordsList);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/forbiddenKeywords"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0]").value("geometry"))
                .andExpect(jsonPath("$.[1]").value("geometry_type"))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testForbiddenKeyWordsList);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(labelConfigurationServiceMock, times(1)).getForbiddenKeyWords();
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void getForbiddenKeyWords_whenZeroForbiddenKeyWords_emptyCollection() throws Exception {
        when(labelConfigurationServiceMock.getForbiddenKeyWords())
                .thenReturn(new ArrayList<>());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lconfig/forbiddenKeywords"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();

        verify(labelConfigurationServiceMock, times(1)).getForbiddenKeyWords();
        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }

    @Test
    void clearConfig_whenConfigIdIsValid_clearConfig() throws Exception {
        // Given
        LcConfigDeleteViewModel testLcConfigDeleteViewModel = LabelConfigurationUtils.createTestLcConfigDeleteViewModel();

        // When
        when(labelConfigurationServiceMock.clearConfig(anyString()))
                .thenReturn(testLcConfigDeleteViewModel);

        MvcResult mvcResult = mockMvc.perform(delete("/api/lconfig/{configId}", LC_CONFIG_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.configId").value(testLcConfigDeleteViewModel.getConfigId()))
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(labelConfigurationServiceMock, times(1)).clearConfig(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), LC_CONFIG_ID);

        verifyNoMoreInteractions(labelConfigurationServiceMock);
    }
}