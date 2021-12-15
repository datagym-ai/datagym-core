package ai.datagym.application.project.models.viewModels;

import ai.datagym.application.accountmanagement.client.model.OrgDataMinTO;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.project.entity.MediaType;

import java.util.Set;

public class ProjectViewModel {
    private String id;
    private String name;
    private String shortDescription;
    private String description;
    private boolean pinned;
    private Long timestamp;
    private boolean isDeleted;
    private Long deleteTime;
    private Set<DatasetAllViewModel> datasets;
    private String labelConfigurationId;
    private String labelIterationId;
    private String owner;
    private boolean exportable = false;
    private boolean reviewActivated = false;
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

    public Set<DatasetAllViewModel> getDatasets() {
        return datasets;
    }

    public void setDatasets(Set<DatasetAllViewModel> datasets) {
        this.datasets = datasets;
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

    public boolean isExportable() {
        return exportable;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    public boolean isReviewActivated() {
        return reviewActivated;
    }

    public void setReviewActivated(boolean reviewActivated) {
        this.reviewActivated = reviewActivated;
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
        return "ProjectViewModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", pinned=" + pinned +
                ", timestamp=" + timestamp +
                ", isDeleted=" + isDeleted +
                ", deleteTime=" + deleteTime +
                ", datasets=" + datasets +
                ", labelConfigurationId='" + labelConfigurationId + '\'' +
                ", labelIterationId='" + labelIterationId + '\'' +
                ", owner='" + owner + '\'' +
                ", exportable=" + exportable +
                ", reviewActivated=" + reviewActivated +
                ", orgData=" + orgData +
                ", mediaType=" + mediaType +
                '}';
    }
}
