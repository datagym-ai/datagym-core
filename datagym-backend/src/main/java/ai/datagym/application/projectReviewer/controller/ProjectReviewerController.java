package ai.datagym.application.projectReviewer.controller;

import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.projectReviewer.service.ProjectReviewerService;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/reviewer")
@Validated
public class ProjectReviewerController {
    private final ProjectReviewerService projectReviewerService;

    @Autowired
    public ProjectReviewerController(ProjectReviewerService projectReviewerService) {
        this.projectReviewerService = projectReviewerService;
    }

    @PostMapping()
    public ProjectReviewerViewModel createReviewer(@RequestBody @Valid ProjectReviewerCreateBindingModel projectReviewerCreateBindingModel) throws IOException {
        return projectReviewerService.createReviewer(projectReviewerCreateBindingModel);
    }

    @DeleteMapping("/{reviewerId}")
    public void deleteReviewerFromProject(@PathVariable @NotBlank @Length(min = 1) String reviewerId) {
        projectReviewerService.deleteReviewerFromProject(reviewerId);
    }

    @GetMapping("/{projectId}")
    public List<ProjectReviewerViewModel> getAllReviewerForProject(@PathVariable("projectId") String projectId) throws IOException {
        return projectReviewerService.getAllReviewerForProject(projectId);
    }

    @GetMapping("/{projectId}/possible")
    public List<UserMinInfoViewModel> getAllPossibleReviewerForProject(@PathVariable("projectId") String projectId) throws IOException {
        return projectReviewerService.getAllPossibleReviewerForProject(projectId);
    }
}
