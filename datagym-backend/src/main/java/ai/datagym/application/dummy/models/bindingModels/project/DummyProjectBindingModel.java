package ai.datagym.application.dummy.models.bindingModels.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyProjectBindingModel {
    @JsonIgnore
    private String id;
    private String name;
    private String shortDescription;
    private String description;
    private boolean pinned;
    @JsonIgnore
    private Long timestamp;

    @JsonIgnore
    private boolean deleted;

    @JsonIgnore
    private Long deleteTime;
    @JsonIgnore
    private List<DummyDatasetViewModel> datasets = new ArrayList<>();
    private String labelConfigurationId;
    private String labelIterationId;
    private String owner;
    private String exportable;

    public DummyProjectBindingModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public List<DummyDatasetViewModel> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DummyDatasetViewModel> datasets) {
        this.datasets = datasets;
    }

    public String getLabelConfigurationId() {
        return labelConfigurationId;
    }

    public void setLabelConfigurationId(String labelConfigurationId) {
        this.labelConfigurationId = labelConfigurationId;
    }

    public String getLabelIterationId() {
        return labelIterationId;
    }

    public void setLabelIterationId(String labelIterationId) {
        this.labelIterationId = labelIterationId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getExportable() {
        return exportable;
    }

    public void setExportable(String exportable) {
        this.exportable = exportable;
    }
}
