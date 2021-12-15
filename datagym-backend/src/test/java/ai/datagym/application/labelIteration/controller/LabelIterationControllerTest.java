package ai.datagym.application.labelIteration.controller;

import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.testUtils.LabelIterationUtils;
import ai.datagym.application.testUtils.LcEntryValueUtils;
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
import java.util.List;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;
import static ai.datagym.application.testUtils.LcEntryValueUtils.LC_ENTRY_VALUE_ID;
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
class LabelIterationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LcEntryValueService lcEntryValueServiceMock;

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
        Assertions.assertNotNull(webApplicationContext.getBean("labelIterationController"));
    }

    @Test
    void getLabelIterationValues_whenInputIsValidIsValid_updateLabelValues() throws Exception {
        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils.createTestLabelIterationViewModel();

        when(lcEntryValueServiceMock.getLabelIterationValues(anyString(), anyString(), anyString()))
                .thenReturn(testLabelIterationViewModel);

        MvcResult mvcResult = mockMvc
                .perform(get("/api/lcvalues/{iterationId}/{imageId}/{taskId}", LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelIterationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelIterationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entryValues", hasSize(0)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelIterationViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(lcEntryValueServiceMock, times(1)).getLabelIterationValues(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void updateLabelValues_whenLcEntryValueUpdateBindingModelIsValid_updateLabelValues() throws Exception {
        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(2);

        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils.createTestLabelIterationViewModel();
        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(2);

        testLabelIterationViewModel.setEntryValues(testLcEntryViewModels);

        when(lcEntryValueServiceMock.updateLcEntryValues(anyString(), anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelIterationViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(testLcEntryValueUpdateBindingModels);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/lcvalues/{iterationId}/{imageId}/{taskId}", LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelIterationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelIterationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entryValues", hasSize(2)))
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(testLabelIterationViewModel);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualToIgnoringWhitespace(actualResponseBody);

        verify(lcEntryValueServiceMock, times(1)).updateLcEntryValues(anyString(), anyString(), anyString(), anyList(), anyBoolean());
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void updateSingleLabelValue_whenLcEntryValueUpdateBindingModelIsValid_updateSingleLabelValue() throws Exception {
        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(2);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueUpdateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/lcvalues/{lcValueId}", LC_ENTRY_VALUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).updateSingleLcEntryValue(anyString(), any(LcEntryValueUpdateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void createLabelValue_whenLcEntryValueCreateBindingModelIsValid_createLabelValue() throws Exception {
        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.createLcEntryValue(any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/lcvalues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).createLcEntryValue(any(LcEntryValueCreateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void createLabelValuesTree_whenLcEntryValueCreateBindingModelIsValid_createLabelValue() throws Exception {
        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/lcvalues/{lcEntryId}", LC_ENTRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).createLcEntryValueTree(anyString(), any(LcEntryValueCreateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void createGlobalClassificationsValuesTree_whenLcEntryValueCreateBindingModelIsValid_createGlobalClassificationsValuesTree() throws Exception {
        // Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils.createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(2);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.createGlobalClassificationsValuesGetRoots(anyString(), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(testLcEntryViewModels);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueCreateBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/lcvalues/{configId}/{taskId}/classification", LC_CONFIG_ID, LABEL_TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$[0].labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$[0].mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$[0].lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$[0].labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$[0].configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$[0].entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$[0].entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$[0].entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).createGlobalClassificationsValuesGetRoots(anyString(), any(LcEntryValueCreateBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void deleteLabelValue_whenLcValueIdIsValid_200OK() throws Exception {
        // Given
        mockMvc.perform(delete("/api/lcvalues/{lcValueId}", LC_ENTRY_VALUE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(lcEntryValueServiceMock, times(1)).deleteLcValue(idCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), LC_ENTRY_VALUE_ID);

        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void extendValuesTree_whenLcEntryValueExtendBindingModelIsValid_200OK() throws Exception {
        LcEntryValueExtendBindingModel lcEntryValueExtendBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendBindingModel(LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_ID, null);

        String requestBody = objectMapper.writeValueAsString(lcEntryValueExtendBindingModel);

        mockMvc.perform(post("/api/lcvalues/{lcEntryId}/extend", LC_ENTRY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LcEntryValueExtendBindingModel> bindingModelCapture = ArgumentCaptor.forClass(LcEntryValueExtendBindingModel.class);
        verify(lcEntryValueServiceMock, times(1)).extendValueTree(idCapture.capture(), bindingModelCapture.capture());
        Assertions.assertEquals(idCapture.getValue(), LC_ENTRY_ID);
        Assertions.assertEquals(bindingModelCapture.getValue().getMediaId(), IMAGE_ID);
        Assertions.assertEquals(bindingModelCapture.getValue().getIterationId(), LC_ITERATION_ID);
//        Assertions.assertEquals();(bindingModelCapture.getValue().getLabeler(),"labeler");
        Assertions.assertEquals(bindingModelCapture.getValue().getLcEntryParentId(), null);

        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void extendAllConfigEntryValues_whenLcEntryValueExtendAllBindingModelIsValid_extendAllConfigEntryValues() throws Exception {
        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils.createTestLabelIterationViewModel();
        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(2);

        testLabelIterationViewModel.setEntryValues(testLcEntryViewModels);

        when(lcEntryValueServiceMock.updateLcEntryValues(anyString(), anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelIterationViewModel);

        when(lcEntryValueServiceMock.extendAllConfigEntryValues(anyString(), any(LcEntryValueExtendAllBindingModel.class)))
                .thenReturn(testLabelIterationViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(lcEntryValueExtendBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/lcvalues/{configId}/extendAll", LC_CONFIG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLabelIterationViewModel.getId()))
                .andExpect(jsonPath("$.projectId").value(testLabelIterationViewModel.getProjectId()))
                .andExpect(jsonPath("$.entryValues", hasSize(2)))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).extendAllConfigEntryValues(
                anyString(),
                any(LcEntryValueExtendAllBindingModel.class)
        );
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }

    @Test
    void changeTypeOfSingleLabelValue_whenLcEntryValueUpdateBindingModelIsValid_changeTypeOfSingleLabelValue() throws Exception {
        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        List<LcEntryValueViewModel> testLcEntryViewModels = LcEntryValueUtils.createLcEntryValueViewModelList(1);
        LcEntryValueViewModel lcEntryValueViewModel = testLcEntryViewModels.get(0);

        when(lcEntryValueServiceMock.changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class)))
                .thenReturn(lcEntryValueViewModel);

        // Then
        String requestBody = objectMapper.writeValueAsString(changeValueClassBindingModel);

        MvcResult mvcResult = mockMvc
                .perform(put("/api/lcvalues/{lcValueId}/changeType", LC_ENTRY_VALUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(lcEntryValueViewModel.getId()))
                .andExpect(jsonPath("$.labelIterationId").value(lcEntryValueViewModel.getLabelIterationId()))
                .andExpect(jsonPath("$.mediaId").value(lcEntryValueViewModel.getMediaId()))
                .andExpect(jsonPath("$.lcEntryId").value(lcEntryValueViewModel.getLcEntryId()))
                .andExpect(jsonPath("$.labeler").value(lcEntryValueViewModel.getLabeler()))
                .andExpect(jsonPath("$.configurationId").value(lcEntryValueViewModel.getConfigurationId()))
                .andExpect(jsonPath("$.entryTypeLcEntry").value(lcEntryValueViewModel.getEntryTypeLcEntry()))
                .andExpect(jsonPath("$.entryKeyLcEntry").value(lcEntryValueViewModel.getEntryKeyLcEntry()))
                .andExpect(jsonPath("$.entryValueLcEntry").value(lcEntryValueViewModel.getEntryValueLcEntry()))
                .andReturn();

        verify(lcEntryValueServiceMock, times(1)).changeTypeOfSingleLabelValue(anyString(), any(LcEntryValueChangeValueClassBindingModel.class));
        verifyNoMoreInteractions(lcEntryValueServiceMock);
    }
}