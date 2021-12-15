package ai.datagym.application.dummy.models.bindingModels.labelTask;

import ai.datagym.application.dummy.models.bindingModels.labelIteration.DummyLabelIterationViewModel;
import ai.datagym.application.dummy.models.bindingModels.media.DummyMediaViewModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyLabelTaskBindingModel {
    @JsonIgnore
    private String taskId;

    private String labelTaskState;
    private DummyLabelIterationViewModel labelIteration;
    private DummyMediaViewModel media;

    public DummyLabelTaskBindingModel() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLabelTaskState() {
        return labelTaskState;
    }

    public void setLabelTaskState(String labelTaskState) {
        this.labelTaskState = labelTaskState;
    }

    public DummyLabelIterationViewModel getLabelIteration() {
        return labelIteration;
    }

    public void setLabelIteration(DummyLabelIterationViewModel labelIteration) {
        this.labelIteration = labelIteration;
    }

    public DummyMediaViewModel getMedia() {
        return media;
    }

    public void setMedia(DummyMediaViewModel media) {
        this.media = media;
    }
}
