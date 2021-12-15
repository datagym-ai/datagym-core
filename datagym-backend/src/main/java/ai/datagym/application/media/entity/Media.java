package ai.datagym.application.media.entity;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.lablerRating.entity.LabelerRating;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "media", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Media {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @Column(name = "delete_time")
    private Long deleteTime;

    @JsonBackReference
    @ManyToMany(mappedBy = "media", targetEntity = Dataset.class)
    private Set<Dataset> datasets = new HashSet<>();

    @JsonManagedReference(value = "entryValue_media")
    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LcEntryValue> entryValues = new HashSet<>();

    @JsonManagedReference(value = "media_task")
    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabelTask> labelTasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaSourceType mediaSourceType = MediaSourceType.LOCAL;

    @Column(name = "media_name", nullable = false)
    private String mediaName;

    @JsonManagedReference(value = "labeler_rating_media")
    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabelerRating> labelerRatings = new ArrayList<>();

    @Column(name = "valid", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean valid = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "invalid_media_reason",
            columnDefinition = "ENUM('INVALID_MIME_TYPE','INVALID_URL','AWS_ERROR', 'UNSUPPORTED_AWS_S3_STORAGE_CLASS')")
    private InvalidMediaReason invalidMediaReason;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public Set<LcEntryValue> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(Set<LcEntryValue> entryValues) {
        this.entryValues = entryValues;
    }

    public List<LabelTask> getLabelTasks() {
        return labelTasks;
    }

    public void setLabelTasks(List<LabelTask> labelTasks) {
        this.labelTasks = labelTasks;
    }

    public MediaSourceType getMediaSourceType() {
        return mediaSourceType;
    }

    public void setMediaSourceType(MediaSourceType mediaSourceType) {
        this.mediaSourceType = mediaSourceType;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public List<LabelerRating> getLabelerRatings() {
        return labelerRatings;
    }

    public void setLabelerRatings(List<LabelerRating> labelerRatings) {
        this.labelerRatings = labelerRatings;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public InvalidMediaReason getInvalidMediaReason() {
        return invalidMediaReason;
    }

    public void setInvalidMediaReason(InvalidMediaReason invalidMediaReason) {
        this.invalidMediaReason = invalidMediaReason;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", deleted=" + deleted +
                ", deleteTime=" + deleteTime +
                ", datasets=" + datasets +
                ", entryValues=" + entryValues +
                ", labelTasks=" + labelTasks +
                ", mediaSourceType=" + mediaSourceType +
                ", mediaName='" + mediaName + '\'' +
                ", labelerRatings=" + labelerRatings +
                ", valid=" + valid +
                ", invalidMediaReason=" + invalidMediaReason +
                '}';
    }
}


