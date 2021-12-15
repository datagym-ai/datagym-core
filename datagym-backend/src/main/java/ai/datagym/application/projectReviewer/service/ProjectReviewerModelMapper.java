package ai.datagym.application.projectReviewer.service;

import ai.datagym.application.project.entity.Project;
import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public final class ProjectReviewerModelMapper {

    private ProjectReviewerModelMapper() {
    }

    public static ProjectReviewer mapToProjectReviewer(@Valid ProjectReviewerCreateBindingModel from) {
        ProjectReviewer to = new ProjectReviewer();
        to.setUserId(from.getUserId());
        return to;
    }

    public static ProjectReviewerViewModel mapToProjectReviewerViewModel(ProjectReviewer from, String projectId, UserMinInfoViewModel userMinInfoViewModel) {
        ProjectReviewerViewModel to = new ProjectReviewerViewModel();

        to.setReviewerId(from.getId());
        to.setUserInfo(userMinInfoViewModel);
        to.setProjectId(projectId);
        to.setTimeStamp(from.getTimestamp());

        return to;
    }

    public static List<ProjectReviewerViewModel> mapToProjectReviewerViewModelList(Project project, List<UserMinInfoViewModel> userMinInfoViewModel) {
        List<ProjectReviewerViewModel> to = new ArrayList<>();
        String projectId = project.getId();

        List<ProjectReviewer> reviewers = project.getReviewers();

        reviewers.forEach(currentReviewer -> userMinInfoViewModel.forEach(userInfo -> {
            if (userInfo.getId().equals(currentReviewer.getUserId())) {
                ProjectReviewerViewModel projectReviewerViewModel = ProjectReviewerModelMapper
                        .mapToProjectReviewerViewModel(currentReviewer, projectId, userInfo);

                to.add(projectReviewerViewModel);
            }
        }));

        return to;
    }
}
