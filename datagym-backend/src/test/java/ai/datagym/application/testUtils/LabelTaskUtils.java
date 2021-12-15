package ai.datagym.application.testUtils;

import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskMoveAllBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskReviewBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.UrlImage;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.project.entity.Project;
import com.eforce21.lib.bin.file.entity.BinFileEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;

public class LabelTaskUtils {
    public static final String LABEL_TASK_ID = "TestId " + UUID.randomUUID();
    public static final String REVIEW_COMMENT = "review_comment";

    private static final Project project = ProjectUtils.createTestProject(PROJECT_ID);
    private static final Media MEDIA = ImageUtils.createTestImage(IMAGE_ID);
    private static final UrlImage urlImage = ImageUtils.createTestUrlImage();
    private static final BinFileEntity binFileEntity = BinfileEntityUtils.createTestBinFileEntity();
    private static final LabelIteration labelIteration = LabelIterationUtils.createTestLabelIteration(project);
    private static final Long TIME = System.currentTimeMillis();

    public static LabelTask createTestLabelTask(String labeler) {
        project.setOwner(labeler);

        return new LabelTask() {{
            setId(LABEL_TASK_ID);
            setLabelTaskState(LabelTaskState.BACKLOG);
            setProject(project);
            setMedia(MEDIA);
            setLabeler(labeler);
            setLabelIteration(labelIteration);
        }};
    }

    public static List<LabelTask> createTestLabelTaskList(int count, String labeler) {
        project.setOwner(labeler);

        return IntStream.range(0, count)
                .mapToObj(index -> new LabelTask() {{
                    setId(LABEL_TASK_ID + index);
                    setLabelTaskState(LabelTaskState.BACKLOG);
                    setProject(project);
                    setMedia(MEDIA);
                    setLabeler(labeler);
                    setLabelIteration(labelIteration);
                }})
                .collect(Collectors.toList());
    }
    public static List<LabelTask> createTestLabelTaskListWithUrlImages(int count, String labeler) {
        project.setOwner(labeler);


        return IntStream.range(0, count)
                .mapToObj(index -> new LabelTask() {{
                    setId(LABEL_TASK_ID + index);
                    setLabelTaskState(LabelTaskState.BACKLOG);
                    setProject(project);
                    setMedia(ImageUtils.createTestUrlImage());
                    setLabeler(labeler);
                    setLabelIteration(labelIteration);
                }})
                .collect(Collectors.toList());
    }

    public static LabelTaskMoveAllBindingModel createTestLabelTaskMoveAllBindingModel() {
        return new LabelTaskMoveAllBindingModel() {{
            setProjectId(PROJECT_ID);
            setDatasetId(DATASET_ID);
            setDirection("waiting");
        }};
    }

    public static LabelTaskCompleteBindingModel createTestLabelTaskCompleteBindingModel() {
        return new LabelTaskCompleteBindingModel() {{
            setLastChangedConfig(TIME);
        }};
    }

    public static LabelTaskCompleteViewModel createTestLabelTaskCompleteViewModel() {
        return new LabelTaskCompleteViewModel() {{
            setHasLabelConfigChanged(false);
            setCurrentTaskId(LABEL_TASK_ID);
        }};
    }

    public static LabelTaskReviewBindingModel createTestLabelTaskReviewBindingModel() {
        return new LabelTaskReviewBindingModel() {{
            setTaskId("label_task_id");
            setReviewComment(REVIEW_COMMENT);
        }};
    }

    public static LabelTaskViewModel createTestLabelTaskViewModel(String labeler) {
        return new LabelTaskViewModel() {{
            setTaskId(LABEL_TASK_ID);
            setProjectId(PROJECT_ID);
            setProjectName("projectName");
            setLabelTaskState(LabelTaskState.BACKLOG.name());
            setMediaId(IMAGE_ID);
            setMediaName("mediaName");
            setLabeler(labeler);
            setIterationId(LC_ITERATION_ID);
            setIterationRun(1);
        }};
    }

    public static List<LabelTaskViewModel> createTestLabelTaskViewModelList(int count, String labeler) {
        project.setOwner(labeler);

        return IntStream.range(0, count)
                .mapToObj(index -> new LabelTaskViewModel() {{
                    setTaskId(LABEL_TASK_ID + index);
                    setProjectId(PROJECT_ID);
                    setProjectName("projectName");
                    setLabelTaskState(LabelTaskState.BACKLOG.name());
                    setMediaId(IMAGE_ID + index);
                    setMediaName("mediaName" + index);
                    setLabeler("labeler");
                    setIterationId(LC_ITERATION_ID);
                    setIterationRun(1);
                }})
                .collect(Collectors.toList());
    }

    public static List<UserTaskViewModel> createTestUserTaskViewModelList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new UserTaskViewModel() {{
                    setProjectId(PROJECT_ID + index);
                    setProjectName("projectName" + index);
                    setCountWaitingTasks(index + 1);
                }})
                .collect(Collectors.toList());
    }

    public static LabelModeDataViewModel createTestLabelModeDataViewModel() {
        LabelConfigurationViewModel testLabelConfigurationViewModel = LabelConfigurationUtils
                .createTestLabelConfigurationViewModel();

        MediaViewModel testMediaViewModel = ImageUtils.createTestImageViewModel(IMAGE_ID);

        LabelIterationViewModel testLabelIterationViewModel = LabelIterationUtils
                .createTestLabelIterationViewModel();

        return new LabelModeDataViewModel() {{
            setTaskId(LABEL_TASK_ID);
            setLabelConfig(testLabelConfigurationViewModel);
            setMedia(testMediaViewModel);
            setLabelIteration(testLabelIterationViewModel);
        }};
    }
}
