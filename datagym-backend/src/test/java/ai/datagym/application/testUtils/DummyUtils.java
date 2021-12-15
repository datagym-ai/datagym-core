package ai.datagym.application.testUtils;

import ai.datagym.application.dummy.models.bindingModels.labelConfiguration.DummyConfigBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyLabelIterationViewModel;
import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyValueUpdateBindingModel;
import ai.datagym.application.dummy.models.bindingModels.labelTask.DummyLabelTaskBindingModel;
import ai.datagym.application.dummy.models.bindingModels.media.DummyMediaViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyDatasetViewModel;
import ai.datagym.application.dummy.models.bindingModels.project.DummyProjectBindingModel;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.project.entity.Project;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;

public class DummyUtils {
    public static final String IMAGE_URL_ONE = "https://scontent-ber1-1.xx.fbcdn.net/v/t1.0-9/84036291_10221602732601812_2950827215945728_n.jpg?_nc_cat=104&_nc_ohc=v5cUBLA9-_sAX_6_kWl&_nc_ht=scontent-ber1-1.xx&oh=c6c33009ab92d59f945e4d4e9fd6aa29&oe=5E94C39D";
    public static final String IMAGE_URL_TWO = "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/83088336_2529574723992748_7382513766337871872_o.jpg?_nc_cat=105&_nc_ohc=vk7CQTosR1sAX9lL7Ul&_nc_ht=scontent-frx5-1.xx&oh=6b87cf92e734f4650e592e6c461c8dc9&oe=5E9B9E8D";
    public static final String IMAGE_URL_THREE = "https://scontent-frt3-2.xx.fbcdn.net/v/t1.0-9/82484858_2532575237026030_4340186360959729664_o.jpg?_nc_cat=101&_nc_ohc=_PrrPm7Nun0AX9535c-&_nc_ht=scontent-frt3-2.xx&oh=0c78a689167111ad19a8a146b6b0a9f6&oe=5E9F19F7";
    public static final String IMAGE_URL_FOUR = "https://scontent-frt3-1.xx.fbcdn.net/v/t1.0-9/82486906_2532088360408051_860179016202584064_o.jpg?_nc_cat=102&_nc_ohc=sKMPPQXH2KsAX-aBxgL&_nc_ht=scontent-frt3-1.xx&oh=528e1ecd1a0395fe2f98dde198e771d6&oe=5E9794F8";
    public static final String IMAGE_URL_FIVE = "https://scontent-frt3-1.xx.fbcdn.net/v/t1.0-9/82132722_2527426450874242_2820555811271475200_o.jpg?_nc_cat=106&_nc_ohc=8ArYk1lF8bwAX94wLfH&_nc_ht=scontent-frt3-1.xx&oh=a891d9497b84a88d59e88ec8f59e1c27&oe=5E91B7DA";
    public static final String IMAGE_URL_SIX = "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/81935505_2524576057825948_758053335377903616_o.jpg?_nc_cat=111&_nc_ohc=MrtQALf9PUwAX-EuI6a&_nc_ht=scontent-frx5-1.xx&oh=3632ac2c6696c22f0fc6ffca94be5eb8&oe=5E9162A9";
    public static final String IMAGE_URL_SEVEN = "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/81702248_2523745481242339_6270696507503017984_o.jpg?_nc_cat=105&_nc_ohc=GbfU1sFfzRwAX9mAokE&_nc_ht=scontent-frx5-1.xx&oh=e938e94c2db02936004afdfa79619e51&oe=5E907042";

    public static final String DUMMY_PROJECT_PLACEHOLDER = "Dummy_Project";
    public static final String DUMMY_DATASET_ONE_PLACEHOLDER = "Dummy_Dataset_One";
    public static final String DUMMY_DATASET_TWO_PLACEHOLDER = "Dummy_Dataset_Two";

    private static final Long TIME = System.currentTimeMillis();

    public static final String LABEL_TASK_ID = "TestId " + UUID.randomUUID();

    private static final Project project = ProjectUtils.createTestProject(PROJECT_ID);
    private static final Media MEDIA = ImageUtils.createTestImage(IMAGE_ID);
    private static final LabelIteration labelIteration = LabelIterationUtils.createTestLabelIteration(project);


    public static LcEntryUpdateBindingModel createTestLcEntryUpdateBindingModel(
            String id,
            String entryKey,
            String entryValue,
            String type,
            String color,
            String shortcut,
            Integer maxLength,
            boolean required,
            Map<String, String> options,
            LcEntryUpdateBindingModel parent) {
        return new LcEntryUpdateBindingModel() {{
            setId(id);
            setEntryKey(entryKey);
            setEntryValue(entryValue);
            setType(type);
            setColor(color);
            setShortcut(shortcut);
            setParentEntry(parent);
            setChildren(new ArrayList<>());
            setMaxLength(maxLength);
            setOptions(options);
            setRequired(required);
        }};
    }

    public static DummyProjectBindingModel createTestDummyProjectBindingModel(String id) {
        return new DummyProjectBindingModel() {{
            setId(id);
            setName(DUMMY_PROJECT_PLACEHOLDER);
            setDescription("Project description");
            setShortDescription("Project shortDescription");
            setPinned(false);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new ArrayList<>());
            setLabelConfigurationId(LC_CONFIG_ID);
            setLabelIterationId(LC_ITERATION_ID);
            setOwner("eforce21");
        }};
    }

    public static DummyConfigBindingModel createTestDummyConfigBindingModel() {
        return new DummyConfigBindingModel() {{
            setId(LC_CONFIG_ID);
            setProjectId(PROJECT_ID);
            setEntries(new ArrayList<>());
        }};
    }

    public static DummyLabelTaskBindingModel[] createTestLabelModeDataViewModelArr(int count) {
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils
                .createTestLabelConfigurationViewModel();

        DummyMediaViewModel testDummyMediaViewModel = createTestDummyImageViewModel();
        DummyLabelIterationViewModel testDummyLabelIterationViewModel = createTestDummyLabelIterationViewModel();

        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils
                .createTestLabelIterationViewModel();

        return IntStream.range(0, count)
                .mapToObj(index -> new DummyLabelTaskBindingModel() {{
                    setTaskId(LABEL_TASK_ID);
                    setLabelTaskState(LabelTaskState.BACKLOG.name());
                    setMedia(testDummyMediaViewModel);
                    setLabelIteration(testDummyLabelIterationViewModel);
                }})
                .toArray(DummyLabelTaskBindingModel[]::new);
    }

    public static DummyMediaViewModel createTestDummyImageViewModel() {
        return new DummyMediaViewModel() {{
            setId(IMAGE_ID);
            setUrl(IMAGE_URL_ONE);
            setTimestamp(null);
            setMediaSourceType(MediaSourceType.SHAREABLE_LINK.name());
            setMediaSourceType("test_image_name");
        }};
    }


    public static DummyLabelIterationViewModel createTestDummyLabelIterationViewModel() {
        return new DummyLabelIterationViewModel() {{
            setId(LC_ITERATION_ID);
            setProjectId(PROJECT_ID);
            setRun(1);
            setEntryValues(new ArrayList<>());
        }};
    }

    public static DummyDatasetViewModel createTestDummyDatasetViewModel() {
        return new DummyDatasetViewModel() {{
            setId(DATASET_ID);
            setName(DUMMY_DATASET_ONE_PLACEHOLDER);
            setShortDescription("short_description");
            setTimestamp(null);
            setDeleted(false);
            setDeleteTime(null);
            setOwner("eforce21");
            setMedia(new ArrayList<>());
            setProjectCount(1);
        }};
    }

    public static DummyDatasetViewModel[] createTestDummyDatasetViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new DummyDatasetViewModel() {{
                    setId(DATASET_ID + index);
                    setName(DUMMY_DATASET_ONE_PLACEHOLDER);
                    setShortDescription("short_description");
                    setTimestamp(null);
                    setDeleted(false);
                    setDeleteTime(null);
                    setOwner("eforce21");
                    setMedia(new ArrayList<>());
                    setProjectCount(1);
                }})
                .toArray(DummyDatasetViewModel[]::new);
    }

    public static DummyValueUpdateBindingModel createTestDummyValueUpdateBindingModel() {
        return new DummyValueUpdateBindingModel() {{
            setId(DATASET_ID);
            setLcEntryId(LC_ITERATION_ID);
            setLcEntryValueParentId(LC_ITERATION_ID + "parent");
            setSelectKey("select_key");
            setParentEntry(null);
            setChildren(new ArrayList<>());
            setEntryKeyLcEntry("rocket");
        }};
    }
}
