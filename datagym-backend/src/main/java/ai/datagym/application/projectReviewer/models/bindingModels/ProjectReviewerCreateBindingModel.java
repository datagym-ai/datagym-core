package ai.datagym.application.projectReviewer.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProjectReviewerCreateBindingModel {
    @NotNull
    @NotEmpty(message = "ProjectId is required.")
    @Size(min = 1, max = 56)
    private String projectId;

    @NotNull
    @NotEmpty(message = "UserId is required.")
    @Size(min = 1, max = 56)
    private String userId;

    public ProjectReviewerCreateBindingModel() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
