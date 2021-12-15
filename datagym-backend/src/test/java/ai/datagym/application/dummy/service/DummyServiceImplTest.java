package ai.datagym.application.dummy.service;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.dummy.models.bindingModels.labelConfiguration.DummyConfigBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyValueUpdateBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelTask.DummyLabelTaskBindingModel;
import ai.datagym.application.dummy.models.bindingModels.media.DummyMediaViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyDatasetViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyProjectBindingModel;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.UrlImage;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.service.ProjectService;
import ai.datagym.application.testUtils.*;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.datagym.application.testUtils.DummyUtils.*;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class DummyServiceImplTest {

    private DummyService dummyService;

    @Mock
    private ProjectService projectServiceMock;

    @Mock
    private DatasetService datasetServiceMock;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private LcEntryValueRepository lcEntryValueRepositoryMock;

    @Mock
    private LcEntryValueService lcEntryValueServiceMock;

    @Mock
    private LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    private ObjectMapper objectMapperMock;

    @Mock
    private ModelMapper modelMapperMock;


    @BeforeEach
    void setUp() {
        dummyService = new DummyServiceImpl(
                projectServiceMock,
                datasetServiceMock,
                labelTaskRepositoryMock,
                lcEntryValueRepositoryMock,
                lcEntryValueServiceMock,
                labelConfigurationServiceMock,
                objectMapperMock,
                modelMapperMock);
    }


    private static void setImageUrls(List<LabelTask> labelTasks) {
        UrlImage urlImage1 = (UrlImage) labelTasks.get(0).getMedia();
        urlImage1.setUrl(IMAGE_URL_ONE);

        UrlImage urlImage2 = (UrlImage) labelTasks.get(1).getMedia();
        urlImage2.setUrl(IMAGE_URL_TWO);

        UrlImage urlImage3 = (UrlImage) labelTasks.get(2).getMedia();
        urlImage3.setUrl(IMAGE_URL_THREE);

        UrlImage urlImage4 = (UrlImage) labelTasks.get(3).getMedia();
        urlImage4.setUrl(IMAGE_URL_FOUR);

        UrlImage urlImage5 = (UrlImage) labelTasks.get(4).getMedia();
        urlImage5.setUrl(IMAGE_URL_FIVE);

        UrlImage urlImage6 = (UrlImage) labelTasks.get(5).getMedia();
        urlImage6.setUrl(IMAGE_URL_SIX);

        UrlImage urlImage7 = (UrlImage) labelTasks.get(6).getMedia();
        urlImage7.setUrl(IMAGE_URL_SEVEN);
    }

    @Test
    void createDummyDataForOrg_whenProjectAlreadyExists_throwException() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IOException, IllegalAccessException, ClassNotFoundException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        DummyProjectBindingModel testDummyProjectBindingModel = DummyUtils.createTestDummyProjectBindingModel(PROJECT_ID);
        DummyDatasetViewModel testDummyDatasetViewModel = DummyUtils.createTestDummyDatasetViewModel();
        testDummyProjectBindingModel.getDatasets().add(testDummyDatasetViewModel);

        DummyConfigBindingModel testDummyConfigBindingModel = DummyUtils.createTestDummyConfigBindingModel();
        DummyLabelTaskBindingModel[] testLabelModeDataViewModelArr = DummyUtils.createTestLabelModeDataViewModelArr(2);

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenReturn(testDummyProjectBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyConfigBindingModel.class)))
                .thenReturn(testDummyConfigBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenReturn(testLabelModeDataViewModelArr);

        //Then
        assertThrows(AlreadyExistsException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }

    @Test
    void createDummyDataForOrg_whenOrgIdIsValidAndUserIsAuthenticatedAndAdmin_createDummyDataForOrg() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IOException, IllegalAccessException, ClassNotFoundException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);
        DatasetViewModel firstDatasetViewModel = testListDatasetViewModel.get(0);

        // Task
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskListWithUrlImages(7, loggedInUserId);

        setImageUrls(testLabelTaskList);

        // LabelConfiguration
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> testLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(8);
        List<LcEntryViewModel> lcEntryViewModels = updateLcEntryViewModels(testLcEntryViewModels);

        testLabelConfigurationViewModel.setEntries(lcEntryViewModels);

        // LcEntryValueViewModel
        List<LcEntryValueViewModel> lcEntryValueViewModelList = LcEntryValueUtils.createLcEntryValueViewModelList(8);
        List<LcEntryValueViewModel> lcEntryValueViewModels = updateLcEntryValueViewModelList(lcEntryValueViewModelList);

        LcEntryValueViewModel rectangleLcEntryValueViewModel = lcEntryValueViewModels.get(0);
        LcEntryValueViewModel firstPolyLcEntryValueViewModel = lcEntryValueViewModels.get(1);
        LcEntryValueViewModel secondPolyLcEntryValueViewModel = lcEntryValueViewModels.get(2);
        LcEntryValueViewModel firstClassLcEntryValueViewModel = lcEntryValueViewModels.get(3);

        DummyProjectBindingModel testDummyProjectBindingModel = DummyUtils.createTestDummyProjectBindingModel(PROJECT_ID);
        DummyDatasetViewModel testDummyDatasetViewModel = DummyUtils.createTestDummyDatasetViewModel();
        DummyDatasetViewModel[] dummyDatasetViewModelsArray= DummyUtils.createTestDummyDatasetViewModels(1);

        DummyMediaViewModel testDummyMediaViewModel = DummyUtils.createTestDummyImageViewModel();

        testDummyProjectBindingModel.getDatasets().add(testDummyDatasetViewModel);
        testDummyDatasetViewModel.getMedia().add(testDummyMediaViewModel);

        DummyConfigBindingModel testDummyConfigBindingModel = DummyUtils.createTestDummyConfigBindingModel();
        DummyLabelTaskBindingModel[] testLabelModeDataViewModelArr = DummyUtils.createTestLabelModeDataViewModelArr(2);

        DummyValueUpdateBindingModel testDummyValueUpdateBindingModel = DummyUtils.createTestDummyValueUpdateBindingModel();

        testLabelModeDataViewModelArr[0].getLabelIteration().getEntryValues().add(testDummyValueUpdateBindingModel);

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenReturn(testDummyProjectBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyConfigBindingModel.class)))
                .thenReturn(testDummyConfigBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyDatasetViewModel[].class)))
                .thenReturn(dummyDatasetViewModelsArray);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenReturn(testLabelModeDataViewModelArr);

        when(modelMapperMock.map(any(DummyValueUpdateBindingModel.class), eq(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(new LcEntryValueUpdateBindingModel());

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(projectServiceMock.createProject(any(ProjectCreateBindingModel.class), anyBoolean()))
                .thenReturn(testProjectViewModel);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(firstDatasetViewModel);

        when(labelTaskRepositoryMock.findAllByProjectIdAndDatasetId(anyString(), anyString()))
                .thenReturn(testLabelTaskList);

        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("1"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(rectangleLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("3"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("5"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(secondPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("7"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstClassLcEntryValueViewModel);

        dummyService.createDummyDataForOrg("null");

        verify(projectServiceMock).createProject(any(ProjectCreateBindingModel.class), anyBoolean());
        verify(projectServiceMock, times(1)).createProject(any(ProjectCreateBindingModel.class), anyBoolean());
    }

    @Test
    void createDummyDataForOrg_whenDatasetAlreadyExists_throwException() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IOException, IllegalAccessException, ClassNotFoundException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);
        DatasetViewModel firstDatasetViewModel = testListDatasetViewModel.get(0);

        // Task
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskListWithUrlImages(7, loggedInUserId);

        setImageUrls(testLabelTaskList);

        // LabelConfiguration
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> testLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(8);
        List<LcEntryViewModel> lcEntryViewModels = updateLcEntryViewModels(testLcEntryViewModels);

        testLabelConfigurationViewModel.setEntries(lcEntryViewModels);

        DummyProjectBindingModel testDummyProjectBindingModel = DummyUtils.createTestDummyProjectBindingModel(PROJECT_ID);
        DummyDatasetViewModel testDummyDatasetViewModel = DummyUtils.createTestDummyDatasetViewModel();
        DummyDatasetViewModel[] dummyDatasetViewModelsArray= DummyUtils.createTestDummyDatasetViewModels(1);

        DummyMediaViewModel testDummyMediaViewModel = DummyUtils.createTestDummyImageViewModel();

        testDummyProjectBindingModel.getDatasets().add(testDummyDatasetViewModel);
        testDummyDatasetViewModel.getMedia().add(testDummyMediaViewModel);

        DummyConfigBindingModel testDummyConfigBindingModel = DummyUtils.createTestDummyConfigBindingModel();
        DummyLabelTaskBindingModel[] testLabelModeDataViewModelArr = DummyUtils.createTestLabelModeDataViewModelArr(2);

        DummyValueUpdateBindingModel testDummyValueUpdateBindingModel = DummyUtils.createTestDummyValueUpdateBindingModel();

        testLabelModeDataViewModelArr[0].getLabelIteration().getEntryValues().add(testDummyValueUpdateBindingModel);

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenReturn(testDummyProjectBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyConfigBindingModel.class)))
                .thenReturn(testDummyConfigBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyDatasetViewModel[].class)))
                .thenReturn(dummyDatasetViewModelsArray);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenReturn(testLabelModeDataViewModelArr);

        when(modelMapperMock.map(any(DummyValueUpdateBindingModel.class), eq(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(new LcEntryValueUpdateBindingModel());

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(false);

        when(projectServiceMock.createProject(any(ProjectCreateBindingModel.class), anyBoolean()))
                .thenReturn(testProjectViewModel);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(firstDatasetViewModel);

        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        //Then
        assertThrows(AlreadyExistsException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }

    @Test
    void createDummyDataForOrg_whenProjectJSONIsInvalid_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenThrow(GenericException.class);

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        //Then
        assertThrows(GenericException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }

    @Test
    void createDummyDataForOrg_whenLabelTasksJSONIsInvalid_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenThrow(GenericException.class);

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        //Then
        assertThrows(GenericException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }

    @Test
    void createDummyDataForOrg_whenLcEntryKeyIsNotFound_throwException() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IOException, IllegalAccessException, ClassNotFoundException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);
        DatasetViewModel firstDatasetViewModel = testListDatasetViewModel.get(0);

        // Task
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskListWithUrlImages(7, loggedInUserId);

        setImageUrls(testLabelTaskList);

        // LabelConfiguration
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> testLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(8);
        List<LcEntryViewModel> lcEntryViewModels = updateLcEntryViewModels(testLcEntryViewModels);

        testLabelConfigurationViewModel.setEntries(lcEntryViewModels);

        // LcEntryValueViewModel
        List<LcEntryValueViewModel> lcEntryValueViewModelList = LcEntryValueUtils.createLcEntryValueViewModelList(8);
        List<LcEntryValueViewModel> lcEntryValueViewModels = updateLcEntryValueViewModelList(lcEntryValueViewModelList);

        LcEntryValueViewModel rectangleLcEntryValueViewModel = lcEntryValueViewModels.get(0);
        LcEntryValueViewModel firstPolyLcEntryValueViewModel = lcEntryValueViewModels.get(1);
        LcEntryValueViewModel secondPolyLcEntryValueViewModel = lcEntryValueViewModels.get(2);
        LcEntryValueViewModel firstClassLcEntryValueViewModel = lcEntryValueViewModels.get(3);

        DummyProjectBindingModel testDummyProjectBindingModel = DummyUtils.createTestDummyProjectBindingModel(PROJECT_ID);
        DummyDatasetViewModel testDummyDatasetViewModel = DummyUtils.createTestDummyDatasetViewModel();
        DummyDatasetViewModel[] dummyDatasetViewModelsArray = DummyUtils.createTestDummyDatasetViewModels(1);

        DummyMediaViewModel testDummyMediaViewModel = DummyUtils.createTestDummyImageViewModel();

        testDummyProjectBindingModel.getDatasets().add(testDummyDatasetViewModel);
        testDummyDatasetViewModel.getMedia().add(testDummyMediaViewModel);

        DummyConfigBindingModel testDummyConfigBindingModel = DummyUtils.createTestDummyConfigBindingModel();
        DummyLabelTaskBindingModel[] testLabelModeDataViewModelArr = DummyUtils.createTestLabelModeDataViewModelArr(2);

        DummyValueUpdateBindingModel testDummyValueUpdateBindingModel = DummyUtils.createTestDummyValueUpdateBindingModel();

        testLabelModeDataViewModelArr[0].getLabelIteration().getEntryValues().add(testDummyValueUpdateBindingModel);
        testDummyValueUpdateBindingModel.setEntryKeyLcEntry("invalid_key");

        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenReturn(testDummyProjectBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyConfigBindingModel.class)))
                .thenReturn(testDummyConfigBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyDatasetViewModel[].class)))
                .thenReturn(dummyDatasetViewModelsArray);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenReturn(testLabelModeDataViewModelArr);

        when(modelMapperMock.map(any(DummyValueUpdateBindingModel.class), eq(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(new LcEntryValueUpdateBindingModel());

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(projectServiceMock.createProject(any(ProjectCreateBindingModel.class), anyBoolean()))
                .thenReturn(testProjectViewModel);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(firstDatasetViewModel);

        when(labelTaskRepositoryMock.findAllByProjectIdAndDatasetId(anyString(), anyString()))
                .thenReturn(testLabelTaskList);

        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("1"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(rectangleLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("3"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("5"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(secondPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("7"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstClassLcEntryValueViewModel);

        //Then
        assertThrows(GenericException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }


    @Test
    void createDummyDataForOrg_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }

    @Test
    void createDummyDataForOrg_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> dummyService.createDummyDataForOrg("datagym")
        );
    }

    @Test
    void createDummyDataForOrg_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> dummyService.createDummyDataForOrg("testOrg")
        );
    }

    private List<LcEntryValueViewModel> updateLcEntryValueViewModelList(List<LcEntryValueViewModel> lcEntryValueViewModelList) {
        List<LcEntryValueViewModel> lcEntryValueViewModels = new ArrayList<>();

        // first Rectangle Geometry Tree
        LcEntryValueViewModel rectangleValueViewModel = lcEntryValueViewModelList.get(0);
        rectangleValueViewModel.setId("11");
        rectangleValueViewModel.setLcEntryValueParentId(null);
        rectangleValueViewModel.setLcEntryId("1");
        rectangleValueViewModel.setEntryTypeLcEntry("RECTANGLE");

        LcEntryValueViewModel freetextValueViewModel = lcEntryValueViewModelList.get(1);
        freetextValueViewModel.setId("12");
        freetextValueViewModel.setLcEntryValueParentId("11");
        freetextValueViewModel.setLcEntryId("2");
        freetextValueViewModel.setEntryTypeLcEntry("FREETEXT");
        ;

        rectangleValueViewModel.getChildren().add(freetextValueViewModel);

        lcEntryValueViewModels.add(rectangleValueViewModel);

        // Second (Polygon) Geometry Tree
        LcEntryValueViewModel polygonValueViewModel = lcEntryValueViewModelList.get(2);
        polygonValueViewModel.setId("13");
        polygonValueViewModel.setLcEntryValueParentId(null);
        polygonValueViewModel.setLcEntryId("3");
        polygonValueViewModel.setEntryTypeLcEntry("POLYGON");

        LcEntryValueViewModel selectValueViewModel = lcEntryValueViewModelList.get(3);
        selectValueViewModel.setId("14");
        selectValueViewModel.setLcEntryValueParentId("13");
        selectValueViewModel.setLcEntryId("4");
        selectValueViewModel.setEntryTypeLcEntry("SELECT");

        polygonValueViewModel.getChildren().add(selectValueViewModel);

        lcEntryValueViewModels.add(polygonValueViewModel);

        // Third (Polygon) Geometry Tree
        LcEntryValueViewModel secondPolygonValueViewModel = lcEntryValueViewModelList.get(4);
        secondPolygonValueViewModel.setId("15");
        secondPolygonValueViewModel.setLcEntryValueParentId(null);
        secondPolygonValueViewModel.setLcEntryId("5");
        secondPolygonValueViewModel.setEntryTypeLcEntry("POLYGON");

        LcEntryValueViewModel secondSelectValueViewModel = lcEntryValueViewModelList.get(5);
        secondSelectValueViewModel.setId("16");
        secondSelectValueViewModel.setLcEntryValueParentId("15");
        secondSelectValueViewModel.setLcEntryId("6");
        secondSelectValueViewModel.setEntryTypeLcEntry("SELECT");

        secondPolygonValueViewModel.getChildren().add(secondSelectValueViewModel);

        lcEntryValueViewModels.add(secondPolygonValueViewModel);

        // First Classifications Tree
        LcEntryValueViewModel firstClassSelectViewModel = lcEntryValueViewModelList.get(6);
        firstClassSelectViewModel.setId("17");
        firstClassSelectViewModel.setLcEntryValueParentId(null);
        firstClassSelectViewModel.setLcEntryId("7");
        firstClassSelectViewModel.setEntryTypeLcEntry("SELECT");

        LcEntryValueViewModel firstClSelectViewModel = lcEntryValueViewModelList.get(7);
        firstClSelectViewModel.setId("18");
        firstClSelectViewModel.setLcEntryValueParentId("17");
        firstClSelectViewModel.setLcEntryId("8");
        firstClSelectViewModel.setEntryTypeLcEntry("SELECT");

        firstClSelectViewModel.getChildren().add(firstClSelectViewModel);

        lcEntryValueViewModels.add(firstClSelectViewModel);

        return lcEntryValueViewModels;

    }

    private List<LcEntryViewModel> updateLcEntryViewModels(List<LcEntryViewModel> testLcEntryViewModels) {
        List<LcEntryViewModel> lcEntryViewModels = new ArrayList<>();

        // first Geometry Tree
        LcEntryViewModel rectangleViewModel = testLcEntryViewModels.get(0);
        rectangleViewModel.setId("1");
        rectangleViewModel.setLcEntryParentId(null);
        rectangleViewModel.setEntryKey("rocket");
        rectangleViewModel.setEntryValue("Rocket");
        rectangleViewModel.setType(LcEntryType.RECTANGLE);
        rectangleViewModel.setRequired(false);

        LcEntryViewModel freetextViewModel = testLcEntryViewModels.get(1);
        freetextViewModel.setId("2");
        freetextViewModel.setLcEntryParentId("1");
        freetextViewModel.setEntryKey("count_rockets");
        freetextViewModel.setEntryValue("How many rockets are on the picture?");
        freetextViewModel.setType(LcEntryType.FREETEXT);
        freetextViewModel.setRequired(false);
        freetextViewModel.setMaxLength(5);

        rectangleViewModel.getChildren().add(freetextViewModel);

        lcEntryViewModels.add(rectangleViewModel);

        // second Geometry Tree
        LcEntryViewModel polygonViewModel = testLcEntryViewModels.get(2);
        polygonViewModel.setId("3");
        polygonViewModel.setLcEntryParentId(null);
        polygonViewModel.setEntryKey("jet_key");
        polygonViewModel.setEntryValue("Jet");
        polygonViewModel.setType(LcEntryType.POLYGON);
        polygonViewModel.setRequired(false);

        Map<String, String> selectOptions = new HashMap<>();
        selectOptions.put("no", "No");
        selectOptions.put("yes", "Yes");

        LcEntryViewModel selectViewModel = testLcEntryViewModels.get(3);
        selectViewModel.setId("4");
        selectViewModel.setLcEntryParentId("3");
        selectViewModel.setEntryKey("military_key");
        selectViewModel.setEntryValue("Military jet?");
        selectViewModel.setType(LcEntryType.SELECT);
        selectViewModel.setRequired(false);
        selectViewModel.setOptions(selectOptions);

        polygonViewModel.getChildren().add(selectViewModel);

        lcEntryViewModels.add(polygonViewModel);

        // third Geometry Tree
        LcEntryViewModel secondPolygonViewModel = testLcEntryViewModels.get(4);
        secondPolygonViewModel.setId("5");
        secondPolygonViewModel.setLcEntryParentId(null);
        secondPolygonViewModel.setEntryKey("helicopter");
        secondPolygonViewModel.setEntryValue("Helicopter");
        secondPolygonViewModel.setType(LcEntryType.POLYGON);
        secondPolygonViewModel.setRequired(false);

        Map<String, String> secondSelectOptions = new HashMap<>();
        secondSelectOptions.put("dual_rotors", "Dual rotors (counterrotating)");
        secondSelectOptions.put("single_main_rotor", "Single main rotor");

        LcEntryViewModel secondSelectViewModel = testLcEntryViewModels.get(5);
        secondSelectViewModel.setId("6");
        secondSelectViewModel.setLcEntryParentId("5");
        secondSelectViewModel.setEntryKey("rotor_configurations");
        secondSelectViewModel.setEntryValue("Rotor configuration");
        secondSelectViewModel.setType(LcEntryType.SELECT);
        secondSelectViewModel.setRequired(false);
        secondSelectViewModel.setOptions(secondSelectOptions);

        secondPolygonViewModel.getChildren().add(secondSelectViewModel);

        lcEntryViewModels.add(secondPolygonViewModel);

        // First Classifications Tree
        Map<String, String> firstClassSelectOptions = new HashMap<>();
        firstClassSelectOptions.put("rainy", "Rainy");
        firstClassSelectOptions.put("sunny", "Sunny");
        firstClassSelectOptions.put("snowy", "Snowy");

        LcEntryViewModel firstClassSelectViewModel = testLcEntryViewModels.get(6);
        firstClassSelectViewModel.setId("7");
        firstClassSelectViewModel.setLcEntryParentId(null);
        firstClassSelectViewModel.setEntryKey("weather");
        firstClassSelectViewModel.setEntryValue("What is the weather like?");
        firstClassSelectViewModel.setType(LcEntryType.SELECT);
        firstClassSelectViewModel.setRequired(false);
        firstClassSelectViewModel.setOptions(firstClassSelectOptions);

        Map<String, String> firstClassChecklistOptions = new HashMap<>();
        firstClassChecklistOptions.put("night", "Night");
        firstClassChecklistOptions.put("day", "Day");

        LcEntryViewModel firstClassChecklistViewModel = testLcEntryViewModels.get(7);
        firstClassChecklistViewModel.setId("8");
        firstClassChecklistViewModel.setLcEntryParentId("7");
        firstClassChecklistViewModel.setEntryKey("daytime");
        firstClassChecklistViewModel.setEntryValue("Part of the day?");
        firstClassChecklistViewModel.setType(LcEntryType.SELECT);
        firstClassChecklistViewModel.setRequired(false);
        firstClassChecklistViewModel.setOptions(firstClassChecklistOptions);

        firstClassSelectViewModel.getChildren().add(firstClassChecklistViewModel);

        lcEntryViewModels.add(firstClassSelectViewModel);

        return lcEntryViewModels;
    }

    @Test
    void createDummyDataForOrg_whenProjectConfigurationJSONIsInvalid_throwException() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        String loggedInUserId = oauthUser.id();

        // Given
        ProjectViewModel testProjectViewModel = ProjectUtils.createTestProjectViewModel(PROJECT_ID);

        List<DatasetViewModel> testListDatasetViewModel = DatasetUtils.createTestListDatasetViewModel(2);
        DatasetViewModel firstDatasetViewModel = testListDatasetViewModel.get(0);

        // Task
        List<LabelTask> testLabelTaskList = LabelTaskUtils.createTestLabelTaskListWithUrlImages(7, loggedInUserId);

        setImageUrls(testLabelTaskList);

        // LabelConfiguration
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils.createTestLabelConfigurationViewModel();
        List<LcEntryViewModel> testLcEntryViewModels = LcEntryUtils.createTestLcEntryViewModels(8);
        List<LcEntryViewModel> lcEntryViewModels = updateLcEntryViewModels(testLcEntryViewModels);

        testLabelConfigurationViewModel.setEntries(lcEntryViewModels);

        // LcEntryValueViewModel
        List<LcEntryValueViewModel> lcEntryValueViewModelList = LcEntryValueUtils.createLcEntryValueViewModelList(8);
        List<LcEntryValueViewModel> lcEntryValueViewModels = updateLcEntryValueViewModelList(lcEntryValueViewModelList);

        LcEntryValueViewModel rectangleLcEntryValueViewModel = lcEntryValueViewModels.get(0);
        LcEntryValueViewModel firstPolyLcEntryValueViewModel = lcEntryValueViewModels.get(1);
        LcEntryValueViewModel secondPolyLcEntryValueViewModel = lcEntryValueViewModels.get(2);
        LcEntryValueViewModel firstClassLcEntryValueViewModel = lcEntryValueViewModels.get(3);

        DummyProjectBindingModel testDummyProjectBindingModel = DummyUtils.createTestDummyProjectBindingModel(PROJECT_ID);
        DummyDatasetViewModel testDummyDatasetViewModel = DummyUtils.createTestDummyDatasetViewModel();
        DummyMediaViewModel testDummyMediaViewModel = DummyUtils.createTestDummyImageViewModel();

        testDummyProjectBindingModel.getDatasets().add(testDummyDatasetViewModel);
        testDummyDatasetViewModel.getMedia().add(testDummyMediaViewModel);

        DummyConfigBindingModel testDummyConfigBindingModel = DummyUtils.createTestDummyConfigBindingModel();
        DummyLabelTaskBindingModel[] testLabelModeDataViewModelArr = DummyUtils.createTestLabelModeDataViewModelArr(2);

        DummyValueUpdateBindingModel testDummyValueUpdateBindingModel = DummyUtils.createTestDummyValueUpdateBindingModel();

        testLabelModeDataViewModelArr[0].getLabelIteration().getEntryValues().add(testDummyValueUpdateBindingModel);
        testDummyValueUpdateBindingModel.setEntryKeyLcEntry("invalid_key");


        //When
        when(objectMapperMock.readValue(any(URL.class), eq(DummyProjectBindingModel.class)))
                .thenReturn(testDummyProjectBindingModel);

        when(objectMapperMock.readValue(any(URL.class), eq(DummyConfigBindingModel.class)))
                .thenThrow(GenericException.class);


        when(objectMapperMock.readValue(any(URL.class), eq(DummyLabelTaskBindingModel[].class)))
                .thenReturn(testLabelModeDataViewModelArr);


        when(modelMapperMock.map(any(DummyValueUpdateBindingModel.class), eq(LcEntryValueUpdateBindingModel.class)))
                .thenReturn(new LcEntryValueUpdateBindingModel());

        when(projectServiceMock.isProjectNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(datasetServiceMock.isDatasetNameUniqueAndDeletedFalse(anyString(), anyString()))
                .thenReturn(true);

        when(projectServiceMock.createProject(any(ProjectCreateBindingModel.class), anyBoolean()))
                .thenReturn(testProjectViewModel);

        when(datasetServiceMock.createDataset(any(DatasetCreateBindingModel.class), anyBoolean()))
                .thenReturn(firstDatasetViewModel);

        when(labelTaskRepositoryMock.findAllByProjectIdAndDatasetId(anyString(), anyString()))
                .thenReturn(testLabelTaskList);

        when(labelConfigurationServiceMock.updateLabelConfiguration(anyString(), anyList(), anyBoolean()))
                .thenReturn(testLabelConfigurationViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("1"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(rectangleLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("3"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("5"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(secondPolyLcEntryValueViewModel);

        when(lcEntryValueServiceMock.createLcEntryValueTree(eq("7"), any(LcEntryValueCreateBindingModel.class)))
                .thenReturn(firstClassLcEntryValueViewModel);

        //Then
        assertThrows(GenericException.class,
                () -> dummyService.createDummyDataForOrg("null")
        );
    }
}