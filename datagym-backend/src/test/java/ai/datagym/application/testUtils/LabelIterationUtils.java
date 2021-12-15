package ai.datagym.application.testUtils;

import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.project.entity.Project;

import java.util.ArrayList;
import java.util.UUID;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;

public class LabelIterationUtils {
    public static final String LC_ITERATION_ID = "TestId " + UUID.randomUUID();

    private static final Long TIME = System.currentTimeMillis();

    public static LabelIteration createTestLabelIteration(Project project) {
        return new LabelIteration() {{
            setId(LC_ITERATION_ID);
            setProject(project);
            setTimestamp(TIME);
            setRun(1);
        }};
    }

    public static LabelIterationViewModel createTestLabelIterationViewModel() {
        return new LabelIterationViewModel() {{
            setId(LC_ITERATION_ID);
            setProjectId(PROJECT_ID);
            setRun(1);
            setEntryValues(new ArrayList<>());
        }};
    }
}
