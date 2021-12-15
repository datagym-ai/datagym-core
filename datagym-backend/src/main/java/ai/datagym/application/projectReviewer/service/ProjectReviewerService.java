package ai.datagym.application.projectReviewer.service;

import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;

import java.io.IOException;
import java.util.List;

public interface ProjectReviewerService {
    ProjectReviewerViewModel createReviewer(ProjectReviewerCreateBindingModel projectReviewerCreateBindingModel) throws IOException;

    void deleteReviewerFromProject(String reviewerId);

    List<ProjectReviewerViewModel> getAllReviewerForProject(String projectId) throws IOException;

    List<UserMinInfoViewModel> getAllPossibleReviewerForProject(String projectId) throws IOException;
}
