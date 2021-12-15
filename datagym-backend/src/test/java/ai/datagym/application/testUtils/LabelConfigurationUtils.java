package ai.datagym.application.testUtils;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;
import ai.datagym.application.project.entity.Project;

import java.util.*;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;

public class LabelConfigurationUtils {
    public static final String LC_CONFIG_ID = "TestId " + UUID.randomUUID();

    private static final Long TIME = System.currentTimeMillis();

    public static LabelConfiguration createTestLabelConfiguration(Project project) {
        return new LabelConfiguration() {{
            setId(LC_CONFIG_ID);
            setProject(project);
            setEntries(new HashSet<>());
            setTimestamp(TIME);
        }};
    }

    public static LabelConfigurationViewModel createTestLabelConfigurationViewModel() {
        return new LabelConfigurationViewModel() {{
            setId(LC_CONFIG_ID);
            setProjectId(PROJECT_ID);
            setEntries(new ArrayList<>());
        }};
    }

    public static LcConfigDeleteViewModel createTestLcConfigDeleteViewModel() {
        return new LcConfigDeleteViewModel() {{
            setConfigId(LC_CONFIG_ID);
        }};
    }

    public static LcConfigHasConfigChangedViewModel createTestLcConfigHasConfigChangedViewModel() {
        return new LcConfigHasConfigChangedViewModel() {{
            setHasLabelConfigChanged(false);
        }};
    }

    public static List<String> createTestForbiddenKeyWordsList() {
        return Arrays
                .asList("geometry", "geometry_type");
    }
}
