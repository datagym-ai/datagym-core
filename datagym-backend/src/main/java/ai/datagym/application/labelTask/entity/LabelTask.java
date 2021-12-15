package ai.datagym.application.labelTask.entity;

import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "label_task", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class LabelTask {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_task_state",
            nullable = false,
            columnDefinition = "ENUM('BACKLOG','WAITING', 'WAITING_CHANGED', 'IN_PROGRESS', 'SKIPPED', 'COMPLETED', 'REVIEWED', 'REVIEWED_SKIP') default 'BACKLOG'")
    private LabelTaskState labelTaskState = LabelTaskState.BACKLOG;

    @JsonBackReference(value = "project_labelTask")
    @ManyToOne(optional = false, targetEntity = Project.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_labeltask_project"))
    private Project project;

    @JsonBackReference(value = "media_task")
    @ManyToOne(optional = false, targetEntity = Media.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "media_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_labeltask_media"))
    private Media media;

    @Column(name = "labeler")
    private String labeler;

    @JsonBackReference
    @ManyToOne(optional = false, targetEntity = LabelIteration.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "label_iteration_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_labeltask_labeliteration"))
    private LabelIteration labelIteration;

    @Column(name = "review_comment", length = 128)
    private String reviewComment;

    @Column(name = "is_benchmark", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isBenchmark = false;

    @Column(name = "has_json_upload", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean hasJsonUpload = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_task_type",
            nullable = false,
            columnDefinition = "ENUM('DEFAULT','BENCHMARK_MASTER', 'BENCHMARK_SLAVE') default 'DEFAULT'")
    private LabelTaskType labelTaskType = LabelTaskType.DEFAULT;

    @JsonManagedReference(value = "labeltask_entryValue")
    @OneToMany(mappedBy = "labelTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LcEntryValue> entryValues = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "pre_label_state",
            columnDefinition = "ENUM('WAITING','IN_PROGRESS', 'FINISHED', 'FAILED')")
    private PreLabelState preLabelState;

    public LabelTask() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LabelTaskState getLabelTaskState() {
        return labelTaskState;
    }

    public void setLabelTaskState(LabelTaskState labelTaskState) {
        this.labelTaskState = labelTaskState;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getLabeler() {
        return labeler;
    }

    public void setLabeler(String labeler) {
        this.labeler = labeler;
    }

    public LabelIteration getLabelIteration() {
        return labelIteration;
    }

    public void setLabelIteration(LabelIteration labelIteration) {
        this.labelIteration = labelIteration;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public boolean isBenchmark() {
        return isBenchmark;
    }

    public void setBenchmark(boolean benchmark) {
        isBenchmark = benchmark;
    }

    public boolean hasJsonUpload() { return hasJsonUpload; }

    public void setHasJsonUpload(boolean hasJsonUpload) { this.hasJsonUpload = hasJsonUpload; }

    public LabelTaskType getLabelTaskType() {
        return labelTaskType;
    }

    public void setLabelTaskType(LabelTaskType labelTaskType) {
        this.labelTaskType = labelTaskType;
    }

    public List<LcEntryValue> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(List<LcEntryValue> entryValues) {
        this.entryValues = entryValues;
    }

    public PreLabelState getPreLabelState() {
        return preLabelState;
    }

    public void setPreLabelState(PreLabelState preLabelState) {
        this.preLabelState = preLabelState;
    }

    @Override
    public String toString() {
        return "LabelTask{" +
                "id='" + id + '\'' +
                ", labelTaskState=" + labelTaskState +
                ", project=" + project +
                ", media=" + media +
                ", labeler='" + labeler + '\'' +
                ", labelIteration=" + labelIteration +
                ", reviewComment='" + reviewComment + '\'' +
                ", isBenchmark=" + isBenchmark +
                ", labelTaskType=" + labelTaskType +
                ", entryValues=" + entryValues +
                ", preLabelState=" + preLabelState +
                '}';
    }
}
