package ai.datagym.application.labelConfiguration.service;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryLine;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPoint;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPolygon;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryRectangle;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.export.LcEntryExport;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValidation;
import ai.datagym.application.labelTask.service.UserTaskService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LabelConfigurationServiceImplTest {

    private LabelConfigurationService labelConfigService;
    private LcEntryValidation lcEntryValidation;
    private ModelMapper mapper;

    @Mock
    private LabelConfigurationRepository labelConfigRepositoryMock;

    @Mock
    private UserTaskService userTaskService;

    @Mock
    private LcEntryRepository lcEntryRepositoryMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepository;

    @Mock
    private LabelIterationRepository labelIterationRepositoryMock;

    @BeforeEach
    void setUp() {
        mapper = new ModelMapper();
        lcEntryValidation = new LcEntryValidation(lcEntryValueRepository);
        labelConfigService = new LabelConfigurationServiceImpl(
                labelConfigRepositoryMock,
                lcEntryRepositoryMock,
                userTaskService,
                mapper,
                lcEntryValueRepository,
                labelIterationRepositoryMock,
                lcEntryValidation);
    }

    @Test
    void createLabelConfiguration_whenInputIsValid_createLabelConfiguration() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        labelConfigService.createLabelConfiguration();

        verify(labelConfigRepositoryMock, times(1)).save(any(LabelConfiguration.class));
        verifyNoMoreInteractions(labelConfigRepositoryMock);
    }

    @Test
    void createLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.createLabelConfiguration()
        );
    }

    @Test
    void getLabelConfiguration_whenIdIsValid_getLabelConfiguration() throws JsonProcessingException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntry> testLcEntryLineList = LcEntryUtils.createTestLcEntryGeometryList(3, LcEntryType.LINE, testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(anyString()))
                .thenReturn(testLcEntryLineList);

        LabelConfigurationViewModel labelConfigurationViewModel = labelConfigService.getLabelConfiguration(LC_CONFIG_ID);

        //Then
        LcEntry expectedLcEntry = testLcEntryLineList.get(0);
        LcEntryViewModel actualLcEntry = labelConfigurationViewModel.getEntries().get(0);

        assertNotNull(labelConfigurationViewModel);
        assertEquals(testLabelConfiguration.getId(), labelConfigurationViewModel.getId());
        assertEquals(testLabelConfiguration.getProject().getId(), labelConfigurationViewModel.getProjectId());
        assertEquals(3, labelConfigurationViewModel.getEntries().size());

        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryValue(), actualLcEntry.getEntryValue());
        assertEquals(expectedLcEntry.getType(), actualLcEntry.getType());

        verify(labelConfigRepositoryMock).findById(anyString());
        verify(labelConfigRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);

        verify(lcEntryRepositoryMock).findAllByParentEntryIsNullAndConfigurationId(anyString());
        verify(lcEntryRepositoryMock, times(1)).findAllByParentEntryIsNullAndConfigurationId(anyString());
        verifyNoMoreInteractions(lcEntryRepositoryMock);
    }

    @Test
    void getLabelConfiguration_whenIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> labelConfigService.getLabelConfiguration("invalid_config_id")
        );
    }

    @Test
    void getLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.getLabelConfiguration(LC_CONFIG_ID)
        );
    }

    @Test
    void getLabelConfiguration_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.getProject().setOwner("test_org");

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.getLabelConfiguration(LC_CONFIG_ID)
        );
    }

    @Test
    void getLabelConfiguration_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.getLabelConfiguration(LC_CONFIG_ID)
        );
    }

    @Test
    void updateLabelConfiguration_whenUpdateBindingModelIsValidAndContainsOnlyRootEntries_updateLabelConfiguration() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = LcEntryUtils.createTestLcEntryUpdateBindingModels(3);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntry> testLcEntryLineList = LcEntryUtils.createTestLcEntryGeometryList(3, LcEntryType.LINE, testLabelConfiguration);

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLcEntryLine));
        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(anyString()))
                .thenReturn(testLcEntryLineList);

        LabelConfigurationViewModel labelConfigurationViewModel = labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, testLcEntryUpdateBindingModels, true);

        //Then
        LcEntry expectedLcEntry = testLcEntryLineList.get(0);
        LcEntryViewModel actualLcEntry = labelConfigurationViewModel.getEntries().get(0);

        assertNotNull(labelConfigurationViewModel);
        assertEquals(testLabelConfiguration.getId(), labelConfigurationViewModel.getId());
        assertEquals(testLabelConfiguration.getProject().getId(), labelConfigurationViewModel.getProjectId());
        assertEquals(3, labelConfigurationViewModel.getEntries().size());

        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryValue(), actualLcEntry.getEntryValue());
        assertEquals(expectedLcEntry.getType(), actualLcEntry.getType());

        verify(labelConfigRepositoryMock, times(2)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);
        verify(lcEntryRepositoryMock, times(2)).findAllByParentEntryIsNullAndConfigurationId(anyString());
    }

    @Test
    void updateLabelConfiguration_whenUpdateBindingModelIsValidAndContainsNestedEntries_updateLabelConfiguration() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = createUpdateBindingModelList();

        List<LcEntry> lcEntryRootElementWithChildren = createLcEntryRootElementWithChildren();
        LcEntryPoint expectedLcEntry = (LcEntryPoint) lcEntryRootElementWithChildren.get(0);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(expectedLcEntry));
        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(anyString()))
                .thenReturn(new ArrayList<>() {{
                    add(expectedLcEntry);
                }});

        LabelConfigurationViewModel labelConfigurationViewModel = labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true);

        //Then
        LcEntryViewModel actualLcEntry = labelConfigurationViewModel.getEntries().get(0);

        assertNotNull(labelConfigurationViewModel);
        assertEquals(testLabelConfiguration.getId(), labelConfigurationViewModel.getId());
        assertEquals(testLabelConfiguration.getProject().getId(), labelConfigurationViewModel.getProjectId());
        assertEquals(1, labelConfigurationViewModel.getEntries().size());

        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryValue(), actualLcEntry.getEntryValue());
        assertEquals(expectedLcEntry.getColor(), actualLcEntry.getColor());
        assertEquals(expectedLcEntry.getShortcut(), actualLcEntry.getShortcut());
        assertEquals(expectedLcEntry.getChildren().size(), actualLcEntry.getChildren().size());

        verify(labelConfigRepositoryMock, times(2)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);
    }

    @Test
    void updateLabelConfiguration_whenNewEntriesAreAddedToTheConfiguration_updateLabelConfiguration() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = new ArrayList<>();

        LcEntryUpdateBindingModel point = createUpdateBindingModelEntries("point");
        LcEntryUpdateBindingModel select = createUpdateBindingModelEntries("select");

        point.getChildren().add(select);
        select.setParentEntry(point);
        select.setId(null);

        updateBindingModelList.add(point);

        List<LcEntry> lcEntryRootElementWithChildren = createLcEntryRootElementWithChildren();
        LcEntryPoint expectedLcEntry = (LcEntryPoint) lcEntryRootElementWithChildren.get(0);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(expectedLcEntry));
        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(anyString()))
                .thenReturn(new ArrayList<>() {{
                    add(expectedLcEntry);
                }});

        LabelConfigurationViewModel labelConfigurationViewModel = labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true);

        //Then
        LcEntryViewModel actualLcEntry = labelConfigurationViewModel.getEntries().get(0);

        assertNotNull(labelConfigurationViewModel);
        assertEquals(testLabelConfiguration.getId(), labelConfigurationViewModel.getId());
        assertEquals(testLabelConfiguration.getProject().getId(), labelConfigurationViewModel.getProjectId());
        assertEquals(1, labelConfigurationViewModel.getEntries().size());

        assertEquals(expectedLcEntry.getEntryKey(), actualLcEntry.getEntryKey());
        assertEquals(expectedLcEntry.getEntryValue(), actualLcEntry.getEntryValue());
        assertEquals(expectedLcEntry.getColor(), actualLcEntry.getColor());
        assertEquals(expectedLcEntry.getShortcut(), actualLcEntry.getShortcut());
        assertEquals(expectedLcEntry.getChildren().size(), actualLcEntry.getChildren().size());

        verify(labelConfigRepositoryMock, times(2)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);
    }

    @Test
    void updateLabelConfiguration_whenEntryTypeFromUserIsDifferentFromEntryTypeInDatabase_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = new ArrayList<>();

        LcEntryUpdateBindingModel point = createUpdateBindingModelEntries("point");
        LcEntryUpdateBindingModel line = createUpdateBindingModelEntries("line");

        point.getChildren().add(line);
        line.setParentEntry(point);
        line.setId(null);

        point.setType("rectangle");

        updateBindingModelList.add(point);

        List<LcEntry> lcEntryRootElementWithChildren = createLcEntryRootElementWithChildren();
        LcEntryPoint expectedLcEntry = (LcEntryPoint) lcEntryRootElementWithChildren.get(0);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(expectedLcEntry));

        //Then
        assertThrows(GenericException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true)
        );
    }

    @Test
    void updateLabelConfiguration_when2EntriesHaveIdenticalEntryKey_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryUpdateBindingModel> updateBindingModelList = createUpdateBindingModelList();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);


        LcEntryUpdateBindingModel firstRootElement = updateBindingModelList.get(0);
        String entryKey = firstRootElement.getEntryKey();
        firstRootElement.getChildren().get(0).setEntryKey(entryKey);
        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));

        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLcEntryPoint));

        //Then
        assertThrows(AlreadyExistsException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenEntryTypeIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = createUpdateBindingModelList();
        updateBindingModelList.get(0).setType("invalid type");

        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLcEntryPoint));

        //Then
        assertThrows(GenericException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenNestingOfEntriesIsDeeperAsAllowed_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = new ArrayList<>();

        LcEntryUpdateBindingModel select1 = createUpdateBindingModelEntries("select");
        select1.setId(LC_ENTRY_ID + 5);
        select1.setEntryKey("select1");

        LcEntryUpdateBindingModel select2 = createUpdateBindingModelEntries("select");
        select2.setId(LC_ENTRY_ID + 6);
        select2.setEntryKey("select2");

        LcEntryUpdateBindingModel select3 = createUpdateBindingModelEntries("select");
        select3.setId(LC_ENTRY_ID + 7);
        select3.setEntryKey("select3");

        LcEntryUpdateBindingModel select4 = createUpdateBindingModelEntries("select");
        select4.setId(LC_ENTRY_ID + 8);
        select4.setEntryKey("select4");

        select1.getChildren().add(select2);
        select2.setParentEntry(select1);

        select2.getChildren().add(select3);
        select3.setParentEntry(select2);

        select3.getChildren().add(select4);
        select4.setParentEntry(select3);

        LcEntryUpdateBindingModel point = createUpdateBindingModelEntries("point");
        point.setId(LC_ENTRY_ID + 10);
        point.setEntryKey("test");
        point.setId("test_id");

        select4.getChildren().add(point);
        point.setParentEntry(select4);

        updateBindingModelList.add(select1);

        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLcEntryPoint));

        //Then
        assertThrows(GenericException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenParentEntryHasNotIdAndChildEntryHasId_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntryUpdateBindingModel> updateBindingModelList = createUpdateBindingModelList();
        updateBindingModelList.get(0).setId(null);
        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));
        when(lcEntryRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLcEntryPoint));
        //Then
        assertThrows(GenericException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, updateBindingModelList, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, null, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenIdIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.getProject().setOwner("test_org");

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, null, true)
        );
    }

    @Test
    void updateLabelConfiguration_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        //When
        when(labelConfigRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.updateLabelConfiguration(LC_CONFIG_ID, null, true)
        );
    }

    @Test
    void hasConfigChanged_whenInputIsValidAndConfigHasChanged_returnsTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(2L);

        testProject.setLabelConfiguration(testLabelConfiguration);

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        LcConfigHasConfigChangedViewModel lcConfigHasConfigChangedViewModel = labelConfigService
                .hasConfigChanged(1L, LC_ITERATION_ID);

        //Then
        assertNotNull(lcConfigHasConfigChangedViewModel);
        assertTrue(lcConfigHasConfigChangedViewModel.isHasLabelConfigChanged());

        verify(labelIterationRepositoryMock).findById(anyString());
        verify(labelIterationRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelIterationRepositoryMock);
    }

    @Test
    void hasConfigChanged_whenInputIsValidAndConfigHasNotChanged_returnsFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(1L);

        testProject.setLabelConfiguration(testLabelConfiguration);

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        LcConfigHasConfigChangedViewModel lcConfigHasConfigChangedViewModel = labelConfigService
                .hasConfigChanged(1L, LC_ITERATION_ID);

        //Then
        assertNotNull(lcConfigHasConfigChangedViewModel);
        assertFalse(lcConfigHasConfigChangedViewModel.isHasLabelConfigChanged());

        verify(labelIterationRepositoryMock).findById(anyString());
        verify(labelIterationRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelIterationRepositoryMock);
    }

    @Test
    void hasConfigChanged_whenIterationIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> labelConfigService.hasConfigChanged(1L, LC_ITERATION_ID)
        );
    }

    @Test
    void hasConfigChanged_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.hasConfigChanged(1L, LC_ITERATION_ID)
        );
    }

    @Test
    void hasConfigChanged_whenUserIsNotInOrg_throwException() {
// Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(1L);

        testProject.setLabelConfiguration(testLabelConfiguration);
        testProject.setOwner("test_org");

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.hasConfigChanged(1L, LC_ITERATION_ID)
        );
    }

    @Test
    void hasConfigChanged_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(1L);

        testProject.setLabelConfiguration(testLabelConfiguration);

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.hasConfigChanged(1L, LC_ITERATION_ID)
        );
    }

    @Test
    void clearConfig_whenConfigIdIsValid_clearConfig() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        List<LcEntry> testLcEntryLineList = LcEntryUtils.createTestLcEntryGeometryList(3, LcEntryType.LINE, testLabelConfiguration);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(anyString()))
                .thenReturn(testLcEntryLineList);

        LcConfigDeleteViewModel lcConfigDeleteViewModel = labelConfigService.clearConfig(LC_CONFIG_ID);

        System.out.println();

        //Then
        assertNotNull(lcConfigDeleteViewModel);
        assertEquals(LC_CONFIG_ID, lcConfigDeleteViewModel.getConfigId());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(lcEntryRepositoryMock, times(1)).deleteLcEntriesByConfigurationId(idCapture.capture());
        assertThat(idCapture.getValue()).isEqualTo(LC_CONFIG_ID);
        verifyNoMoreInteractions(lcEntryRepositoryMock);
    }

    @Test
    void clearConfig_whenConfigIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> labelConfigService.clearConfig("invalid_config_id")
        );
    }

    @Test
    void clearConfig_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.clearConfig(LC_CONFIG_ID)
        );
    }

    @Test
    void clearConfig_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.getProject().setOwner("test_org");

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.clearConfig(LC_CONFIG_ID)
        );
    }

    @Test
    void clearConfig_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.clearConfig(LC_CONFIG_ID)
        );
    }

    @Test
    void exportLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        final String configId = "irrelevant";
        final HttpServletResponse res = mock(HttpServletResponse.class);

        Assertions.assertThrows(ForbiddenException.class,
                () -> labelConfigService.exportLabelConfiguration(configId, res)
        );
    }

    @Test
    void exportLabelConfiguration_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        final OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        final Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        final LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.getProject().setOwner("test_org");

        final String configId = "irrelevant";
        final HttpServletResponse res = mock(HttpServletResponse.class);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        assertThrows(ForbiddenException.class,
                () -> labelConfigService.exportLabelConfiguration(configId, res)
        );
    }

    @Test
    void exportLabelConfiguration_whenConfigIdIsNotValid_throwException() {
        // Set Security Context
        final OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        final HttpServletResponse res = mock(HttpServletResponse.class);

        assertThrows(NotFoundException.class,
                () -> labelConfigService.exportLabelConfiguration("invalid_config_id", res)
        );
    }

    @Test
    void exportLabelConfiguration_emptyConfiguration_exportLabelConfiguration() throws IOException {
        // Set Security Context
        final OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        final Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        final LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(2L);

        testProject.setLabelConfiguration(testLabelConfiguration);

        final String configId = "anyString";
        final HttpServletResponse res = mock(HttpServletResponse.class);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        List<LcEntryExport> exportList = labelConfigService
                .exportLabelConfiguration(configId, res);

        //Then
        assertNotNull(exportList);
        assertEquals(0, exportList.size());

        verify(labelConfigRepositoryMock).findById(anyString());
        verify(labelConfigRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);
    }

    @Test
    void exportLabelConfiguration_exportLabelConfiguration() throws IOException {
        // Set Security Context
        final OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        final Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        final Set<LcEntry> entries = new HashSet<>(createLcEntryRootElementWithChildren());
        final LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelConfiguration.setTimestamp(2L);
        testLabelConfiguration.setEntries(entries);

        testProject.setLabelConfiguration(testLabelConfiguration);

        final String configId = "anyString";
        final HttpServletResponse res = mock(HttpServletResponse.class);

        //When
        when(labelConfigRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        List<LcEntryExport> exportList = labelConfigService
                .exportLabelConfiguration(configId, res);

        //Then
        assertNotNull(exportList);
        assertEquals(entries.size(), exportList.size());

        verify(labelConfigRepositoryMock).findById(anyString());
        verify(labelConfigRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelConfigRepositoryMock);
    }

    private List<LcEntry> createLcEntryRootElementWithChildren() {
        List<LcEntry> rootElements = new ArrayList<>();
        LcEntryUpdateBindingModel point = createUpdateBindingModelEntries("point");
        LcEntryUpdateBindingModel line = createUpdateBindingModelEntries("line");
        LcEntryUpdateBindingModel polygon = createUpdateBindingModelEntries("polygon");
        LcEntryUpdateBindingModel rectangle = createUpdateBindingModelEntries("rectangle");
        LcEntryUpdateBindingModel select = createUpdateBindingModelEntries("select");
        LcEntryUpdateBindingModel checklist = createUpdateBindingModelEntries("checklist");
        LcEntryUpdateBindingModel freetext = createUpdateBindingModelEntries("freetext");

        LcEntryPoint pointLcEntry = mapper.map(point, LcEntryPoint.class);
        pointLcEntry.setType(LcEntryType.POINT);
        LcEntryLine lineLcEntry = mapper.map(line, LcEntryLine.class);
        lineLcEntry.setType(LcEntryType.LINE);
        LcEntryPolygon polygonLcEntry = mapper.map(polygon, LcEntryPolygon.class);
        polygonLcEntry.setType(LcEntryType.POLYGON);
        LcEntryRectangle rectangleLcEntry = mapper.map(rectangle, LcEntryRectangle.class);
        rectangleLcEntry.setType(LcEntryType.RECTANGLE);
        LcEntrySelect selectLcEntry = mapper.map(select, LcEntrySelect.class);
        selectLcEntry.setType(LcEntryType.SELECT);
        LcEntryChecklist checklistLcEntry = mapper.map(checklist, LcEntryChecklist.class);
        checklistLcEntry.setType(LcEntryType.CHECKLIST);
        LcEntryFreeText freetextLcEntry = mapper.map(freetext, LcEntryFreeText.class);
        freetextLcEntry.setType(LcEntryType.FREETEXT);

        // LcEntry:  Construct parent-children relationships
        pointLcEntry.getChildren().add(lineLcEntry);
        lineLcEntry.setParentEntry(pointLcEntry);
        lineLcEntry.getChildren().add(polygonLcEntry);
        polygonLcEntry.setParentEntry(lineLcEntry);
        polygonLcEntry.getChildren().add(rectangleLcEntry);
        rectangleLcEntry.setParentEntry(polygonLcEntry);

        checklistLcEntry.getChildren().add(freetextLcEntry);
        freetextLcEntry.setParentEntry(checklistLcEntry);

        rootElements.add(pointLcEntry);
        rootElements.add(selectLcEntry);

        return rootElements;
    }

    private List<LcEntryUpdateBindingModel> createUpdateBindingModelList() {
        List<LcEntryUpdateBindingModel> testLcEntryUpdateBindingModels = new ArrayList<>();

        LcEntryUpdateBindingModel point = createUpdateBindingModelEntries("point");
        LcEntryUpdateBindingModel line = createUpdateBindingModelEntries("line");
        LcEntryUpdateBindingModel polygon = createUpdateBindingModelEntries("polygon");
        LcEntryUpdateBindingModel rectangle = createUpdateBindingModelEntries("rectangle");
        LcEntryUpdateBindingModel select = createUpdateBindingModelEntries("select");
        LcEntryUpdateBindingModel checklist = createUpdateBindingModelEntries("checklist");
        LcEntryUpdateBindingModel freetext = createUpdateBindingModelEntries("freetext");

        // LcEntryUpdateBindingModel:  Construct parent-children relationships
        point.getChildren().add(select);
        select.setParentEntry(point);

        polygon.getChildren().add(checklist);
        checklist.setParentEntry(polygon);

        rectangle.getChildren().add(freetext);
        freetext.setParentEntry(rectangle);

        testLcEntryUpdateBindingModels.add(point);
        testLcEntryUpdateBindingModels.add(line);
        testLcEntryUpdateBindingModels.add(polygon);
        testLcEntryUpdateBindingModels.add(rectangle);

        return testLcEntryUpdateBindingModels;
    }

    private LcEntryUpdateBindingModel createUpdateBindingModelEntries(String type) {

        switch (type.toLowerCase()) {
            case "line":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 1,
                        "entrykey 1",
                        "entryValue 1",
                        "line",
                        "blue",
                        "1",
                        null,
                        false,
                        null,
                        null);
            case "point":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 2,
                        "entrykey 2",
                        "entryValue 2",
                        "point",
                        "red",
                        "2",
                        null,
                        false,
                        null,
                        null);
            case "polygon":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 3,
                        "entrykey 3",
                        "entryValue 3",
                        "polygon",
                        "red",
                        "3",
                        null,
                        false,
                        null,
                        null);
            case "rectangle":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 4,
                        "entrykey 4",
                        "entryValue 4",
                        "rectangle",
                        "red",
                        "4",
                        null,
                        false,
                        null,
                        null);

            case "select":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 5,
                        "entrykey 5",
                        "entryValue 5",
                        "select",
                        null,
                        null,
                        null,
                        true,
                        new HashMap<>(),
                        null);
            case "checklist":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 7,
                        "entrykey 7",
                        "entryValue 7",
                        "checklist",
                        null,
                        null,
                        null,
                        true,
                        new HashMap<>(),
                        null);
            case "freetext":
                return LcEntryUtils.createTestLcEntryUpdateBindingModel(
                        LC_ENTRY_ID + 8,
                        "entrykey 8",
                        "entryValue 8",
                        "freetext",
                        null,
                        null,
                        15,
                        true,
                        null,
                        null);
            default:
                throw new GenericException("entry_type_not_found", null, null, "entry type");
        }
    }
}
