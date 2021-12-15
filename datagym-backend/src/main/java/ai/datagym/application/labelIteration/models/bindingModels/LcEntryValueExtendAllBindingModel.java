package ai.datagym.application.labelIteration.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LcEntryValueExtendAllBindingModel {
    @NotNull
    @NotEmpty
    private String iterationId;

    @NotNull
    @NotEmpty
    private String mediaId;

    @NotNull
    @NotEmpty
    private String labelTaskId;

    public LcEntryValueExtendAllBindingModel() {
    }

    public String getIterationId() {
        return iterationId;
    }

    public void setIterationId(String iterationId) {
        this.iterationId = iterationId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getLabelTaskId() {
        return labelTaskId;
    }

    public void setLabelTaskId(String labelTaskId) {
        this.labelTaskId = labelTaskId;
    }
}
