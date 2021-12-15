package ai.datagym.application.testUtils;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.project.entity.MediaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatasetUtils {
    public static final String DATASET_ID = "TestId " + UUID.randomUUID();
    public static final String DATASET_NAME = "TestName " + UUID.randomUUID();

    private static final Long TIME = System.currentTimeMillis();

    public static Dataset createTestDataset(String id) {
        return new Dataset() {{
            setId(id);
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setMedia(new HashSet<>());
            setOwner("eforce21");
        }};
    }

    public static Set<Dataset> createTestSetsDatasets(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Dataset() {{
                    setId(String.valueOf(index + 1));
                    setName("ProjectName " + index + 1);
                    setShortDescription("Project shortDescription " + index + 1);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMedia(new HashSet<>());
                    setOwner("eforce21");
                }})
                .collect(Collectors.toSet());
    }

    public static List<Dataset> createTestListDatasets(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Dataset() {{
                    setId(String.valueOf(index + 1));
                    setName("ProjectName " + index + 1);
                    setShortDescription("Project shortDescription " + index + 1);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMedia(new HashSet<>());
                    setOwner("eforce21");
                }})
                .collect(Collectors.toList());
    }

    public static DatasetCreateBindingModel createTestDatasetCreateBindingModel() {
        return new DatasetCreateBindingModel() {{
            setOwner("eforce21");
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
            setMediaType(MediaType.IMAGE);
        }};
    }

    public static DatasetUpdateBindingModel createTestDatasetUpdateBindingModel() {
        return new DatasetUpdateBindingModel() {{
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
        }};
    }

    public static DatasetViewModel createTestDatasetViewModel(String id) {
        return new DatasetViewModel() {{
            setId(id);
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setMedia(new HashSet<>());
            setOwner("eforce21");
        }};
    }

    public static List<DatasetViewModel> createTestListDatasetViewModel(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new DatasetViewModel() {{
                    setId(String.valueOf(index + 1));
                    setName("DatsetName " + index + 1);
                    setShortDescription("Dataset shortDescription " + index + 1);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMedia(new HashSet<>());
                    setOwner("eforce21");
                }})
                .collect(Collectors.toList());
    }

    public static List<DatasetAllViewModel> createTestListDatasetAllViewModel(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new DatasetAllViewModel() {{
                    setId(String.valueOf(index + 1));
                    setName("DatasetName " + index + 1);
                    setShortDescription("Dataset shortDescription " + index + 1);
                    setTimestamp(TIME);
                    setDeleted(false);
                    setDeleteTime(null);
                    setMediaCount(1 + index);
                    setOwner("eforce21");
                }})
                .collect(Collectors.toList());
    }

    public static DatasetProjectViewModel createTestDatasetProjectViewModel(String id) {
        return new DatasetProjectViewModel() {{
            setId(id);
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
            setTimestamp(TIME);
            setDeleted(false);
            setDeleteTime(null);
            setProjects(new HashSet<>());
            setMedia(new HashSet<>());
            setOwner("eforce21");
        }};
    }
}
