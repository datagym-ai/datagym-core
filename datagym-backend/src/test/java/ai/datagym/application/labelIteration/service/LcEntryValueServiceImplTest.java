package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryLine;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPoint;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPolygon;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryRectangle;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.ChecklistValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.FreetextValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.SelectValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.LineValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.PointValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.PolygonValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.RectangleValueViewModel;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.repo.PointPojoRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;
import static ai.datagym.application.testUtils.LcEntryValueUtils.LC_ENTRY_VALUE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LcEntryValueServiceImplTest {
    private LcEntryValueService lcEntryValueService;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private LcEntryRepository lcEntryRepositoryMock;

    @Mock
    private LabelIterationRepository labelIterationRepositoryMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @Mock
    private PointPojoRepository pointPojoRepositoryMock;

    @Mock
    private LabelConfigurationRepository labelConfigurationRepositoryMock;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    private LcEntryValidation lcEntryValidation;

    private Project testProject;
    private LabelConfiguration testLabelConfiguration;
    private LabelIteration testLabelIteration;
    private Media testMedia;

    private LcEntryRectangle testLcEntryRectangle;
    private LcEntryLine testLcEntryLine;
    private LcEntrySelect testLcEntrySelect;
    private LcEntryFreeText testLcEntryText;
    private LcEntryPoint testLcEntryPoint;
    private LcEntryPolygon testLcEntryPolygon;
    private LcEntryChecklist testLcEntryChecklist;


    private LcEntryRectangleValue testLcEntryRectangleValue;
    private LcEntryLineValue testLcEntryLineValue;
    private LcEntrySelectValue testLcEntrySelectValue;
    private LcEntryTextValue testLcEntryTextValue;
    private LcEntryPointValue testLcEntryPointValue;
    private LcEntryPolygonValue testLcEntryPolyValue;
    private LcEntryCheckListValue testLcEntryCheckListValue;

    List<PointPojo> pointPojoListLine = new ArrayList<>();
    List<PointPojo> pointPojoList = new ArrayList<>();
    List<String> checkedValues = new ArrayList<>();
    List<LcEntryValue> lcEntryValues = new ArrayList<>();

    @BeforeEach
    void setUp() {
        lcEntryValidation = new LcEntryValidation(lcEntryValueRepositoryMock);
        lcEntryValueService = new LcEntryValueServiceImpl(
                lcEntryValueRepositoryMock,
                labelIterationRepositoryMock,
                lcEntryRepositoryMock,
                mediaRepositoryMock,
                pointPojoRepositoryMock,
                labelConfigurationRepositoryMock,
                labelTaskRepositoryMock, lcEntryValidation);

        testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        testMedia = ImageUtils.createTestImage(IMAGE_ID);

        // LcEntries
        testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 0);

        testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        testLcEntryLine.setId(LC_ENTRY_ID + 1);

        testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 3);

        testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        testLcEntryText.setId(LC_ENTRY_ID + 4);

        testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);
        testLcEntryPoint.setId(LC_ENTRY_ID + 5);

        testLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);
        testLcEntryPolygon.setId(LC_ENTRY_ID + 6);

        testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryChecklist.setId(LC_ENTRY_ID + 7);

        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);

        testLcEntryPoint.getChildren().add(testLcEntryText);
        testLcEntryText.setParentEntry(testLcEntryPoint);

        testLcEntryPolygon.getChildren().add(testLcEntryChecklist);
        testLcEntryChecklist.setParentEntry(testLcEntryPolygon);


        // LcEntryValues
        testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID + 0);
        testLcEntryRectangleValue.setX(0.5);
        testLcEntryRectangleValue.setY(0.5);
        testLcEntryRectangleValue.setWidth(0.5);
        testLcEntryRectangleValue.setHeight(0.5);

        testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);
        pointPojoListLine = PointPojoUtils.createPointPojoList(2);
        testLcEntryLineValue.setPoints(pointPojoListLine);

        testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 3);
        testLcEntrySelectValue.setSelectKey("selectKey");

        testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValue.setId(LC_ENTRY_VALUE_ID + 4);
        testLcEntryTextValue.setText("text");

        testLcEntryPointValue = LcEntryValueUtils.createTestLcEntryPointValue(testLabelIteration, testMedia, testLcEntryPoint);
        testLcEntryPointValue.setId(LC_ENTRY_VALUE_ID + 5);
        testLcEntryPointValue.setX(0.5);
        testLcEntryPointValue.setY(0.5);

        testLcEntryPolyValue = LcEntryValueUtils.createTestLcEntryPolyValue(testLabelIteration, testMedia, testLcEntryPolygon);
        testLcEntryPolyValue.setId(LC_ENTRY_VALUE_ID + 6);
        pointPojoList = PointPojoUtils.createPointPojoList(3);
        testLcEntryPolyValue.setPoints(pointPojoList);

        testLcEntryCheckListValue = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValue.setId(LC_ENTRY_VALUE_ID + 7);
        checkedValues.add("value1");
        checkedValues.add("value2");
        testLcEntryCheckListValue.setCheckedValues(checkedValues);

        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);

        testLcEntryPointValue.getChildren().add(testLcEntryTextValue);
        testLcEntryTextValue.setLcEntryValueParent(testLcEntryPointValue);

        testLcEntryPolyValue.getChildren().add(testLcEntryCheckListValue);
        testLcEntryCheckListValue.setLcEntryValueParent(testLcEntryPolyValue);

        lcEntryValues.add(testLcEntryRectangleValue);
        lcEntryValues.add(testLcEntryLineValue);
        lcEntryValues.add(testLcEntryPointValue);
        lcEntryValues.add(testLcEntryPolyValue);
    }

    @Test
    void getLabelIterationValues_whenIdIsValid_getLabelIterationValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        List<LcEntryValue> lcEntryValues = new ArrayList<>() {{
            add(testLcEntryRectangleValue);
            add(testLcEntryLineValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .getLabelIterationValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID);

        //Then
        LcEntryRectangleValue expectedLcEntry = (LcEntryRectangleValue) lcEntryValues.get(0);
        RectangleValueViewModel actualLcEntryValue = (RectangleValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        assertNotNull(labelIterationViewModel);
        assertEquals(testLabelIteration.getId(), labelIterationViewModel.getId());
        assertEquals(testLabelIteration.getProject().getId(), labelIterationViewModel.getProjectId());
        assertEquals(testLabelIteration.getId(), labelIterationViewModel.getId());
        assertEquals(3, labelIterationViewModel.getEntryValues().size());

        assertEquals(expectedLcEntry.getMedia().getId(), actualLcEntryValue.getMediaId());
        assertEquals(expectedLcEntry.getLcEntry().getId(), actualLcEntryValue.getLcEntryId());
        assertEquals(expectedLcEntry.getLabeler(), actualLcEntryValue.getLabeler());
        assertEquals(expectedLcEntry.getHeight(), actualLcEntryValue.getHeight());
        assertEquals(expectedLcEntry.getWidth(), actualLcEntryValue.getWidth());
        assertEquals(expectedLcEntry.getX(), actualLcEntryValue.getPoint().getX());
        assertEquals(expectedLcEntry.getY(), actualLcEntryValue.getPoint().getY());
        assertEquals(expectedLcEntry.getLcEntry().getType().name(), actualLcEntryValue.getEntryTypeLcEntry());

        verify(labelIterationRepositoryMock).findById(anyString());
        verify(labelIterationRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelIterationRepositoryMock);

        verify(lcEntryValueRepositoryMock)
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString());
        verify(lcEntryValueRepositoryMock, times(1))
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(lcEntryValueRepositoryMock);
    }

    @Test
    void getLabelIterationValues_whenIdIsValidAndLcValuesWithChildren_getLabelIterationValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        // LcEntry
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);

        // LcEntryValue
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);


        List<LcEntryValue> lcEntryValues = new ArrayList<>() {{
            add(testLcEntryRectangleValue);
            add(testLcEntryLineValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryValueRepositoryMock.findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        LabelIterationViewModel labelIterationViewModel = lcEntryValueService.getLabelIterationValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID);

        //Then
        LcEntryRectangleValue expectedLcEntry = (LcEntryRectangleValue) lcEntryValues.get(0);
        RectangleValueViewModel actualLcEntryValue = (RectangleValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        assertNotNull(labelIterationViewModel);
        assertEquals(testLabelIteration.getId(), labelIterationViewModel.getId());
        assertEquals(testLabelIteration.getProject().getId(), labelIterationViewModel.getProjectId());
        assertEquals(testLabelIteration.getId(), labelIterationViewModel.getId());
        assertEquals(2, labelIterationViewModel.getEntryValues().size());

        assertEquals(expectedLcEntry.getMedia().getId(), actualLcEntryValue.getMediaId());
        assertEquals(expectedLcEntry.getLcEntry().getId(), actualLcEntryValue.getLcEntryId());
        assertEquals(expectedLcEntry.getLabeler(), actualLcEntryValue.getLabeler());
        assertEquals(expectedLcEntry.getHeight(), actualLcEntryValue.getHeight());
        assertEquals(expectedLcEntry.getWidth(), actualLcEntryValue.getWidth());
        assertEquals(expectedLcEntry.getX(), actualLcEntryValue.getPoint().getX());
        assertEquals(expectedLcEntry.getY(), actualLcEntryValue.getPoint().getY());
        assertEquals(expectedLcEntry.getLcEntry().getType().name(), actualLcEntryValue.getEntryTypeLcEntry());

        verify(labelIterationRepositoryMock).findById(anyString());
        verify(labelIterationRepositoryMock, times(1)).findById(anyString());
        verifyNoMoreInteractions(labelIterationRepositoryMock);

        verify(lcEntryValueRepositoryMock).findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString());
        verify(lcEntryValueRepositoryMock, times(1)).findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(lcEntryValueRepositoryMock);
    }

    @Test
    void getLabelIterationValues_whenIterationIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.getLabelIterationValues("invalid_iteration_id", IMAGE_ID, LABEL_TASK_ID)
        );
    }

    @Test
    void getLabelIterationValues_whenImageIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.getLabelIterationValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID)
        );
    }

    @Test
    void getLabelConfiguration_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.getLabelIterationValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID)
        );
    }

    @Test
    void getLabelConfiguration_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.getLabelIterationValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID)
        );
    }

    @Test
    void getLabelConfiguration_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.getLabelIterationValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID)
        );
    }

    @Test
    void createLcEntryValue_whenInputsAreValid_createLcEntryValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        LcEntryValueViewModel actualLcEntryValue = lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel);

        //Then
        assertNotNull(actualLcEntryValue);
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(LC_ENTRY_ID, actualLcEntryValue.getLcEntryId());
        assertEquals("eforce21", actualLcEntryValue.getLabeler());
        assertEquals(testLcEntryRectangle.getType().name(), actualLcEntryValue.getEntryTypeLcEntry());
        assertNull(actualLcEntryValue.getLcEntryValueParentId());

        verify(lcEntryValueRepositoryMock).save(any(LcEntryValue.class));
        verify(lcEntryValueRepositoryMock, times(1)).save(any(LcEntryValue.class));
        verifyNoMoreInteractions(lcEntryValueRepositoryMock);
    }

    @Test
    void createLcEntryValue_whenInputsAreValidAndParentEntryValueIdIsNotNull_createLcEntryValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntry parent
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 1);
        String parentLcEntryId = testLcEntryRectangle.getId();

        // LcEntry child
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);
        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        String childLcEntryId = testLcEntrySelect.getId();

        // LcEntryValue parent
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID);

        // LcEntryValueCreateBindingModel
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_VALUE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(parentLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(childLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        LcEntryValueViewModel actualLcEntryValue = lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel);

        //Then
        assertNotNull(actualLcEntryValue);
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(LC_ENTRY_ID, actualLcEntryValue.getLcEntryId());
        assertEquals("eforce21", actualLcEntryValue.getLabeler());
        assertEquals(testLcEntrySelect.getType().name(), actualLcEntryValue.getEntryTypeLcEntry());
        assertEquals(LC_ENTRY_VALUE_ID, actualLcEntryValue.getLcEntryValueParentId());

        verify(lcEntryValueRepositoryMock).save(any(LcEntryValue.class));
        verify(lcEntryValueRepositoryMock, times(1)).save(any(LcEntryValue.class));
    }

    @Test
    void createLcEntryValue_whenParentLcEntryTypeIsNotEqualToParentLcEntryValueType_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntry parent
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 1);
        String parentLcEntryId = testLcEntryRectangle.getId();

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);

        // LcEntry child
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);
        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        String childLcEntryId = testLcEntrySelect.getId();

        // LcEntryValue parent
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID);

        // LcEntryValueCreateBindingModel
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_VALUE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(parentLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(childLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenParentLcEntryValueContainsAlreadyAllChildrenFromConfig_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntry parent
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 1);
        String parentLcEntryId = testLcEntryRectangle.getId();

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);

        // LcEntry child
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);
        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        String childLcEntryId = testLcEntrySelect.getId();

        // LcEntryValue parent

        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID);

        // LcEntryValue child
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);
        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);

        // LcEntryValueCreateBindingModel
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_VALUE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(parentLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(childLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenLabelConfigDoesntContainLcEntryFromCurrentValueType_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntry parent
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 1);
        String parentLcEntryId = testLcEntryRectangle.getId();

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);

        // LcEntry child
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);
        testLcEntryRectangle.getChildren().add(testLcEntrySelect);

        LcEntryChecklist testLcEntryCheckList = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryCheckList.setParentEntry(testLcEntryRectangle);
        String childLcEntryId = testLcEntryCheckList.getId();

        // LcEntryValue parent
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID);

        // LcEntryValue child
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);
        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);

        // LcEntryValueCreateBindingModel
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_VALUE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(parentLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(childLcEntryId))
                .thenReturn(java.util.Optional.of(testLcEntryCheckList));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenIterationIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenImageIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenLcEntryIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenIterationProjectIdAndLcEntryProjectIdAreNotEqual_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Project testProject_InvalidId = ProjectUtils.createTestProject("invalid_id");
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject_InvalidId);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());


        assertThrows(GenericException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenParentEntryValueIdIsNullAndParentEntryIsNotNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        assertThrows(GenericException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));


        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void createLcEntryValue_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));


        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.createLcEntryValue(lcEntryValueCreateBindingModel)
        );
    }

    @Test
    void deleteLcValue_whenLcValueIdIsValid_deleteLcValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);

        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);

        // When
        when(lcEntryValueRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        // Then
        lcEntryValueService.deleteLcValue(testLcEntryRectangleValue.getId());

        verify(lcEntryValueRepositoryMock).delete(any(LcEntryValue.class));

        ArgumentCaptor<LcEntryValue> lcEntryValueCapture = ArgumentCaptor.forClass(LcEntryValue.class);
        verify(lcEntryValueRepositoryMock, times(1)).delete(lcEntryValueCapture.capture());
        assertThat(lcEntryValueCapture.getValue().getId()).isEqualTo(LC_ENTRY_VALUE_ID);
        assertThat(lcEntryValueCapture.getValue().getLabeler()).isEqualTo(testLcEntryRectangleValue.getLabeler());
        assertThat(lcEntryValueCapture.getValue().getLcEntry().getType().name())
                .isEqualTo(testLcEntryRectangleValue.getLcEntry().getType().name());

        verifyNoMoreInteractions(lcEntryRepositoryMock);
    }

    @Test
    void deleteLcValue_whenLcValueIsClassification_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils
                .createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        //When
        when(lcEntryValueRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        assertThrows(GenericException.class,
                () -> lcEntryValueService.deleteLcValue(testLcEntrySelectValue.getId()));
    }

    @Test
    void deleteLcValue_whenLcValueParentIsNotNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        // LcEntry
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);

        // LcEntryValue
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils
                .createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);

        //When
        when(lcEntryValueRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        assertThrows(GenericException.class,
                () -> lcEntryValueService.deleteLcValue(testLcEntrySelectValue.getId()));
    }

    @Test
    void deleteLcValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.deleteLcValue(testLcEntrySelectValue.getId())
        );
    }

    @Test
    void deleteLcValue_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        // LcEntry
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        testLcEntryRectangle.getChildren().add(testLcEntrySelect);
        testLcEntrySelect.setParentEntry(testLcEntryRectangle);

        // LcEntryValue
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils
                .createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        testLcEntryRectangleValue.getChildren().add(testLcEntrySelectValue);
        testLcEntrySelectValue.setLcEntryValueParent(testLcEntryRectangleValue);

        //When
        when(lcEntryValueRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.deleteLcValue(testLcEntrySelectValue.getId()));
    }

    @Test
    void deleteLcValue_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);


        //When
        when(lcEntryValueRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.deleteLcValue(testLcEntrySelectValue.getId()));
    }

    @Test
    void updateSingleLcEntryValue_whenInputsAreValidAndParentIsNull_updateSingleLcEntryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                null,
                "free_text",
                null,
                0.0,
                0.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );

        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);

        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        FreetextValueViewModel actualLcEntryValue = (FreetextValueViewModel) lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel);

        //Then
        assertNotNull(actualLcEntryValue);
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(LC_ENTRY_ID, actualLcEntryValue.getLcEntryId());
        assertEquals("eforce21", actualLcEntryValue.getLabeler());
        assertEquals(testLcEntryValueUpdateBindingModel.getText(), actualLcEntryValue.getText());

        verify(lcEntryValueRepositoryMock).save(any(LcEntryValue.class));
        verify(lcEntryValueRepositoryMock, times(1)).save(any(LcEntryValue.class));
    }

    @Test
    void updateSingleLcEntryValue_whenInputsAreValidAndParentIsNotNull_updateSingleLcEntryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 1,
                "free_text",
                null,
                0.0,
                0.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );

        // LcEntry
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);

        // LcEntryValue
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);
        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(lcEntryValueRepositoryMock.findById(testLcEntryLineValue.getId()))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        LcEntryValueViewModel lcEntryValueViewModel = lcEntryValueService
                .updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel);

        FreetextValueViewModel actualLcEntryValue = (FreetextValueViewModel) Hibernate.unproxy(lcEntryValueViewModel);
        //Then
        assertNotNull(actualLcEntryValue);
        assertNotNull(actualLcEntryValue.getLcEntryValueParentId());
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(LC_ENTRY_ID, actualLcEntryValue.getLcEntryId());
        assertEquals("eforce21", actualLcEntryValue.getLabeler());
        assertEquals(testLcEntryValueUpdateBindingModel.getText(), actualLcEntryValue.getText());
        assertEquals(testLcEntryValueUpdateBindingModel.getLcEntryValueParentId(), actualLcEntryValue.getLcEntryValueParentId());

//        verify(lcEntryValueRepositoryMock).save(any(LcEntryValue.class));
//        verify(lcEntryValueRepositoryMock, times(1)).save(any(LcEntryValue.class));
    }

    @Test
    void updateSingleLcEntryValue_whenLcValueIdAndBindingModelLcValueIdAreNotEqual_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID + 1,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 2,
                "free_text",
                null,
                10.0,
                10.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );


        //Then
        assertThrows(GenericException.class,
                () -> lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void updateSingleLcEntryValue_whenParentValueIsNotNullAndLcEntryValueIsGeometry_updateSingleLcEntryValue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 1,
                "free_text",
                null,
                10.0,
                10.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );


        // LcEntry
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        // LcEntryValue
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);
        LcEntryPointValue testLcEntryPointValue = LcEntryValueUtils.createTestLcEntryPointValue(testLabelIteration, testMedia, testLcEntryPoint);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPoint));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPointValue));

        when(lcEntryValueRepositoryMock.findById(testLcEntryLineValue.getId()))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        //Then
        LcEntryValueViewModel lcEntryValueViewModel = lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel);
        assertNotNull(lcEntryValueViewModel);
        assertEquals(IMAGE_ID, lcEntryValueViewModel.getMediaId());
        assertEquals(LC_ITERATION_ID, lcEntryValueViewModel.getLabelIterationId());
        assertEquals(LC_ENTRY_ID, lcEntryValueViewModel.getLcEntryId());
        assertEquals("eforce21", lcEntryValueViewModel.getLabeler());
        assertEquals(testLcEntryValueUpdateBindingModel.getLcEntryValueParentId(), lcEntryValueViewModel.getLcEntryValueParentId());
    }

    @Test
    void updateSingleLcEntryValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 1,
                "free_text",
                null,
                10.0,
                10.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void updateSingleLcEntryValue_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 1,
                "free_text",
                null,
                10.0,
                10.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );

        // LcEntry
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        // LcEntryValue
        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);
        LcEntryPointValue testLcEntryPointValue = LcEntryValueUtils.createTestLcEntryPointValue(testLabelIteration, testMedia, testLcEntryPoint);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPointValue));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void updateSingleLcEntryValue_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);

        LcEntryValueUpdateBindingModel testLcEntryValueUpdateBindingModel = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModel(
                LC_ENTRY_VALUE_ID,
                LC_ENTRY_ID,
                LC_ENTRY_VALUE_ID + 1,
                "free_text",
                null,
                10.0,
                10.0,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                false
        );

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPointValue));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateSingleLcEntryValue(LC_ENTRY_VALUE_ID, testLcEntryValueUpdateBindingModel)
        );
    }

    @Test
    void updateLcEntryValues_whenAllInputsAreValid_updateLcEntryValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(3);
        LcEntryValueUpdateBindingModel firstLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);
        firstLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 0);

        LcEntryValueUpdateBindingModel secondLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(1);
        secondLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 1);

        LcEntryValueUpdateBindingModel thirdLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(2);
        thirdLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 2);
        thirdLcEntryValueUpdateBindingModel.setSelectKey("selectKey");

        // LcEntry
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 0);

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        testLcEntryLine.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        // LcEntryValues
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils.createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID + 0);

        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");

        List<LcEntryValue> lcEntryValues = new ArrayList<>() {{
            add(testLcEntryRectangleValue);
            add(testLcEntryLineValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLine));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());
        //Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false);

        LcEntrySelectValue expectedLcEntry = (LcEntrySelectValue) lcEntryValues.get(2);
        SelectValueViewModel actualLcEntryValue = (SelectValueViewModel) labelIterationViewModel.getEntryValues().get(2);

        //Then
        assertNotNull(actualLcEntryValue);
        assertNotNull(actualLcEntryValue.getEntryTypeLcEntry());
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(expectedLcEntry.getLabelIteration().getId(), actualLcEntryValue.getLabelIterationId());
        assertEquals(expectedLcEntry.getLcEntry().getId(), actualLcEntryValue.getLcEntryId());
        assertEquals(expectedLcEntry.getLabeler(), actualLcEntryValue.getLabeler());
        assertEquals(expectedLcEntry.getSelectKey(), actualLcEntryValue.getSelectKey());
        assertEquals(expectedLcEntry.getLcEntry().getType().name(), actualLcEntryValue.getEntryTypeLcEntry());
        assertEquals(expectedLcEntry.getLcEntry().getEntryKey(), actualLcEntryValue.getEntryKeyLcEntry());
        assertEquals(expectedLcEntry.getLcEntry().getEntryValue(), actualLcEntryValue.getEntryValueLcEntry());
        assertNull(actualLcEntryValue.getLcEntryValueParentId());
    }

    @Test
    void updateLcEntryValues_whenAllInputsAreValidAndNestedEntries_updateLcEntryValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        List<LcEntryValueUpdateBindingModel> valueUpdateBindingModels = createLcEntryValueUpdateBindingModels();

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eforce21");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLine));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 4))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 5))
                .thenReturn(java.util.Optional.of(testLcEntryPoint));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 6))
                .thenReturn(java.util.Optional.of(testLcEntryPolygon));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 7))
                .thenReturn(java.util.Optional.of(testLcEntryChecklist));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 4))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 5))
                .thenReturn(java.util.Optional.of(testLcEntryPointValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 6))
                .thenReturn(java.util.Optional.of(testLcEntryPolyValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 7))
                .thenReturn(java.util.Optional.of(testLcEntryCheckListValue));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());
        //Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, valueUpdateBindingModels, false);

        //Rectangle
        LcEntryRectangleValue expectedRectangleValue = (LcEntryRectangleValue) lcEntryValues.get(0);
        RectangleValueViewModel actualRectangleValueViewModel = (RectangleValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        // Select
        LcEntrySelectValue expectedSelectValueFirst = (LcEntrySelectValue) expectedRectangleValue.getChildren().get(0);
        SelectValueViewModel actualSelectFirstValueViewModel = (SelectValueViewModel) actualRectangleValueViewModel.getChildren().get(0);

        //Line
        LcEntryLineValue expectedLineValue = (LcEntryLineValue) lcEntryValues.get(1);
        LineValueViewModel actualLineValueViewModel = (LineValueViewModel) labelIterationViewModel.getEntryValues().get(1);

        //Point
        LcEntryPointValue expectedPointValue = (LcEntryPointValue) lcEntryValues.get(2);
        PointValueViewModel actualPointValueViewModel = (PointValueViewModel) labelIterationViewModel.getEntryValues().get(2);

        //Text
        LcEntryTextValue expectedTextValue = (LcEntryTextValue) expectedPointValue.getChildren().get(0);
        FreetextValueViewModel actualTextValueViewModel = (FreetextValueViewModel) actualPointValueViewModel.getChildren().get(0);

        //Polygon
        LcEntryPolygonValue expectedPolygonValue = (LcEntryPolygonValue) lcEntryValues.get(3);
        PolygonValueViewModel actualPolygonValueViewModel = (PolygonValueViewModel) labelIterationViewModel.getEntryValues().get(3);

        //Text
        LcEntryCheckListValue expectedCheckListValue = (LcEntryCheckListValue) expectedPolygonValue.getChildren().get(0);
        ChecklistValueViewModel actualChecklistValueViewModel = (ChecklistValueViewModel) actualPolygonValueViewModel.getChildren().get(0);

        assertNotNull(labelIterationViewModel);
        assertEquals(LC_ITERATION_ID, labelIterationViewModel.getId());
        assertEquals(PROJECT_ID, labelIterationViewModel.getProjectId());
        assertEquals(4, labelIterationViewModel.getEntryValues().size());
        assertEquals(1, labelIterationViewModel.getRun());

        // Rectangle
        assertEquals(expectedRectangleValue.getLcEntry().getType().name(), actualRectangleValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedRectangleValue.getLabeler(), actualRectangleValueViewModel.getLabeler());
        assertEquals(expectedRectangleValue.getWidth(), actualRectangleValueViewModel.getWidth());
        assertEquals(expectedRectangleValue.getHeight(), actualRectangleValueViewModel.getHeight());
        assertEquals(expectedRectangleValue.getX(), actualRectangleValueViewModel.getPoint().getX());
        assertEquals(expectedRectangleValue.getY(), actualRectangleValueViewModel.getPoint().getY());
        assertEquals("blue", actualRectangleValueViewModel.getColor());
        assertEquals("4", actualRectangleValueViewModel.getShortcut());
        assertEquals(expectedRectangleValue.getMedia().getId(), actualRectangleValueViewModel.getMediaId());
        assertEquals(expectedRectangleValue.getLcEntry().getId(), actualRectangleValueViewModel.getLcEntryId());
        assertEquals(expectedRectangleValue.getLcEntry().getConfiguration().getId(), actualRectangleValueViewModel.getConfigurationId());
        assertEquals(expectedRectangleValue.getChildren().size(), actualRectangleValueViewModel.getChildren().size());

        // Select First
        assertEquals(expectedSelectValueFirst.getLcEntry().getType().name(), actualSelectFirstValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedSelectValueFirst.getLabeler(), actualSelectFirstValueViewModel.getLabeler());
        assertEquals(expectedSelectValueFirst.getSelectKey(), actualSelectFirstValueViewModel.getSelectKey());
        assertEquals(expectedSelectValueFirst.getMedia().getId(), actualSelectFirstValueViewModel.getMediaId());
        assertEquals(expectedSelectValueFirst.getLcEntry().getId(), actualSelectFirstValueViewModel.getLcEntryId());
        assertEquals(expectedSelectValueFirst.getLcEntry().getConfiguration().getId(), actualSelectFirstValueViewModel.getConfigurationId());
        assertEquals(expectedSelectValueFirst.getChildren().size(), actualSelectFirstValueViewModel.getChildren().size());

        // Line
        assertEquals(expectedLineValue.getLcEntry().getType().name(), actualLineValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedLineValue.getLabeler(), actualLineValueViewModel.getLabeler());
        assertEquals(expectedLineValue.getPoints().get(0).getX(), actualLineValueViewModel.getPoints().get(0).getX());
        assertEquals(expectedLineValue.getPoints().get(0).getY(), actualLineValueViewModel.getPoints().get(0).getY());
        assertEquals("blue", actualLineValueViewModel.getColor());
        assertEquals("1", actualLineValueViewModel.getShortcut());
        assertEquals(expectedLineValue.getMedia().getId(), actualLineValueViewModel.getMediaId());
        assertEquals(expectedLineValue.getLcEntry().getId(), actualLineValueViewModel.getLcEntryId());
        assertEquals(expectedLineValue.getLcEntry().getConfiguration().getId(), actualLineValueViewModel.getConfigurationId());
        assertEquals(expectedLineValue.getChildren().size(), actualLineValueViewModel.getChildren().size());


        // Point
        assertEquals(expectedPointValue.getLcEntry().getType().name(), actualPointValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedPointValue.getLabeler(), actualPointValueViewModel.getLabeler());
        assertEquals(expectedPointValue.getX(), actualPointValueViewModel.getPoint().getX());
        assertEquals(expectedPointValue.getY(), actualPointValueViewModel.getPoint().getY());
        assertEquals("blue", actualPointValueViewModel.getColor());
        assertEquals("2", actualPointValueViewModel.getShortcut());
        assertEquals(expectedPointValue.getMedia().getId(), actualPointValueViewModel.getMediaId());
        assertEquals(expectedPointValue.getLcEntry().getId(), actualPointValueViewModel.getLcEntryId());
        assertEquals(expectedPointValue.getLcEntry().getConfiguration().getId(), actualPointValueViewModel.getConfigurationId());
        assertEquals(expectedPointValue.getChildren().size(), actualPointValueViewModel.getChildren().size());

        // Text
        assertEquals(expectedTextValue.getLcEntry().getType().name(), actualTextValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedTextValue.getLabeler(), actualTextValueViewModel.getLabeler());
        assertEquals(expectedTextValue.getText(), actualTextValueViewModel.getText());
        assertEquals(expectedTextValue.getMedia().getId(), actualTextValueViewModel.getMediaId());
        assertEquals(expectedTextValue.getLcEntry().getId(), actualTextValueViewModel.getLcEntryId());
        assertEquals(expectedTextValue.getLcEntry().getConfiguration().getId(), actualTextValueViewModel.getConfigurationId());
        assertEquals(expectedTextValue.getChildren().size(), actualTextValueViewModel.getChildren().size());

        // Polygon
        assertEquals(expectedPolygonValue.getLcEntry().getType().name(), actualPolygonValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedPolygonValue.getLabeler(), actualPolygonValueViewModel.getLabeler());
//        assertEquals(expectedPolygonValue.getPoints().get(0).getX(), actualPolygonValueViewModel.getPoints().get(0).getX());
//        assertEquals(expectedPolygonValue.getPoints().get(0).getY(), actualPolygonValueViewModel.getPoints().get(0).getY());
        assertEquals("blue", actualPolygonValueViewModel.getColor());
        assertEquals("3", actualPolygonValueViewModel.getShortcut());
        assertEquals(expectedPolygonValue.getMedia().getId(), actualPolygonValueViewModel.getMediaId());
        assertEquals(expectedPolygonValue.getLcEntry().getId(), actualPolygonValueViewModel.getLcEntryId());
        assertEquals(expectedPolygonValue.getLcEntry().getConfiguration().getId(), actualPolygonValueViewModel.getConfigurationId());
        assertEquals(expectedPolygonValue.getChildren().size(), actualPolygonValueViewModel.getChildren().size());

        // Checklist
        assertEquals(expectedCheckListValue.getLcEntry().getType().name(), actualChecklistValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedCheckListValue.getLabeler(), actualChecklistValueViewModel.getLabeler());
        assertEquals(expectedCheckListValue.getCheckedValues().size(), actualChecklistValueViewModel.getCheckedValues().size());
        assertEquals(expectedCheckListValue.getCheckedValues().get(0), actualChecklistValueViewModel.getCheckedValues().get(0));
        assertEquals(expectedCheckListValue.getMedia().getId(), actualChecklistValueViewModel.getMediaId());
        assertEquals(expectedCheckListValue.getLcEntry().getId(), actualChecklistValueViewModel.getLcEntryId());
        assertEquals(expectedCheckListValue.getLcEntry().getConfiguration().getId(), actualChecklistValueViewModel.getConfigurationId());
        assertEquals(expectedCheckListValue.getChildren().size(), actualChecklistValueViewModel.getChildren().size());
    }

    @Test
    void updateLcEntryValues_whenIterationIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(8);

        //Then
        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.updateLcEntryValues("invalid_iteration_id", IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false));
    }

    @Test
    void updateLcEntryValues_whenImageIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(8);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false));
    }

    @Test
    void updateLcEntryValues_whenLcEntryIdFromBindingModelIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(8);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        //Then
        assertThrows(NotFoundException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false));
    }

    @Test
    void updateLcEntryValues_whenLcEntryValueIdAlreadyExists_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        List<LcEntryValueUpdateBindingModel> valueUpdateBindingModels = createLcEntryValueUpdateBindingModels();

        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = valueUpdateBindingModels.get(1);
        lcEntryValueUpdateBindingModel.setId(LC_ENTRY_VALUE_ID + 0);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLine));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        //Then
        assertThrows(AlreadyExistsException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID, valueUpdateBindingModels, false));
    }

    @Test
    void updateLcEntryValues_whenKeyIsRequiredButValueIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        List<LcEntryValueUpdateBindingModel> valueUpdateBindingModels = createLcEntryValueUpdateBindingModels();

        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = valueUpdateBindingModels.get(0);
        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModelChild = lcEntryValueUpdateBindingModel.getChildren().get(0);
        lcEntryValueUpdateBindingModelChild.setSelectKey(null);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLine));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 3))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(),
                        anyString(),
                        anyString()))
                .thenReturn(lcEntryValues);

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        //Then
        assertThrows(GenericException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, "invalid_image_id", LABEL_TASK_ID, valueUpdateBindingModels, false));
    }

    @Test
    void updateLcEntryValues_whenChildValueIdIsNull_updateLcEntryValues() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(3);
        LcEntryValueUpdateBindingModel firstLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);
        firstLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 0);

        LcEntryValueUpdateBindingModel secondLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(1);
        secondLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 1);
        secondLcEntryValueUpdateBindingModel.setId(null);
        secondLcEntryValueUpdateBindingModel.setLcEntryValueParentId(LC_ENTRY_VALUE_ID + 0);

        firstLcEntryValueUpdateBindingModel.getChildren().add(secondLcEntryValueUpdateBindingModel);
        secondLcEntryValueUpdateBindingModel.setParentEntry(firstLcEntryValueUpdateBindingModel);

        LcEntryValueUpdateBindingModel thirdLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(2);
        thirdLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 2);
        thirdLcEntryValueUpdateBindingModel.setSelectKey("selectKey");

        // LcEntry
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        testLcEntryRectangle.setId(LC_ENTRY_ID + 0);

        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        testLcEntryLine.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        testLcEntryRectangle.getChildren().add(testLcEntryLine);
        testLcEntryLine.setParentEntry(testLcEntryRectangle);

        // LcEntryValues
        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);
        testLcEntryRectangleValue.setId(LC_ENTRY_VALUE_ID + 0);

        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils.createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);
        testLcEntryLineValue.setId(LC_ENTRY_VALUE_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");

        testLcEntryRectangleValue.getChildren().add(testLcEntryLineValue);
        testLcEntryLineValue.setLcEntryValueParent(testLcEntryRectangleValue);

        List<LcEntryValue> lcEntryValues = new ArrayList<>() {{
            add(testLcEntryRectangleValue);
            add(testLcEntrySelectValue);
        }};

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangle));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLine));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(anyString(), anyString(), anyString()))
                .thenReturn(lcEntryValues);

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());
        //Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false);

        LcEntrySelectValue expectedLcEntry = (LcEntrySelectValue) lcEntryValues.get(1);
        SelectValueViewModel actualLcEntryValue = (SelectValueViewModel) labelIterationViewModel.getEntryValues().get(1);

        //Then
        assertNotNull(actualLcEntryValue);
        assertNotNull(actualLcEntryValue.getEntryTypeLcEntry());
        assertEquals(IMAGE_ID, actualLcEntryValue.getMediaId());
        assertEquals(LC_ITERATION_ID, actualLcEntryValue.getLabelIterationId());
        assertEquals(expectedLcEntry.getLabelIteration().getId(), actualLcEntryValue.getLabelIterationId());
        assertEquals(expectedLcEntry.getLcEntry().getId(), actualLcEntryValue.getLcEntryId());
        assertEquals(expectedLcEntry.getLabeler(), actualLcEntryValue.getLabeler());
        assertEquals(expectedLcEntry.getSelectKey(), actualLcEntryValue.getSelectKey());
        assertEquals(expectedLcEntry.getLcEntry().getType().name(), actualLcEntryValue.getEntryTypeLcEntry());
        assertEquals(expectedLcEntry.getLcEntry().getEntryKey(), actualLcEntryValue.getEntryKeyLcEntry());
        assertEquals(expectedLcEntry.getLcEntry().getEntryValue(), actualLcEntryValue.getEntryValueLcEntry());
        assertNull(actualLcEntryValue.getLcEntryValueParentId());
    }

    @Test
    void updateLcEntryValues_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(3);

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false)
        );
    }

    @Test
    void updateLcEntryValues_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(3);
        testLabelIteration.getProject().setOwner("test_org");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false)
        );
    }

    @Test
    void updateLcEntryValues_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(3);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.updateLcEntryValues(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID, testLcEntryValueUpdateBindingModels, false)
        );
    }

    @Test
    void createGlobalClassificationsValues_whenInputsAreValidAndNoGlobalClassifications_returnEmptyCollection() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel createBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);


        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        // Then
        List<LcEntryValueViewModel> globalClassificationsValues = lcEntryValueService
                .createGlobalClassificationsValuesGetRoots(LC_CONFIG_ID, createBindingModel);

        assertEquals(0, globalClassificationsValues.size());
    }

    @Test
    void createGlobalClassificationsValues_whenInputsAreValidAndGlobalClassificationsSizeIsNotZero_createGlobalClassificationsValues() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel createBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID + 0, LC_ITERATION_ID, IMAGE_ID, null);

        // LcEntry
        LcEntryChecklist testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryChecklist.setId(LC_ENTRY_ID + 0);

        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        testLcEntryText.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        testLcEntryChecklist.getChildren().add(testLcEntryText);
        testLcEntryText.setParentEntry(testLcEntryChecklist);

        List<LcEntry> globalClassificationLcEntries = new ArrayList<>() {{
            add(testLcEntryChecklist);
            add(testLcEntrySelect);
        }};

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        // LcEntryValues
        LcEntryCheckListValue testLcEntryCheckListValue = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValue.setId(LC_ENTRY_VALUE_ID + 0);

        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValue.setId(LC_ENTRY_VALUE_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");

        testLcEntryCheckListValue.getChildren().add(testLcEntryTextValue);
        testLcEntryTextValue.setLcEntryValueParent(testLcEntryCheckListValue);

        List<LcEntryValue> globalClassificationLcEntryValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryChecklist));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryCheckListValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        List<LcEntryValue> findByLcEntryIdCheckList = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
        }};

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 0))
                .thenReturn(findByLcEntryIdCheckList);

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 2))
                .thenReturn(new ArrayList<>());

        when(lcEntryValueRepositoryMock.findByLcEntryIdAndMediaIdAndLabelTaskId(LC_ENTRY_ID + 0, IMAGE_ID, LABEL_TASK_ID))
                .thenReturn(findByLcEntryIdCheckList);

        when(lcEntryValueRepositoryMock.findByLcEntryIdAndMediaIdAndLabelTaskId(LC_ENTRY_ID + 2, IMAGE_ID, LABEL_TASK_ID))
                .thenReturn(new ArrayList<>());

        when(lcEntryRepositoryMock.findAllClassificationLcEntriesWithLcEntryTypes(anyString(), anyList()))
                .thenReturn(globalClassificationLcEntries);

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        // Then
        List<LcEntryValueViewModel> globalClassificationsValues = lcEntryValueService
                .createGlobalClassificationsValuesGetRoots(LC_CONFIG_ID, createBindingModel);


        //Checklist
        LcEntryCheckListValue expectedCheckListValue = (LcEntryCheckListValue) globalClassificationLcEntryValues.get(0);
        ChecklistValueViewModel actualChecklistValueViewModel = (ChecklistValueViewModel) globalClassificationsValues.get(0);

        //Text
        LcEntryTextValue expectedTextValue = (LcEntryTextValue) expectedCheckListValue.getChildren().get(0);
        FreetextValueViewModel actualTextValueViewModel = (FreetextValueViewModel) actualChecklistValueViewModel.getChildren().get(0);

        //Select
        LcEntrySelectValue expectedSelectValue = (LcEntrySelectValue) globalClassificationLcEntryValues.get(1);
        SelectValueViewModel actualSelectValueViewModel = (SelectValueViewModel) globalClassificationsValues.get(1);


        assertNotNull(globalClassificationsValues);
        assertEquals(2, globalClassificationsValues.size());

        // Select
        assertEquals(expectedSelectValue.getLcEntry().getType().name(), actualSelectValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedSelectValue.getLabeler(), actualSelectValueViewModel.getLabeler());
        assertEquals(expectedSelectValue.getMedia().getId(), actualSelectValueViewModel.getMediaId());
        assertEquals(expectedSelectValue.getLcEntry().getId(), actualSelectValueViewModel.getLcEntryId());
        assertEquals(expectedSelectValue.getLcEntry().getConfiguration().getId(), actualSelectValueViewModel.getConfigurationId());
        assertEquals(expectedSelectValue.getChildren().size(), actualSelectValueViewModel.getChildren().size());

        // Text
        assertEquals(expectedTextValue.getLcEntry().getType().name(), actualTextValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedTextValue.getLabeler(), actualTextValueViewModel.getLabeler());
        assertEquals(expectedTextValue.getText(), actualTextValueViewModel.getText());
        assertEquals(expectedTextValue.getMedia().getId(), actualTextValueViewModel.getMediaId());
        assertEquals(expectedTextValue.getLcEntry().getId(), actualTextValueViewModel.getLcEntryId());
        assertEquals(expectedTextValue.getLcEntry().getConfiguration().getId(), actualTextValueViewModel.getConfigurationId());
        assertEquals(expectedTextValue.getChildren().size(), actualTextValueViewModel.getChildren().size());


        // Checklist
        assertEquals(expectedCheckListValue.getLcEntry().getType().name(), actualChecklistValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedCheckListValue.getLabeler(), actualChecklistValueViewModel.getLabeler());
        assertEquals(expectedCheckListValue.getCheckedValues().size(), actualChecklistValueViewModel.getCheckedValues().size());
        assertEquals(expectedCheckListValue.getMedia().getId(), actualChecklistValueViewModel.getMediaId());
        assertEquals(expectedCheckListValue.getLcEntry().getId(), actualChecklistValueViewModel.getLcEntryId());
        assertEquals(expectedCheckListValue.getLcEntry().getConfiguration().getId(), actualChecklistValueViewModel.getConfigurationId());
        assertEquals(expectedCheckListValue.getChildren().size(), actualChecklistValueViewModel.getChildren().size());
    }

    @Test
    void createGlobalClassificationsValues_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueCreateBindingModel createBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID + 0, LC_ITERATION_ID, IMAGE_ID, null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .createGlobalClassificationsValuesGetRoots(LC_CONFIG_ID, createBindingModel)
        );
    }

    @Test
    void createGlobalClassificationsValues_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel createBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID + 0, LC_ITERATION_ID, IMAGE_ID, null);

        testLabelIteration.getProject().setOwner("test_org");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .createGlobalClassificationsValuesGetRoots(LC_CONFIG_ID, createBindingModel)
        );
    }

    @Test
    void createGlobalClassificationsValues_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueCreateBindingModel createBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID + 0, LC_ITERATION_ID, IMAGE_ID, null);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .createGlobalClassificationsValuesGetRoots(LC_CONFIG_ID, createBindingModel)
        );
    }

    @Test
    void extendAllConfigEntryValues_whenInputsAreValidAndGlobalClassificationsSizeIsNotZero_extendAllConfigEntryValues() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        // LcEntry
        LcEntryChecklist testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryChecklist.setId(LC_ENTRY_ID + 0);

        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        testLcEntryText.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        testLcEntryChecklist.getChildren().add(testLcEntryText);
        testLcEntryText.setParentEntry(testLcEntryChecklist);

        List<LcEntry> globalClassificationLcEntries = new ArrayList<>() {{
            add(testLcEntryChecklist);
            add(testLcEntrySelect);
        }};

        // LcEntryValues
        LcEntryCheckListValue testLcEntryCheckListValue = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValue.setId(LC_ENTRY_VALUE_ID + 0);

        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValue.setId(LC_ENTRY_VALUE_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");

        testLcEntryCheckListValue.getChildren().add(testLcEntryTextValue);
        testLcEntryTextValue.setLcEntryValueParent(testLcEntryCheckListValue);

        List<LcEntryValue> globalClassificationLcEntryValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryChecklist));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryCheckListValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryTextValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        List<LcEntryValue> findByLcEntryIdCheckList = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
        }};

        List<LcEntryValue> findByLcEntryIdText = new ArrayList<>() {{
            add(testLcEntryTextValue);
        }};

        List<LcEntryValue> findByLcEntryIdSelect = new ArrayList<>() {{
            add(testLcEntrySelectValue);
        }};

        List<LcEntryValue> responseValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 0))
                .thenReturn(findByLcEntryIdCheckList);

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 1))
                .thenReturn(findByLcEntryIdText);

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 2))
                .thenReturn(findByLcEntryIdSelect);


        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(LC_CONFIG_ID))
                .thenReturn(globalClassificationLcEntries);


        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID))
                .thenReturn(responseValues);


        // Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel);

        System.out.println();
        //Checklist
        LcEntryCheckListValue expectedCheckListValue = (LcEntryCheckListValue) globalClassificationLcEntryValues.get(0);
        ChecklistValueViewModel actualChecklistValueViewModel = (ChecklistValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        //Text
        LcEntryTextValue expectedTextValue = (LcEntryTextValue) expectedCheckListValue.getChildren().get(0);
        FreetextValueViewModel actualTextValueViewModel = (FreetextValueViewModel) actualChecklistValueViewModel.getChildren().get(0);

        //Select
        LcEntrySelectValue expectedSelectValue = (LcEntrySelectValue) globalClassificationLcEntryValues.get(1);
        SelectValueViewModel actualSelectValueViewModel = (SelectValueViewModel) labelIterationViewModel.getEntryValues().get(1);


        assertNotNull(labelIterationViewModel);
        assertEquals(2, labelIterationViewModel.getEntryValues().size());

        // Select
        assertEquals(expectedSelectValue.getLcEntry().getType().name(), actualSelectValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedSelectValue.getLabeler(), actualSelectValueViewModel.getLabeler());
        assertEquals(expectedSelectValue.getMedia().getId(), actualSelectValueViewModel.getMediaId());
        assertEquals(expectedSelectValue.getLcEntry().getId(), actualSelectValueViewModel.getLcEntryId());
        assertEquals(expectedSelectValue.getLcEntry().getConfiguration().getId(), actualSelectValueViewModel.getConfigurationId());
        assertEquals(expectedSelectValue.getChildren().size(), actualSelectValueViewModel.getChildren().size());

        // Text
        assertEquals(expectedTextValue.getLcEntry().getType().name(), actualTextValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedTextValue.getLabeler(), actualTextValueViewModel.getLabeler());
        assertEquals(expectedTextValue.getText(), actualTextValueViewModel.getText());
        assertEquals(expectedTextValue.getMedia().getId(), actualTextValueViewModel.getMediaId());
        assertEquals(expectedTextValue.getLcEntry().getId(), actualTextValueViewModel.getLcEntryId());
        assertEquals(expectedTextValue.getLcEntry().getConfiguration().getId(), actualTextValueViewModel.getConfigurationId());
        assertEquals(expectedTextValue.getChildren().size(), actualTextValueViewModel.getChildren().size());


        // Checklist
        assertEquals(expectedCheckListValue.getLcEntry().getType().name(), actualChecklistValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedCheckListValue.getLabeler(), actualChecklistValueViewModel.getLabeler());
        assertEquals(expectedCheckListValue.getCheckedValues().size(), actualChecklistValueViewModel.getCheckedValues().size());
        assertEquals(expectedCheckListValue.getMedia().getId(), actualChecklistValueViewModel.getMediaId());
        assertEquals(expectedCheckListValue.getLcEntry().getId(), actualChecklistValueViewModel.getLcEntryId());
        assertEquals(expectedCheckListValue.getLcEntry().getConfiguration().getId(), actualChecklistValueViewModel.getConfigurationId());
        assertEquals(expectedCheckListValue.getChildren().size(), actualChecklistValueViewModel.getChildren().size());
    }

    @Test
    void extendAllConfigEntryValues_whenInputsAreValidAndEntryValuesAreMissing_extendAllConfigEntryValues() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        // LcEntry
        LcEntryChecklist testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryChecklist.setId(LC_ENTRY_ID + 0);

        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        testLcEntryText.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        testLcEntryChecklist.getChildren().add(testLcEntryText);
        testLcEntryText.setParentEntry(testLcEntryChecklist);

        List<LcEntry> globalClassificationLcEntries = new ArrayList<>() {{
            add(testLcEntryChecklist);
            add(testLcEntrySelect);
        }};

        // LcEntryValues
        LcEntryCheckListValue testLcEntryCheckListValue = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValue.setId(LC_ENTRY_VALUE_ID + 0);


        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValue.setId(LC_ENTRY_VALUE_ID + 1);

        testLcEntryCheckListValue.getChildren().add(testLcEntryTextValue);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");
        List<LcEntryValue> globalClassificationLcEntryValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryChecklist));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryCheckListValue));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        List<LcEntryValue> findByLcEntryIdCheckList = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
        }};

        List<LcEntryValue> findByLcEntryIdSelect = new ArrayList<>() {{
            add(testLcEntrySelectValue);
        }};

        List<LcEntryValue> responseValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 0))
                .thenReturn(findByLcEntryIdCheckList);

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 2))
                .thenReturn(findByLcEntryIdSelect);

        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(LC_CONFIG_ID))
                .thenReturn(globalClassificationLcEntries);

        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID))
                .thenReturn(responseValues);

        // Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel);

        //Checklist
        LcEntryCheckListValue expectedCheckListValue = (LcEntryCheckListValue) globalClassificationLcEntryValues.get(0);
        ChecklistValueViewModel actualChecklistValueViewModel = (ChecklistValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        //Text
        LcEntryTextValue expectedTextValue = (LcEntryTextValue) expectedCheckListValue.getChildren().get(0);
        FreetextValueViewModel actualTextValueViewModel = (FreetextValueViewModel) actualChecklistValueViewModel.getChildren().get(0);

        //Select
        LcEntrySelectValue expectedSelectValue = (LcEntrySelectValue) globalClassificationLcEntryValues.get(1);
        SelectValueViewModel actualSelectValueViewModel = (SelectValueViewModel) labelIterationViewModel.getEntryValues().get(1);

        assertNotNull(labelIterationViewModel);
        assertEquals(2, labelIterationViewModel.getEntryValues().size());

        // Select
        assertEquals(expectedSelectValue.getLcEntry().getType().name(), actualSelectValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedSelectValue.getLabeler(), actualSelectValueViewModel.getLabeler());
        assertEquals(expectedSelectValue.getMedia().getId(), actualSelectValueViewModel.getMediaId());
        assertEquals(expectedSelectValue.getLcEntry().getId(), actualSelectValueViewModel.getLcEntryId());
        assertEquals(expectedSelectValue.getLcEntry().getConfiguration().getId(), actualSelectValueViewModel.getConfigurationId());
        assertEquals(expectedSelectValue.getChildren().size(), actualSelectValueViewModel.getChildren().size());

        // Text
        assertEquals(expectedTextValue.getLcEntry().getType().name(), actualTextValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedTextValue.getLabeler(), actualTextValueViewModel.getLabeler());
        assertEquals(expectedTextValue.getText(), actualTextValueViewModel.getText());
        assertEquals(expectedTextValue.getMedia().getId(), actualTextValueViewModel.getMediaId());
        assertEquals(expectedTextValue.getLcEntry().getId(), actualTextValueViewModel.getLcEntryId());
        assertEquals(expectedTextValue.getLcEntry().getConfiguration().getId(), actualTextValueViewModel.getConfigurationId());
        assertEquals(expectedTextValue.getChildren().size(), actualTextValueViewModel.getChildren().size());

        // Checklist
        assertEquals(expectedCheckListValue.getLcEntry().getType().name(), actualChecklistValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedCheckListValue.getLabeler(), actualChecklistValueViewModel.getLabeler());
        assertEquals(expectedCheckListValue.getCheckedValues().size(), actualChecklistValueViewModel.getCheckedValues().size());
        assertEquals(expectedCheckListValue.getMedia().getId(), actualChecklistValueViewModel.getMediaId());
        assertEquals(expectedCheckListValue.getLcEntry().getId(), actualChecklistValueViewModel.getLcEntryId());
        assertEquals(expectedCheckListValue.getLcEntry().getConfiguration().getId(), actualChecklistValueViewModel.getConfigurationId());
        assertEquals(expectedCheckListValue.getChildren().size(), actualChecklistValueViewModel.getChildren().size());
    }

    @Test
    void extendAllConfigEntryValues_whenInputsAreValidAndNestedEntryValuesAreMissing_extendAllConfigEntryValues() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        // LcEntry
        LcEntryChecklist testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        testLcEntryChecklist.setId(LC_ENTRY_ID + 0);

        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        testLcEntryText.setId(LC_ENTRY_ID + 1);

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        testLcEntrySelect.setId(LC_ENTRY_ID + 2);

        testLcEntryChecklist.getChildren().add(testLcEntryText);
        testLcEntryText.setParentEntry(testLcEntryChecklist);

        List<LcEntry> globalClassificationLcEntries = new ArrayList<>() {{
            add(testLcEntryChecklist);
            add(testLcEntrySelect);
        }};

        // LcEntryValues
        LcEntryCheckListValue testLcEntryCheckListValue = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValue.setId(LC_ENTRY_VALUE_ID + 0);

        LcEntryTextValue testLcEntryTextValue = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValue.setId(LC_ENTRY_VALUE_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValue.setId(LC_ENTRY_VALUE_ID + 2);
        testLcEntrySelectValue.setSelectKey("selectKey");

        // Response LcEntryValues
        LcEntryCheckListValue testLcEntryCheckListValueResult = LcEntryValueUtils.createTestLcEntryCheckListValue(testLabelIteration, testMedia, testLcEntryChecklist);
        testLcEntryCheckListValueResult.setId(LC_ENTRY_VALUE_ID + 3);

        LcEntryTextValue testLcEntryTextValueResult = LcEntryValueUtils.createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);
        testLcEntryTextValueResult.setId(LC_ENTRY_VALUE_ID + 4);

        LcEntrySelectValue testLcEntrySelectValueResult = LcEntryValueUtils.createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);
        testLcEntrySelectValueResult.setId(LC_ENTRY_VALUE_ID + 5);
        testLcEntrySelectValueResult.setSelectKey("selectKey");

        testLcEntryCheckListValueResult.getChildren().add(testLcEntryTextValueResult);
        testLcEntryTextValueResult.setLcEntryValueParent(testLcEntryCheckListValueResult);


        List<LcEntryValue> globalClassificationLcEntryValues = new ArrayList<>() {{
            add(testLcEntryCheckListValue);
            add(testLcEntrySelectValue);
        }};

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelConfigurationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelConfiguration));

        // LcEntry
        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryChecklist));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 1))
                .thenReturn(java.util.Optional.of(testLcEntryText));

        when(lcEntryRepositoryMock.findById(LC_ENTRY_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        // LcEntryValue
        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 0))
                .thenReturn(java.util.Optional.of(testLcEntryCheckListValue));


        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID + 2))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryCheckListValue.class)))
                .thenReturn(testLcEntryCheckListValue);

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntryTextValue.class)))
                .thenReturn(testLcEntryTextValue);

        when(lcEntryValueRepositoryMock.saveAndFlush(any(LcEntrySelectValue.class)))
                .thenReturn(testLcEntrySelectValue);

        List<LcEntryValue> findByLcEntryIdSelect = new ArrayList<>() {{
            add(testLcEntrySelectValue);
        }};

        List<LcEntryValue> responseValues = new ArrayList<>() {{
            add(testLcEntryCheckListValueResult);
            add(testLcEntrySelectValueResult);
        }};

        when(lcEntryValueRepositoryMock.findByLcEntryId(LC_ENTRY_ID + 2))
                .thenReturn(findByLcEntryIdSelect);


        when(lcEntryRepositoryMock.findAllByParentEntryIsNullAndConfigurationId(LC_CONFIG_ID))
                .thenReturn(globalClassificationLcEntries);


        when(lcEntryValueRepositoryMock
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(LC_ITERATION_ID, IMAGE_ID, LABEL_TASK_ID))
                .thenReturn(responseValues);


        // Then
        LabelIterationViewModel labelIterationViewModel = lcEntryValueService
                .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel);

        //Checklist
        LcEntryCheckListValue expectedCheckListValue = (LcEntryCheckListValue) globalClassificationLcEntryValues.get(0);
        ChecklistValueViewModel actualChecklistValueViewModel = (ChecklistValueViewModel) labelIterationViewModel.getEntryValues().get(0);

        //Text
        LcEntryTextValue expectedTextValue = (LcEntryTextValue) testLcEntryTextValue;
        FreetextValueViewModel actualTextValueViewModel = (FreetextValueViewModel) actualChecklistValueViewModel.getChildren().get(0);

        //Select
        LcEntrySelectValue expectedSelectValue = (LcEntrySelectValue) globalClassificationLcEntryValues.get(1);
        SelectValueViewModel actualSelectValueViewModel = (SelectValueViewModel) labelIterationViewModel.getEntryValues().get(1);


        assertNotNull(labelIterationViewModel);
        assertEquals(2, labelIterationViewModel.getEntryValues().size());

        // Select
        assertEquals(expectedSelectValue.getLcEntry().getType().name(), actualSelectValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedSelectValue.getLabeler(), actualSelectValueViewModel.getLabeler());
        assertEquals(expectedSelectValue.getMedia().getId(), actualSelectValueViewModel.getMediaId());
        assertEquals(expectedSelectValue.getLcEntry().getId(), actualSelectValueViewModel.getLcEntryId());
        assertEquals(expectedSelectValue.getLcEntry().getConfiguration().getId(), actualSelectValueViewModel.getConfigurationId());
        assertEquals(expectedSelectValue.getChildren().size(), actualSelectValueViewModel.getChildren().size());

        // Text
        assertEquals(expectedTextValue.getLcEntry().getType().name(), actualTextValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedTextValue.getLabeler(), actualTextValueViewModel.getLabeler());
        assertEquals(expectedTextValue.getText(), actualTextValueViewModel.getText());
        assertEquals(expectedTextValue.getMedia().getId(), actualTextValueViewModel.getMediaId());
        assertEquals(expectedTextValue.getLcEntry().getId(), actualTextValueViewModel.getLcEntryId());
        assertEquals(expectedTextValue.getLcEntry().getConfiguration().getId(), actualTextValueViewModel.getConfigurationId());
        assertEquals(expectedTextValue.getChildren().size(), actualTextValueViewModel.getChildren().size());


        // Checklist
        assertEquals(expectedCheckListValue.getLcEntry().getType().name(), actualChecklistValueViewModel.getEntryTypeLcEntry());
        assertEquals(expectedCheckListValue.getLabeler(), actualChecklistValueViewModel.getLabeler());
        assertEquals(expectedCheckListValue.getCheckedValues().size(), actualChecklistValueViewModel.getCheckedValues().size());
        assertEquals(expectedCheckListValue.getMedia().getId(), actualChecklistValueViewModel.getMediaId());
        assertEquals(expectedCheckListValue.getLcEntry().getId(), actualChecklistValueViewModel.getLcEntryId());
        assertEquals(expectedCheckListValue.getLcEntry().getConfiguration().getId(), actualChecklistValueViewModel.getConfigurationId());
    }

    @Test
    void extendAllConfigEntryValues_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);
        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel)
        );
    }

    @Test
    void extendAllConfigEntryValues_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        testLabelIteration.getProject().setOwner("test_org");

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel)
        );
    }

    @Test
    void extendAllConfigEntryValues_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Given
        LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendAllBindingModel(LC_ITERATION_ID, IMAGE_ID);

        //When
        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .extendAllConfigEntryValues(LC_CONFIG_ID, lcEntryValueExtendAllBindingModel)
        );
    }

    @Test
    void createLcEntryValueTree_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueCreateBindingModel testLcEntryValueCreateBindingModel = LcEntryValueUtils
                .createTestLcEntryValueCreateBindingModel(LC_ENTRY_ID, LC_ITERATION_ID, IMAGE_ID, null);
        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .createLcEntryValueTree(LC_ENTRY_ID, testLcEntryValueCreateBindingModel)
        );
    }


    @Test
    void extendValueTree_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueExtendBindingModel testLcEntryValueExtendBindingModel = LcEntryValueUtils
                .createTestLcEntryValueExtendBindingModel(LC_ITERATION_ID, IMAGE_ID, LC_ENTRY_ID, null);
        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService
                        .extendValueTree(LC_ENTRY_ID, testLcEntryValueExtendBindingModel)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenInputsAreValidAndGeometryIsPolygon_createNewLcEntryGeometryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryPolygon newTestLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);
        newTestLcEntryPolygon.setId(LC_ENTRY_ID + 1);

        LcEntryPolygonValue testLcEntryPolyValue = LcEntryValueUtils
                .createTestLcEntryPolyValue(testLabelIteration, testMedia, testLcEntryPolygon);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryPolygon));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPolyValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any()))
                .thenReturn(testLcEntryPolyValue);

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        PolygonValueViewModel polygonValueViewModel = (PolygonValueViewModel) lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel);

        //Then
        assertNotNull(polygonValueViewModel);
        assertEquals(IMAGE_ID, polygonValueViewModel.getMediaId());
        assertEquals(LC_ITERATION_ID, polygonValueViewModel.getLabelIterationId());
        assertEquals("blue", polygonValueViewModel.getColor());
        assertEquals("3", polygonValueViewModel.getShortcut());
        assertEquals(4, polygonValueViewModel.getPoints().size());
        assertEquals("POLYGON", polygonValueViewModel.getEntryTypeLcEntry());

    }

    @Test
    void changeTypeOfSingleLabelValue_whenInputsAreValidAndGeometryIsline_createNewLcEntryGeometryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryLine newTestLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        newTestLcEntryLine.setId(LC_ENTRY_ID + 1);


        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils
                .createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryLine));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any()))
                .thenReturn(testLcEntryLineValue);

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        LineValueViewModel lineValueViewModel = (LineValueViewModel) lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel);

        //Then
        assertNotNull(lineValueViewModel);
        assertEquals(IMAGE_ID, lineValueViewModel.getMediaId());
        assertEquals(LC_ITERATION_ID, lineValueViewModel.getLabelIterationId());
        assertEquals("blue", lineValueViewModel.getColor());
        assertEquals("1", lineValueViewModel.getShortcut());
        assertEquals("LINE", lineValueViewModel.getEntryTypeLcEntry());
    }

    @Test
    void changeTypeOfSingleLabelValue_whenInputsAreValidAndGeometryIsRectangle_createNewLcEntryGeometryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils
                .createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryRectangle newTestLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        newTestLcEntryRectangle.setId(LC_ENTRY_ID + 1);

        LcEntryRectangleValue testLcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia, testLcEntryRectangle);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryRectangle));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryRectangleValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any()))
                .thenReturn(testLcEntryRectangleValue);

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        RectangleValueViewModel rectangleValueViewModel = (RectangleValueViewModel) lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel);

        //Then
        assertNotNull(rectangleValueViewModel);
        assertEquals(IMAGE_ID, rectangleValueViewModel.getMediaId());
        assertEquals(LC_ITERATION_ID, rectangleValueViewModel.getLabelIterationId());
        assertEquals("blue", rectangleValueViewModel.getColor());
        assertEquals("4", rectangleValueViewModel.getShortcut());
        assertEquals("RECTANGLE", rectangleValueViewModel.getEntryTypeLcEntry());

    }

    @Test
    void changeTypeOfSingleLabelValue_whenInputsAreValidAndGeometryIsPont_createNewLcEntryGeometryValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils
                .createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryPoint newTestLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);
        newTestLcEntryPoint.setId(LC_ENTRY_ID + 1);

        LcEntryPointValue testLcEntryPointValue = LcEntryValueUtils
                .createTestLcEntryPointValue(testLabelIteration, testMedia, testLcEntryPoint);

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryPoint));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPointValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any()))
                .thenReturn(testLcEntryPointValue);

        when(labelIterationRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLabelIteration));

        when(mediaRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        when(labelTaskRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(labelTask));

        when(lcEntryValueRepositoryMock.save(any(LcEntryValue.class)))
                .then(returnsFirstArg());

        PointValueViewModel pointValueViewModel = (PointValueViewModel) lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel);

        //Then
        assertNotNull(pointValueViewModel);
        assertEquals(IMAGE_ID, pointValueViewModel.getMediaId());
        assertEquals(LC_ITERATION_ID, pointValueViewModel.getLabelIterationId());
        assertEquals("blue", pointValueViewModel.getColor());
        assertEquals("2", pointValueViewModel.getShortcut());
        assertEquals("POINT", pointValueViewModel.getEntryTypeLcEntry());
    }

    @Test
    void changeTypeOfSingleLabelValue_whenNewLcEntryTypeIsNotGeometry_throwException() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        LcEntryPolygonValue testLcEntryPolyValue = LcEntryValueUtils
                .createTestLcEntryPolyValue(testLabelIteration, testMedia, testLcEntryPolygon);
        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(testLcEntrySelect));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPolyValue));

        when(lcEntryValueRepositoryMock.saveAndFlush(any()))
                .thenReturn(testLcEntryPolyValue);

        assertThrows(GenericException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );

    }


    @Test
    void changeTypeOfSingleLabelValue_whenOldLcEntryTypeIsNotGeometry_throwException() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryPolygon newTestLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);
        newTestLcEntryPolygon.setId(LC_ENTRY_ID + 1);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils
                .createTestLcEntrySelectValue(testLabelIteration, testMedia, testLcEntrySelect);

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryPolygon));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntrySelectValue));

        assertThrows(GenericException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );

    }

    @Test
    void changeTypeOfSingleLabelValue_whenOldLcEntryTypeIsNotEqualNewLcEntryType_throwException() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryPolygon newTestLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);
        newTestLcEntryPolygon.setId(LC_ENTRY_ID + 1);

        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils
                .createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryPolygon));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        assertThrows(GenericException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenOldLcEntryIsTheSameAsNewLcEntry_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryPolygon newTestLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);

        LcEntryPolygonValue testLcEntryPolyValue = LcEntryValueUtils
                .createTestLcEntryPolyValue(testLabelIteration, testMedia, newTestLcEntryPolygon);

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryPolygon));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryPolyValue));

        assertThrows(GenericException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }


    @Test
    void changeTypeOfSingleLabelValue_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        // Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenUserIsNotInOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryLine newTestLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        newTestLcEntryLine.setId(LC_ENTRY_ID + 1);


        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils
                .createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);

        Project project = testLcEntryLineValue.getLabelIteration().getProject();
        project.setOwner("test_org");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryLine));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    @Test
    void changeTypeOfSingleLabelValue_whenIdIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        //Given
        LcEntryValueChangeValueClassBindingModel changeValueClassBindingModel = LcEntryValueUtils.createTestLcEntryValueChangeValueClassBindingModel();

        LcEntryLine newTestLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        newTestLcEntryLine.setId(LC_ENTRY_ID + 1);


        LcEntryLineValue testLcEntryLineValue = LcEntryValueUtils
                .createTestLcEntryLineValue(testLabelIteration, testMedia, testLcEntryLine);

        Project project = testLcEntryLineValue.getLabelIteration().getProject();
        project.setOwner("test_org");

        //When
        when(lcEntryRepositoryMock.findById(anyString()))
                .thenReturn(java.util.Optional.of(newTestLcEntryLine));

        when(lcEntryValueRepositoryMock.findById(LC_ENTRY_VALUE_ID))
                .thenReturn(java.util.Optional.of(testLcEntryLineValue));

        //Then
        assertThrows(ForbiddenException.class,
                () -> lcEntryValueService.changeTypeOfSingleLabelValue(LC_ENTRY_VALUE_ID, changeValueClassBindingModel)
        );
    }

    private List<LcEntryValueUpdateBindingModel> createLcEntryValueUpdateBindingModels() {
        // LcEntryValueUpdateBindingModel
        List<LcEntryValueUpdateBindingModel> testLcEntryValueUpdateBindingModels = LcEntryValueUtils.createTestLcEntryValueUpdateBindingModels(8);

        // Create RectangleBindingModel
        LcEntryValueUpdateBindingModel firstLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(0);
        firstLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 0);
        firstLcEntryValueUpdateBindingModel.setX(0.5);
        firstLcEntryValueUpdateBindingModel.setY(0.5);
        firstLcEntryValueUpdateBindingModel.setWidth(0.5);
        firstLcEntryValueUpdateBindingModel.setHeight(0.5);

        // Create LineBindingModel
        LcEntryValueUpdateBindingModel secondLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(1);
        secondLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 1);

        List<PointPojoBindingModel> pointPojoBindingModels = PointPojoUtils.createPointPojoBindingModels(2);
        secondLcEntryValueUpdateBindingModel.setPoints(pointPojoBindingModels);

        // Create SelectBindingModel
        LcEntryValueUpdateBindingModel fourthLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(3);
        fourthLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 3);
        fourthLcEntryValueUpdateBindingModel.setSelectKey("selectKey");

        // Create TextBindingModel
        LcEntryValueUpdateBindingModel fifthLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(4);
        fifthLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 4);
        fifthLcEntryValueUpdateBindingModel.setText("text");

        // Create PointBindingModel
        LcEntryValueUpdateBindingModel sixthLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(5);
        sixthLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 5);
        sixthLcEntryValueUpdateBindingModel.setX(0.5);
        sixthLcEntryValueUpdateBindingModel.setY(0.5);

        // Create PolygonBindingModel
        LcEntryValueUpdateBindingModel seventhLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(6);
        seventhLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 6);

        List<PointPojoBindingModel> pointPojoBindingModelsPolygon = PointPojoUtils.createPointPojoBindingModels(3);
        seventhLcEntryValueUpdateBindingModel.setPoints(pointPojoBindingModelsPolygon);

        // Create ChecklistBindingModel
        LcEntryValueUpdateBindingModel eighthLcEntryValueUpdateBindingModel = testLcEntryValueUpdateBindingModels.get(7);
        eighthLcEntryValueUpdateBindingModel.setLcEntryId(LC_ENTRY_ID + 7);
        List<String> checkedValues = new ArrayList<>() {{
            add("value1");
            add("value2");
        }};
        eighthLcEntryValueUpdateBindingModel.setCheckedValues(checkedValues);


        firstLcEntryValueUpdateBindingModel.getChildren().add(fourthLcEntryValueUpdateBindingModel);
        fourthLcEntryValueUpdateBindingModel.setParentEntry(firstLcEntryValueUpdateBindingModel);

        //secondLcEntryValueUpdateBindingModel.getChildren().add(fourthLcEntryValueUpdateBindingModel);
        //fourthLcEntryValueUpdateBindingModel.setParentEntry(secondLcEntryValueUpdateBindingModel);

        sixthLcEntryValueUpdateBindingModel.getChildren().add(fifthLcEntryValueUpdateBindingModel);
        fifthLcEntryValueUpdateBindingModel.setParentEntry(sixthLcEntryValueUpdateBindingModel);

        seventhLcEntryValueUpdateBindingModel.getChildren().add(eighthLcEntryValueUpdateBindingModel);
        eighthLcEntryValueUpdateBindingModel.setParentEntry(seventhLcEntryValueUpdateBindingModel);

        List<LcEntryValueUpdateBindingModel> valueUpdateBindingModels = new ArrayList<>() {{
            add(firstLcEntryValueUpdateBindingModel);
            add(secondLcEntryValueUpdateBindingModel);
            add(sixthLcEntryValueUpdateBindingModel);
            add(seventhLcEntryValueUpdateBindingModel);
        }};

        return valueUpdateBindingModels;
    }
}