package ai.datagym.application.labelTask.models.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LabelTaskMoveAllBindingModel {
    @NotNull
    @NotEmpty
    private String projectId;

    @NotNull
    @NotEmpty
    private String datasetId;

    @NotNull
    @NotEmpty
    private String direction;

    public LabelTaskMoveAllBindingModel() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
