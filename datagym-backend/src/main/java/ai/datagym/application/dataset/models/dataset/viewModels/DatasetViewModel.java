package ai.datagym.application.dataset.models.dataset.viewModels;

import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.project.entity.MediaType;

import java.util.HashSet;
import java.util.Set;

public class DatasetViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private Long timestamp;
    private boolean deleted;
    private Long deleteTime;
    private String owner;
    private int projectCount;
    private boolean allowPublicUrls = true;
    private Set<MediaViewModel> media = new HashSet<>();
    private MediaType mediaType;

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

    public int getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isAllowPublicUrls() {
        return allowPublicUrls;
    }

    public void setAllowPublicUrls(boolean allowPublicUrls) {
        this.allowPublicUrls = allowPublicUrls;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
