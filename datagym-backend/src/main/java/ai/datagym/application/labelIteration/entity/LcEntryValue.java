package ai.datagym.application.labelIteration.entity;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.media.entity.Media;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lc_entry_value", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entry_values_type", discriminatorType = DiscriminatorType.STRING)
public abstract class LcEntryValue {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @JsonBackReference(value = "entryValue_labelIteration")
    @ManyToOne(optional = false, targetEntity = LabelIteration.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "label_iteration_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_entryvalue_labeliteration"))
    private LabelIteration labelIteration;

    @JsonBackReference(value = "entryValue_media")
    @ManyToOne(optional = false, targetEntity = Media.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_entryvalue_media"))
    private Media media;

    @JsonBackReference(value = "entryValue_lcEntry")
    @ManyToOne(optional = false, targetEntity = LcEntry.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_entry_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_entryvalue_lcentrychildren"))
    private LcEntry lcEntry;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "lcEntryValueParent_Id", foreignKey = @ForeignKey(name = "fk_entryvalue_lcentryparent"))
    private LcEntryValue lcEntryValueParent;

    @JsonManagedReference
    @OneToMany(mappedBy = "lcEntryValueParent", orphanRemoval = true)
    private List<LcEntryValue> children;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "labeler", nullable = false)
    private String labeler;

    @Column(name = "is_valid", columnDefinition = "boolean default false")
    private boolean valid;

    @JsonBackReference(value = "labeltask_entryValue")
    @ManyToOne(optional = false, targetEntity = LabelTask.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "label_task_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_entryvalue_labeltask"))
    private LabelTask labelTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_source",
            nullable = false,
            columnDefinition = "ENUM('AI_PRE_LABEL','API_UPLOAD','API_KEY','USER') default 'USER'")
    private LabelSource labelSource = LabelSource.USER;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "lcEntryValue", orphanRemoval = true)
    private List<LcEntryValueChange> lcEntryValueChanges = new ArrayList<>();

    @OneToMany(mappedBy = "lcEntryRootValue", orphanRemoval = true)
    private List<LcEntryValueChange> lcEntryValueRootChanges = new ArrayList<>();

    public LcEntryValue() {
    }

    public LcEntryValue(String name) {
        this.labeler = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LabelIteration getLabelIteration() {
        return labelIteration;
    }

    public void setLabelIteration(LabelIteration labelIteration) {
        this.labelIteration = labelIteration;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public LcEntry getLcEntry() {
        return lcEntry;
    }

    public void setLcEntry(LcEntry lcEntry) {
        this.lcEntry = lcEntry;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabeler() {
        return labeler;
    }

    public void setLabeler(String labeler) {
        this.labeler = labeler;
    }

    public LcEntryValue getLcEntryValueParent() {
        return lcEntryValueParent;
    }

    public void setLcEntryValueParent(LcEntryValue lcEntryValueParent) {
        this.lcEntryValueParent = lcEntryValueParent;
    }

    public List<LcEntryValue> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryValue> children) {
        this.children = children;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public LabelTask getLabelTask() {
        return labelTask;
    }

    public void setLabelTask(LabelTask labelTask) {
        this.labelTask = labelTask;
    }

    public LabelSource getLabelSource() {
        return labelSource;
    }

    public void setLabelSource(LabelSource labelSource) {
        this.labelSource = labelSource;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public List<LcEntryValueChange> getLcEntryValueChanges() {
        return lcEntryValueChanges;
    }

    public void setLcEntryValueChanges(
            List<LcEntryValueChange> lcEntryValueChanges) {
        this.lcEntryValueChanges = lcEntryValueChanges;
    }

    public List<LcEntryValueChange> getLcEntryValueRootChanges() {
        return lcEntryValueRootChanges;
    }

    public void setLcEntryValueRootChanges(
            List<LcEntryValueChange> lcEntryValueRootChanges) {
        this.lcEntryValueRootChanges = lcEntryValueRootChanges;
    }

    @Override
    public String toString() {
        return "LcEntryValue{" +
                "id='" + id + '\'' +
                ", labelIteration=" + labelIteration +
                ", media=" + media +
                ", lcEntry=" + lcEntry +
                ", lcEntryValueParent=" + lcEntryValueParent +
                ", children=" + children +
                ", timestamp=" + timestamp +
                ", labeler='" + labeler + '\'' +
                ", valid=" + valid +
                ", labelTask=" + labelTask +
                ", labelSource=" + labelSource +
                ", comment='" + comment + '\'' +
                ", lcEntryValueChanges=" + lcEntryValueChanges +
                ", lcEntryValueRootChanges=" + lcEntryValueRootChanges +
                '}';
    }
}
