package ai.datagym.application.lablerRating.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LabelerRatingUpdateBindingModel {
    @NotNull
    @NotEmpty
    private String labelerId;

    @NotNull
    @NotEmpty
    private String projectId;

    @NotNull
    @NotEmpty
    private String mediaId;

    public LabelerRatingUpdateBindingModel() {
    }

    public String getLabelerId() {
        return labelerId;
    }

    public void setLabelerId(String labelerId) {
        this.labelerId = labelerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}
