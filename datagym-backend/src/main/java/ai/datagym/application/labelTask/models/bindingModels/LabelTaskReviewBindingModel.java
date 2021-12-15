package ai.datagym.application.labelTask.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LabelTaskReviewBindingModel {
    @NotNull
    @NotEmpty
    @Size(min = 1, max = 36)
    private String taskId;

    @Size(max = 128, message = "ReviewComment must be less than 128 characters")
    private String reviewComment;

    public LabelTaskReviewBindingModel() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
