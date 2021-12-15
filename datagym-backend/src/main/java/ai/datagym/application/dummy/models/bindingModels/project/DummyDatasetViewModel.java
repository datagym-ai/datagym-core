package ai.datagym.application.dummy.models.bindingModels.project;

import ai.datagym.application.dummy.models.bindingModels.media.DummyMediaViewModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyDatasetViewModel {
    @JsonIgnore
    private String id;
    private String name;
    private String shortDescription;

    @JsonIgnore
    private Long timestamp;

    @JsonIgnore
    private boolean deleted;

    @JsonIgnore
    private Long deleteTime;
    private String owner;
    private List<DummyMediaViewModel> media = new ArrayList<>();
    private int projectCount;

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<DummyMediaViewModel> getMedia() {
        return media;
    }

    public void setMedia(List<DummyMediaViewModel> media) {
        this.media = media;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }
}
