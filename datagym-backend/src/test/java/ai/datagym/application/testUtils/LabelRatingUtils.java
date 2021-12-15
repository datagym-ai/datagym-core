package ai.datagym.application.testUtils;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.models.viewModels.LabelerRatingViewModel;
import ai.datagym.application.project.entity.Project;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static ai.datagym.application.testUtils.ReviewerUtils.USER_ID;

public class LabelRatingUtils {
    public static final String LABELER_RATING_ID = "TestId " + UUID.randomUUID();

    private static final Long TIME = new Date().getTime();

    private static final LabelConfiguration labelConfiguration = new LabelConfiguration();
    private static final LabelIteration labelIteration = new LabelIteration();

    public static LabelerRating createTestLabelerRating() {
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        return new LabelerRating() {{
            setId(LABELER_RATING_ID);
            setProject(testProject);
            setLabeler(USER_ID);
            setPositive(0);
            setNegative(0);
        }};
    }

    public static LabelerRatingUpdateBindingModel createTestLabelerRatingUpdateBindingModel() {
        return new LabelerRatingUpdateBindingModel() {{
            setLabelerId(USER_ID);
            setProjectId(PROJECT_ID);
            setMediaId(IMAGE_ID);
        }};
    }

    public static LabelerRatingViewModel createTestLabelerRatingViewModel() {
        return new LabelerRatingViewModel() {{
            setId(LABELER_RATING_ID);
            setProjectId(PROJECT_ID);
            setLabelerId(USER_ID);
            setPositive(0);
            setNegative(0);
        }};
    }

    public static List<LabelerRatingViewModel> createTestLabelerRatingViewModel(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LabelerRatingViewModel() {{
                    setId(LABELER_RATING_ID + index);
                    setProjectId(PROJECT_ID);
                    setLabelerId(USER_ID);
                    setPositive(0);
                    setNegative(0);
                }})
                .collect(Collectors.toList());
    }
}
