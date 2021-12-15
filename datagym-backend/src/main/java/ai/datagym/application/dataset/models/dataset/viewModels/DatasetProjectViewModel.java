package ai.datagym.application.dataset.models.dataset.viewModels;

import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.project.models.viewModels.ProjectDatasetViewModel;

import java.util.HashSet;
import java.util.Set;

public class DatasetProjectViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private Long timestamp;
    private boolean deleted;
    private Long deleteTime;
    private String owner;
    private Set<MediaViewModel> media = new HashSet<>();
    private Set<ProjectDatasetViewModel> projects = new HashSet<>();

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

    public Set<MediaViewModel> getMedia() {
        return media;
    }

    public void setMedia(Set<MediaViewModel> media) {
        this.media = media;
    }

    public Set<ProjectDatasetViewModel> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectDatasetViewModel> projects) {
        this.projects = projects;
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
}
