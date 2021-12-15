package ai.datagym.application.project.models.viewModels;

import ai.datagym.application.project.entity.MediaType;

public class ProjectDatasetViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private String description;
    private boolean pinned;
    private Long timestamp;
    private boolean isDeleted;
    private Long deleteTime;
    private String owner;
    private boolean reviewActivated = false;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
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

    public boolean isReviewActivated() {
        return reviewActivated;
    }

    public void setReviewActivated(boolean reviewActivated) {
        this.reviewActivated = reviewActivated;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "ProjectDatasetViewModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", pinned=" + pinned +
                ", timestamp=" + timestamp +
                ", isDeleted=" + isDeleted +
                ", deleteTime=" + deleteTime +
                ", owner='" + owner + '\'' +
                ", reviewActivated=" + reviewActivated +
                ", mediaType=" + mediaType +
                '}';
    }
}
