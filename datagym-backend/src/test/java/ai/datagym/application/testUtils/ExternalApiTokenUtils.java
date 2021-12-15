package ai.datagym.application.testUtils;

import ai.datagym.application.externalAPI.models.bindingModels.ExternalApiCreateDatasetBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiDatasetViewModel;
import ai.datagym.application.externalAPI.models.viewModels.ExternalApiProjectViewModel;
import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.DatasetUtils.DATASET_NAME;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;

public class ExternalApiTokenUtils {

    private static final Long TIME = new Date().getTime();

    public static ExternalApiProjectViewModel createTestExternalApiProjectViewModel() {
        return new ExternalApiProjectViewModel() {{
            setId(PROJECT_ID);
            setName("ProjectName");
            setOwner("eforce21");
            setShortDescription("Project shortDescription");
            setDescription("Project description");
            setTimestamp(TIME);
            setLabelConfigurationId(LC_CONFIG_ID);
            setLabelIterationId(LC_ITERATION_ID);
            setDatasets(new HashSet<>());
        }};
    }


    public static List<ExternalApiProjectViewModel> createTestExternalApiProjectViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ExternalApiProjectViewModel() {{
                    setId(PROJECT_ID + index);
                    setName("ProjectName " + index + 1);
                    setOwner("eforce21");
                    setShortDescription("Project shortDescription " + index + 1);
                    setDescription("Project description " + index + 1);
                    setTimestamp(TIME);
                    setLabelConfigurationId(LC_CONFIG_ID);
                    setLabelIterationId(LC_ITERATION_ID);
                    setDatasets(new HashSet<>());
                }})
                .collect(Collectors.toList());
    }

    public static ExternalApiDatasetViewModel createTestExternalApiDatasetViewModel() {
        return new ExternalApiDatasetViewModel() {{
            setId(DATASET_ID);
            setName("DatasetName");
            setOwner("eforce21");
            setShortDescription("Dataset shortDescription");
            setTimestamp(TIME);
            setProjectCount(1);
            setMedia(new HashSet<>());
        }};
    }

    public static List<ExternalApiDatasetViewModel> createTestExternalApiDatasetViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ExternalApiDatasetViewModel() {{
                    setId(DATASET_ID + index);
                    setName(DATASET_NAME);
                    setOwner("eforce21");
                    setShortDescription("shortDescription");
                    setTimestamp(TIME);
                    setProjectCount(1);
                    setMedia(new HashSet<>());
                }})
                .collect(Collectors.toList());
    }

    public static ExternalApiCreateDatasetBindingModel createTestExternalApiCreateDatasetBindingModel() {
        return new ExternalApiCreateDatasetBindingModel() {{
            setName("DatasetName");
            setShortDescription("Dataset shortDescription");
        }};
    }

    public static List<JsonUploadErrorTO> createTestExternalApiJsonUploadErrorTOs(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new JsonUploadErrorTO() {{
                    setMessage("Error message");
                    setLcEntryKey("entry_key");
                    setLcEntryType("POLYGON");
                }})
                .collect(Collectors.toList());
    }
}
