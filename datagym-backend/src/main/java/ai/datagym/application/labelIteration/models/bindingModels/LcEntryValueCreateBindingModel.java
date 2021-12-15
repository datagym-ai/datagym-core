package ai.datagym.application.labelIteration.models.bindingModels;

import ai.datagym.application.labelIteration.entity.LcEntryValue;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LcEntryValueCreateBindingModel {
    @NotNull
    @NotEmpty
    private String iterationId;

    @NotNull
    @NotEmpty
    private String mediaId;

    @NotNull
    @NotEmpty
    private String labelTaskId;

    @NotNull
    @NotEmpty
    private String lcEntryId;

    private String lcEntryValueParentId;

    private LcEntryValue lcEntryValueParent;

    public LcEntryValueCreateBindingModel() {
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

    public String getLcEntryValueParentId() {
        return lcEntryValueParentId;
    }

    public void setLcEntryValueParentId(String lcEntryValueParentId) {
        this.lcEntryValueParentId = lcEntryValueParentId;
    }

    public String getLabelTaskId() {
        return labelTaskId;
    }

    public void setLabelTaskId(String labelTaskId) {
        this.labelTaskId = labelTaskId;
    }

    public LcEntryValue getLcEntryValueParent() {
        return lcEntryValueParent;
    }

    public void setLcEntryValueParent(LcEntryValue lcEntryValueParent) {
        this.lcEntryValueParent = lcEntryValueParent;
    }
}
