package ai.datagym.application.testUtils;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectDatasetViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_NAME;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;

public class ProjectUtils {
    public static final String PROJECT_ID = "TestId " + UUID.randomUUID();
    public static final String PROJECT_NAME = "TestName " + UUID.randomUUID();

    private static final Long TIME = new Date().getTime();

    private static final LabelConfiguration labelConfiguration = new LabelConfiguration();
    private static final LabelIteration labelIteration = new LabelIteration();

    public static Project createTestProject(String id) {
        labelConfiguration.setId(LC_CONFIG_ID);

        return new Project() {{
            setId(id);
            setName("ProjectName");
            setDescription("Project description");
            setShortDescription("Project shortDescription");
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setPinned(false);
            setLabelConfiguration(labelConfiguration);
            setLabelIteration(labelIteration);
            setOwner("eforce21");
            setLabelTasks(new ArrayList<>());
            setMediaType(MediaType.IMAGE);
        }};
    }

    public static List<Project> createTestProjects(int count) {
        labelConfiguration.setId(LC_CONFIG_ID);

        return IntStream.range(0, count)
                .mapToObj(index -> new Project() {{
                    setId(String.valueOf(index + 1));
                    setName("ProjectName " + index + 1);
                    setDescription("Project description " + index + 1);
                    setShortDescription("Project shortDescription " + index + 1);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setPinned(false);
                    setLabelConfiguration(labelConfiguration);
                    setLabelIteration(labelIteration);
                    setOwner("eforce21");
                    setLabelTasks(new ArrayList<>());
                }})
                .collect(Collectors.toList());
    }

    public static ProjectViewModel createTestProjectViewModel(String id) {
        return new ProjectViewModel() {{
            setId(id);
            setName("ProjectName");
            setDescription("Project description");
            setShortDescription("Project shortDescription");
            setPinned(false);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new HashSet<DatasetAllViewModel>());
            setLabelConfigurationId(LC_CONFIG_ID);
            setLabelIterationId(LC_ITERATION_ID);
            setOwner("eforce21");
        }};
    }

    public static List<ProjectViewModel> createTestProjectViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ProjectViewModel() {{
                    setId(String.valueOf(index + 1));
                    setName("ProjectName " + index + 1);
                    setDescription("Project description " + index + 1);
                    setShortDescription("Project shortDescription " + index + 1);
                    setPinned(false);
                    setLabelConfigurationId(LC_CONFIG_ID);
                    setLabelIterationId(LC_ITERATION_ID);
                    setOwner("eforce21");
                }})
                .collect(Collectors.toList());
    }

    public static List<LabelTaskViewModel> createTestLabelTaskViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LabelTaskViewModel() {{
                    setTaskId(String.valueOf(index + 1));
                    setProjectId(String.valueOf(PROJECT_ID + index));
                    setProjectName("ProjectName " + index);
                    setLabelTaskState("BACKLOG");
                    setMediaId(IMAGE_ID + index);
                    setMediaName(IMAGE_NAME + index);
                    setLabeler("eforce21");
                    setIterationId(LC_ITERATION_ID);
                    setIterationRun(1);
                }})
                .collect(Collectors.toList());
    }

    public static ProjectUpdateBindingModel createTestProjectUpdateBindingModel() {
        return new ProjectUpdateBindingModel() {{
            setName("ProjectName");
            setDescription("Project description");
            setShortDescription("Project shortDescription");
        }};
    }

    public static ProjectCreateBindingModel createTestProjectCreateBindingModel() {
        return new ProjectCreateBindingModel() {{
            setOwner("Demo owner");
            setName("ProjectName");
            setDescription("Project description");
            setShortDescription("Project shortDescription");
            setOwner("eforce21");
        }};
    }

    public static List<ProjectDatasetViewModel> createTestProjectDatasetViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ProjectDatasetViewModel() {{
                    setId(String.valueOf(index + 1));
                    setName("ProjectName " + index + 1);
                    setDescription("Project description " + index + 1);
                    setShortDescription("Project shortDescription " + index + 1);
                    setPinned(false);
                    setOwner("eforce21");
                }})
                .collect(Collectors.toList());
    }

    public static ProjectDashboardViewModel createTestProjectDashboardViewModel(String id) {
        return new ProjectDashboardViewModel() {{
            setId(id);
            setName("ProjectName");
            setDescription("Project description");
            setShortDescription("Project shortDescription");
            setPinned(false);
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setDatasets(new HashSet<DatasetAllViewModel>());
            setLabelConfigurationId(LC_CONFIG_ID);
            setLabelIterationId(LC_ITERATION_ID);
            setOwner("eforce21");
            setCountTasks(1);
            setCountDatasets(1);
            setApprovedReviewPerformance(1);
            setDeclinedReviewPerformance(1);
            setHasLabelConfiguration(true);
            setCurrentPlan("DEVELOPER_PRO");
            setTaskStatus(new HashMap<>());
            setTaskMediaDetail(new HashMap<>());
        }};
    }
}
