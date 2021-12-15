package ai.datagym.application.labelIteration.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LcEntryValueExtendBindingModel {
    @NotNull
    @NotEmpty
    private String iterationId;

    @NotNull
    @NotEmpty
    private String mediaId;

    @NotNull
    @NotEmpty
    private String lcEntryId;

    @NotNull
    @NotEmpty
    private String labelTaskId;

    private String lcEntryParentId;

    public LcEntryValueExtendBindingModel() {
    }

    public String getLcEntryId() {
        return lcEntryId;
    }

    public void setLcEntryId(String lcEntryId) {
        this.lcEntryId = lcEntryId;
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

    public String getLcEntryParentId() {
        return lcEntryParentId;
    }

    public void setLcEntryParentId(String lcEntryParentId) {
        this.lcEntryParentId = lcEntryParentId;
    }

    public String getLabelTaskId() {
        return labelTaskId;
    }

    public void setLabelTaskId(String labelTaskId) {
        this.labelTaskId = labelTaskId;
    }
}
