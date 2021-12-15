package ai.datagym.application.externalAPI.models.viewModels;

import ai.datagym.application.project.entity.MediaType;

import java.util.HashSet;
import java.util.Set;

public class ExternalApiDatasetViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private Long timestamp;
    private String owner;
    private int projectCount;
    private MediaType mediaType;
    private Set<ExternalApiMediaViewModel> media = new HashSet<>();

    public ExternalApiDatasetViewModel() {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public Set<ExternalApiMediaViewModel> getMedia() {
        return media;
    }

    public void setMedia(Set<ExternalApiMediaViewModel> media) {
        this.media = media;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
