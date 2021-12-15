package ai.datagym.application.dataset.models.dataset.viewModels;

import ai.datagym.application.accountmanagement.client.model.OrgDataMinTO;
import ai.datagym.application.project.entity.MediaType;

public class DatasetAllViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private Long timestamp;
    private boolean deleted;
    private Long deleteTime;
    private String owner;
    private int mediaCount;
    private int projectCount;
    // Only for super-admin view
    private OrgDataMinTO orgData;
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

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public OrgDataMinTO getOrgData() {
        return orgData;
    }

    public void setOrgData(OrgDataMinTO orgData) {
        this.orgData = orgData;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "DatasetAllViewModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", timestamp=" + timestamp +
                ", deleted=" + deleted +
                ", deleteTime=" + deleteTime +
                ", owner='" + owner + '\'' +
                ", mediaCount=" + mediaCount +
                ", projectCount=" + projectCount +
                ", orgData=" + orgData +
                ", mediaType=" + mediaType +
                '}';
    }
}
