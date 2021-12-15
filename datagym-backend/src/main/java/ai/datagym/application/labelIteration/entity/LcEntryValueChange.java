package ai.datagym.application.labelIteration.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lc_entry_value_change")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class LcEntryValueChange {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @ManyToOne(optional = false, targetEntity = LcEntryValue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_entry_value_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_entryvaluechange_lcentryvalue"))
    private LcEntryValue lcEntryValue;

    @ManyToOne(optional = false, targetEntity = LcEntryValue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_entry_root_value_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_entryvaluechange_lcentryrootvalue"))
    private LcEntryValue lcEntryRootValue;

    @Column(name = "frame", nullable = true)
    private Integer frame;

    @Enumerated(EnumType.STRING)
    @Column(name = "frame_type", nullable = true)
    private FrameType frameType;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "labeler", nullable = false)
    private String labeler;

    public LcEntryValueChange() {
    }

    public LcEntryValueChange(LcEntryValue lcEntryValue,
                              LcEntryValue lcEntryRootValue, Integer frame,
                              FrameType frameType, String labeler) {
        this.lcEntryValue = lcEntryValue;
        this.lcEntryRootValue = lcEntryRootValue;
        this.frame = frame;
        this.frameType = frameType;
        this.timestamp = System.currentTimeMillis();
        this.labeler = labeler;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LcEntryValue getLcEntryValue() {
        return lcEntryValue;
    }

    public void setLcEntryValue(LcEntryValue lcEntryValue) {
        this.lcEntryValue = lcEntryValue;
    }

    public LcEntryValue getLcEntryRootValue() {
        return lcEntryRootValue;
    }

    public void setLcEntryRootValue(LcEntryValue lcEntryRootValue) {
        this.lcEntryRootValue = lcEntryRootValue;
    }

    public Integer getFrame() {
        return frame;
    }

    public void setFrame(Integer frame) {
        this.frame = frame;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
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

    @Override
    public String toString() {
        return "LcEntryValueChange{" +
                "id='" + id + '\'' +
                ", lcEntryValue=" + lcEntryValue +
                ", lcEntryRootValue=" + lcEntryRootValue +
                ", frame=" + frame +
                ", frameType=" + frameType +
                ", timestamp=" + timestamp +
                ", labeler='" + labeler + '\'' +
                '}';
    }
}
