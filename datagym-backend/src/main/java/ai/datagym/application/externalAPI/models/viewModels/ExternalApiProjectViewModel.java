package ai.datagym.application.externalAPI.models.viewModels;

import ai.datagym.application.project.entity.MediaType;

import java.util.Set;

public class ExternalApiProjectViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private String description;
    private Long timestamp;
    private String labelConfigurationId;
    private String labelIterationId;
    private String owner;
    private MediaType mediaType;
    private Set<ExternalApiDatasetProjectViewModel> datasets;

    public ExternalApiProjectViewModel() {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<ExternalApiDatasetProjectViewModel> getDatasets() {
        return datasets;
    }

    public void setDatasets(Set<ExternalApiDatasetProjectViewModel> datasets) {
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

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
