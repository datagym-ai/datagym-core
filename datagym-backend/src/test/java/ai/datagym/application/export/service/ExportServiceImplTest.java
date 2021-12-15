package ai.datagym.application.export.service;

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
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class ExportServiceImplTest {

    private ExportService exportService;

    private MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private LcEntryRepository lcEntryRepositoryMock;

    @Mock
    private ObjectMapper objectMapperMock;

    @Mock
    private JsonGenerator jsonGeneratorMock;

    @Mock
    private JsonFactory jsonFactoryMock;

    @BeforeEach
    void setUp() {
        exportService = new ExportServiceImpl(labelTaskRepositoryMock, lcEntryValueRepositoryMock, lcEntryRepositoryMock, objectMapperMock);
    }

    @Test
    void exportJsonLabelsByProject_WhenProjectIsValid_exportJsonLabelsByProject() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        // LabelTask
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(3, loggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.COMPLETED);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.SKIPPED);
        testLabelTaskList.get(2).setLabelTaskState(LabelTaskState.REVIEWED);

        // LcEntryValues
        List<LcEntryValue> rootClassificationValues = new ArrayList<>();
        createClassificationLcEntryValues(rootClassificationValues);

        List<LcEntryValue> rootGeometryValues = new ArrayList<>();
        createGeometryLcEntryValues(rootGeometryValues);

        // LcEntry
        List<LcEntry> lcEntriesList = new ArrayList<>();
        createLcEntries(lcEntriesList);

        //When
        when(objectMapperMock.getFactory())
                .thenReturn(jsonFactoryMock);

        when(jsonFactoryMock.createGenerator(httpServletResponseMock.getOutputStream()))
                .thenReturn(jsonGeneratorMock);

        when(labelTaskRepositoryMock.findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelTaskList);

        when(lcEntryRepositoryMock.findAllByConfigurationId(anyString()))
                .thenReturn(lcEntriesList);

        when(lcEntryValueRepositoryMock
                .getAllRootValuesFromType(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues);

        exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock);

        verify(labelTaskRepositoryMock).findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean());
        verify(labelTaskRepositoryMock, times(1)).findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean());
    }

    @Test
    void exportJsonLabelsByProject_WhenProjectIsValidAndValuesAreNull_exportJsonLabelsByProject() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        // LabelTask
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(3, loggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.COMPLETED);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.SKIPPED);
        testLabelTaskList.get(2).setLabelTaskState(LabelTaskState.REVIEWED);

        // LcEntryValues
        List<LcEntryValue> rootClassificationValues = new ArrayList<>();
        createClassificationLcEntryValues(rootClassificationValues);

        List<LcEntryValue> rootGeometryValues = new ArrayList<>();
        createGeometryLcEntryValues(rootGeometryValues);
        createGeometryvaluesWithNullPointValues(rootGeometryValues);

        // LcEntry
        List<LcEntry> lcEntriesList = new ArrayList<>();
        createLcEntries(lcEntriesList);

        //When
        when(objectMapperMock.getFactory())
                .thenReturn(jsonFactoryMock);

        when(jsonFactoryMock.createGenerator(httpServletResponseMock.getOutputStream()))
                .thenReturn(jsonGeneratorMock);

        when(labelTaskRepositoryMock.findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelTaskList);

        when(lcEntryRepositoryMock.findAllByConfigurationId(anyString()))
                .thenReturn(lcEntriesList);

        when(lcEntryValueRepositoryMock
                .getAllRootValuesFromType(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues);

        exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock);

        verify(labelTaskRepositoryMock).findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean());
        verify(labelTaskRepositoryMock, times(1)).findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean());
    }

    @Test
    void exportJsonLabelsByProject_WhenClassificationTypeNotExists_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        // LabelTask
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(3, loggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.COMPLETED);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.SKIPPED);
        testLabelTaskList.get(2).setLabelTaskState(LabelTaskState.REVIEWED);

        // LcEntryValues
        List<LcEntryValue> rootClassificationValues = new ArrayList<>();
        createClassificationLcEntryValues(rootClassificationValues);

        LcEntryValue lcEntryValue = rootClassificationValues.get(0);
        lcEntryValue.getLcEntry().setType(LcEntryType.RECTANGLE);

        List<LcEntryValue> rootGeometryValues = new ArrayList<>();
        createGeometryLcEntryValues(rootGeometryValues);

        // LcEntry
        List<LcEntry> lcEntriesList = new ArrayList<>();
        createLcEntries(lcEntriesList);

        //When
        when(objectMapperMock.getFactory())
                .thenReturn(jsonFactoryMock);

        when(jsonFactoryMock.createGenerator(httpServletResponseMock.getOutputStream()))
                .thenReturn(jsonGeneratorMock);

        when(labelTaskRepositoryMock.findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelTaskList);

        when(lcEntryRepositoryMock.findAllByConfigurationId(anyString()))
                .thenReturn(lcEntriesList);

        when(lcEntryValueRepositoryMock
                .getAllRootValuesFromType(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues);

        //Then
        assertThrows(GenericException.class,
                () ->  exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock)
        );
    }

    @Test
    void exportJsonLabelsByProject_WhenGeometryTypeNotExists_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        // LabelTask
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskList(3, loggedInUserId);
        testLabelTaskList.get(0).setLabelTaskState(LabelTaskState.COMPLETED);
        testLabelTaskList.get(1).setLabelTaskState(LabelTaskState.SKIPPED);
        testLabelTaskList.get(2).setLabelTaskState(LabelTaskState.REVIEWED);

        // LcEntryValues
        List<LcEntryValue> rootClassificationValues = new ArrayList<>();
        createClassificationLcEntryValues(rootClassificationValues);

        List<LcEntryValue> rootGeometryValues = new ArrayList<>();
        createGeometryLcEntryValues(rootGeometryValues);

        LcEntryValue lcEntryValue = rootGeometryValues.get(0);
        lcEntryValue.getLcEntry().setType(LcEntryType.SELECT);

        // LcEntry
        List<LcEntry> lcEntriesList = new ArrayList<>();
        createLcEntries(lcEntriesList);

        //When
        when(objectMapperMock.getFactory())
                .thenReturn(jsonFactoryMock);

        when(jsonFactoryMock.createGenerator(httpServletResponseMock.getOutputStream()))
                .thenReturn(jsonGeneratorMock);

        when(labelTaskRepositoryMock.findTasksByProjectIdAndTaskStateAndMediaDeleted(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelTaskList);

        when(lcEntryRepositoryMock.findAllByConfigurationId(anyString()))
                .thenReturn(lcEntriesList);

        when(lcEntryValueRepositoryMock
                .getAllRootValuesFromType(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues, rootClassificationValues, rootGeometryValues);

        //Then
        assertThrows(GenericException.class,
                () ->  exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock)
        );
    }

    @Test
    void exportJsonLabelsByProject_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(null);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        //Then
        assertThrows(ForbiddenException.class,
                () ->  exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock)
        );
    }

    @Test
    void exportJsonLabelsByProject_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //Then
        assertThrows(ForbiddenException.class,
                () ->  exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock)
        );
    }

    @Test
    void exportJsonLabelsByProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("testOrg");

        //Then
        assertThrows(ForbiddenException.class,
                () ->   exportService.exportJsonLabelsByProject(testProject, httpServletResponseMock)
        );
    }

    private void createGeometryLcEntryValues(List<LcEntryValue> rootGeometryValues) {
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        // Create Polygon Value
        Media testMedia1 = ImageUtils.createTestImage(IMAGE_ID + 1);
        LcEntryPolygon lcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);

        LcEntryPolygonValue lcEntryPolygonValue = LcEntryValueUtils
                .createTestLcEntryPolyValue(testLabelIteration, testMedia1, lcEntryPolygon);

        rootGeometryValues.add(lcEntryPolygonValue);

        // Create Rectangle Value
        Media testMedia2 = ImageUtils.createTestImage(IMAGE_ID + 2);
        LcEntryRectangle lcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);

        LcEntryRectangleValue lcEntryRectangleValue = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia2, lcEntryRectangle);

        rootGeometryValues.add(lcEntryRectangleValue);

        // Create Point Value
        Media testMedia3 = ImageUtils.createTestImage(IMAGE_ID + 3);
        LcEntryPoint lcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);

        LcEntryPointValue lcEntryPointValue = LcEntryValueUtils
                .createTestLcEntryPointValue(testLabelIteration, testMedia3, lcEntryPoint);

        rootGeometryValues.add(lcEntryPointValue);

        // Create Line Value
        Media testMedia4 = ImageUtils.createTestImage(IMAGE_ID + 4);
        LcEntryLine lcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);

        LcEntryLineValue lcEntryLineValue = LcEntryValueUtils
                .createTestLcEntryLineValue(testLabelIteration, testMedia4, lcEntryLine);

        rootGeometryValues.add(lcEntryLineValue);


    }

    private void createGeometryvaluesWithNullPointValues(List<LcEntryValue> rootGeometryValues) {
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);


        // Create Polygon with null Points-Values
        Media testMedia5 = ImageUtils.createTestImage(IMAGE_ID + 5);
        LcEntryPolygon lcEntryPolygonWithNullPoints = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);

        LcEntryPolygonValue lcEntryPolygonValueWithNullPoints = LcEntryValueUtils
                .createTestLcEntryPolyValue(testLabelIteration, testMedia5, lcEntryPolygonWithNullPoints);

        List<PointPojo> points = lcEntryPolygonValueWithNullPoints.getPoints();
        for (PointPojo point : points) {
            point.setX(null);
            point.setY(null);
        }
        rootGeometryValues.add(lcEntryPolygonValueWithNullPoints);

        // Create Rectangle with null Points-Values
        Media testMedia6 = ImageUtils.createTestImage(IMAGE_ID + 6);
        LcEntryRectangle lcEntryRectangleWithNullPoints = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);

        LcEntryRectangleValue lcEntryRectangleValueWithNullPoints = LcEntryValueUtils
                .createTestLcEntryRectangleValue(testLabelIteration, testMedia6, lcEntryRectangleWithNullPoints);

        lcEntryRectangleValueWithNullPoints.setX(null);
        lcEntryRectangleValueWithNullPoints.setY(null);
        lcEntryRectangleValueWithNullPoints.setWidth(null);
        lcEntryRectangleValueWithNullPoints.setHeight(null);

        rootGeometryValues.add(lcEntryRectangleValueWithNullPoints);
    }

    private void createClassificationLcEntryValues(List<LcEntryValue> rootClassificationValues) {
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelIteration testLabelIteration = LabelIterationUtils.createTestLabelIteration(testProject);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        // Create Select Value
        Media testMedia3 = ImageUtils.createTestImage(IMAGE_ID + 1);
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);

        LcEntrySelectValue testLcEntrySelectValue = LcEntryValueUtils
                .createTestLcEntrySelectValue(testLabelIteration, testMedia3, testLcEntrySelect);

        // Create Checklist Value
        Media testMedia2 = ImageUtils.createTestImage(IMAGE_ID + 2);
        LcEntryChecklist lcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);

        LcEntryCheckListValue lcEntryCheckListValue = LcEntryValueUtils
                .createTestLcEntryCheckListValue(testLabelIteration, testMedia2, lcEntryChecklist);

        testLcEntrySelectValue.getChildren().add(lcEntryCheckListValue);
        rootClassificationValues.add(testLcEntrySelectValue);

        // Create Freetext Value
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID + 4);
        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);

        LcEntryTextValue lcEntryTextValue = LcEntryValueUtils
                .createTestLcEntryTextValue(testLabelIteration, testMedia, testLcEntryText);

    }

    private void createLcEntries(List<LcEntry> lcEntriesList) {
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        LabelConfiguration testLabelConfiguration = LabelConfigurationUtils.createTestLabelConfiguration(testProject);

        // Polygon
        LcEntryPolygon testLcEntryPolygon = LcEntryUtils.createTestLcEntryPolygon(testLabelConfiguration);
        lcEntriesList.add(testLcEntryPolygon);

        // Point
        LcEntryPoint testLcEntryPoint = LcEntryUtils.createTestLcEntryPoint(testLabelConfiguration);
        lcEntriesList.add(testLcEntryPoint);

        // Line
        LcEntryLine testLcEntryLine = LcEntryUtils.createTestLcEntryLine(testLabelConfiguration);
        lcEntriesList.add(testLcEntryLine);

        // Rectangle
        LcEntryRectangle testLcEntryRectangle = LcEntryUtils.createTestLcEntryRectangle(testLabelConfiguration);
        lcEntriesList.add(testLcEntryRectangle);

        // Select
        LcEntrySelect testLcEntrySelect = LcEntryUtils.createTestLcEntrySelect(testLabelConfiguration);
        lcEntriesList.add(testLcEntrySelect);

        // Checklist
        LcEntryChecklist testLcEntryChecklist = LcEntryUtils.createTestLcEntryChecklist(testLabelConfiguration);
        lcEntriesList.add(testLcEntryChecklist);

        // Checklist Text
        LcEntryFreeText testLcEntryText = LcEntryUtils.createTestLcEntryText(testLabelConfiguration);
        lcEntriesList.add(testLcEntryText);
    }
}