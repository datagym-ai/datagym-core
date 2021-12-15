package ai.datagym.application.project.entity;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.prelLabeling.entity.PreLabelConfiguration;
import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "project", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class Project {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_description", length = 128, nullable = false)
    private String shortDescription;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @Column(name = "delete_time")
    private Long deleteTime;

    @Column(name = "pinned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean pinned;

    @Column(name = "reviewActivated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean reviewActivated;

    @JsonManagedReference
    @ManyToMany
    @JoinTable(name = "project_dataset",
            joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_project_dataset"),
            inverseJoinColumns = @JoinColumn(name = "dataset_id", referencedColumnName = "id"),
            inverseForeignKey = @ForeignKey(name = "fk_dataset_project"))
    private Set<Dataset> datasets = new HashSet<>();

    @JsonManagedReference
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "label_configuration_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_project_labelconfiguration"))
    private LabelConfiguration labelConfiguration;

    @JsonManagedReference
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "label_iteration_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_project_labeliteration"))
    private LabelIteration labelIteration;

    @Column(name = "owner_id", nullable = false)
    private String owner;

    @JsonManagedReference(value = "project_labelTask")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabelTask> labelTasks = new ArrayList<>();

    @JsonManagedReference(value = "project_reviewer")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectReviewer> reviewers = new ArrayList<>();

    @JsonManagedReference(value = "labeler_rating_project")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabelerRating> labelerRatings = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "pre_label_configuration_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_project_prelabelconfiguration"))
    private PreLabelConfiguration preLabelConfiguration;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false,
            columnDefinition = "ENUM('IMAGE', 'VIDEO') default 'IMAGE'")
    private MediaType mediaType = MediaType.IMAGE;

    public String getId() {
        return this.id;
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

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public LabelConfiguration getLabelConfiguration() {
        return labelConfiguration;
    }

    public void setLabelConfiguration(LabelConfiguration labelConfiguration) {
        this.labelConfiguration = labelConfiguration;
    }

    public LabelIteration getLabelIteration() {
        return labelIteration;
    }

    public void setLabelIteration(LabelIteration labelIteration) {
        this.labelIteration = labelIteration;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<LabelTask> getLabelTasks() {
        return labelTasks;
    }

    public void setLabelTasks(List<LabelTask> labelTasks) {
        this.labelTasks = labelTasks;
    }

    public List<ProjectReviewer> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<ProjectReviewer> reviewers) {
        this.reviewers = reviewers;
    }

    public List<LabelerRating> getLabelerRatings() {
        return labelerRatings;
    }

    public void setLabelerRatings(List<LabelerRating> labelerRatings) {
        this.labelerRatings = labelerRatings;
    }

    public boolean isReviewActivated() {
        return reviewActivated;
    }

    public void setReviewActivated(boolean reviewActivated) {
        this.reviewActivated = reviewActivated;
    }

    public PreLabelConfiguration getPreLabelConfiguration() {
        return preLabelConfiguration;
    }

    public void setPreLabelConfiguration(PreLabelConfiguration preLabelConfiguration) {
        this.preLabelConfiguration = preLabelConfiguration;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", deleted=" + deleted +
                ", deleteTime=" + deleteTime +
                ", pinned=" + pinned +
                ", reviewActivated=" + reviewActivated +
                ", datasets=" + datasets +
                ", labelConfiguration=" + labelConfiguration +
                ", labelIteration=" + labelIteration +
                ", owner='" + owner + '\'' +
                ", labelTasks=" + labelTasks +
                ", reviewers=" + reviewers +
                ", labelerRatings=" + labelerRatings +
                ", preLabelConfiguration=" + preLabelConfiguration +
                ", mediaType=" + mediaType +
                '}';
    }
}
