package ai.datagym.application.testUtils;

import ai.datagym.application.project.entity.Project;
import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static ai.datagym.application.testUtils.ProjectUtils.createTestProject;

public class ReviewerUtils {
    public static final String REVIEWER_ID = "TestId " + UUID.randomUUID();
    public static final String USER_ID = "1";

    private static final Long TIME = new Date().getTime();
    private static final Project project = createTestProject(PROJECT_ID);

    private static final UserMinInfoViewModel userMinInfoViewModel = UserInfoUtils.createUserMinInfoViewModel();

    public static ProjectReviewer createTestProjectReviewer() {
        return new ProjectReviewer() {{
            setId(REVIEWER_ID);
            setUserId(USER_ID);
            setProject(project);
            setTimestamp(TIME);
        }};
    }

    public static List<ProjectReviewer> createTestProjectReviewers(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ProjectReviewer() {{
                    setId(String.valueOf(index + 1));
                    setUserId(USER_ID + index);
                    setProject(project);
                    setTimestamp(TIME);
                }})
                .collect(Collectors.toList());
    }

    public static ProjectReviewerCreateBindingModel createTestProjectReviewerCreateBindingModel() {
        return new ProjectReviewerCreateBindingModel() {{
            setProjectId(project.getId());
            setUserId(USER_ID);
        }};
    }

    public static ProjectReviewerViewModel createTestProjectReviewerViewModel() {
        return new ProjectReviewerViewModel() {{
            setReviewerId(REVIEWER_ID);
            setUserInfo(userMinInfoViewModel);
            setProjectId(project.getId());
            setTimeStamp(TIME);
        }};
    }

    public static List<ProjectReviewerViewModel> createTestProjectReviewerViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ProjectReviewerViewModel() {{
                    setReviewerId(REVIEWER_ID + index);
                    setUserInfo(userMinInfoViewModel);
                    setProjectId(project.getId() + index);
                    setTimeStamp(TIME);
                }})
                .collect(Collectors.toList());
    }
}
