package ai.datagym.application.labelConfiguration.entity;

import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "lc_entry", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entry_type", discriminatorType = DiscriminatorType.STRING)
public abstract class LcEntry implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "parentEntry_id", foreignKey = @ForeignKey(name = "fk_lcentry_lcentryparent"))
    private LcEntry parentEntry;

    @JsonManagedReference
    @OneToMany(mappedBy = "parentEntry", orphanRemoval = true)
    private List<LcEntry> children;

    @JsonBackReference(value = "lcEntry_labelConfiguration")
    @ManyToOne(optional = false, targetEntity = LabelConfiguration.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "label_config_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name ="fk_lcentry_labelconfiguration"))
    private LabelConfiguration configuration;

    @Column(name = "entry_key", nullable = false, length = 56)
    private String entryKey;

    @Column(name = "entry_value", nullable = false, length = 56)
    private String entryValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "type",
            nullable = false,
            columnDefinition = "ENUM('POINT', 'LINE', 'POLYGON', 'RECTANGLE', 'SELECT', 'RADIO', 'CHECKLIST', 'FREETEXT', 'IMAGE_SEGMENTATION') " +
                    "default 'FREETEXT'")
    private LcEntryType type = LcEntryType.FREETEXT;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @JsonManagedReference(value = "entryValue_lcEntry")
    @OneToMany(mappedBy = "lcEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LcEntryValue> entryValues = new HashSet<>();

    @JsonManagedReference(value = "lcEntry_preLabelMapping")
    @OneToMany(mappedBy = "lcEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreLabelMappingEntry> preLabelMappingEntries = new ArrayList<>();

    public LcEntry() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LcEntry getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(LcEntry parentEntry) {
        this.parentEntry = parentEntry;
    }

    public List<LcEntry> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntry> children) {
        this.children = children;
    }

    public LabelConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(LabelConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }

    public LcEntryType getType() {
        return type;
    }

    public void setType(LcEntryType type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<LcEntryValue> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(Set<LcEntryValue> entryValues) {
        this.entryValues = entryValues;
    }

    public List<PreLabelMappingEntry> getPreLabelMappingEntries() {
        return preLabelMappingEntries;
    }

    public void setPreLabelMappingEntries(List<PreLabelMappingEntry> preLabelMappingEntries) {
        this.preLabelMappingEntries = preLabelMappingEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LcEntry)) return false;
        LcEntry lcEntry = (LcEntry) o;
        return getEntryKey().equals(lcEntry.getEntryKey()) &&
                getEntryValue().equals(lcEntry.getEntryValue()) &&
                getType() == lcEntry.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntryKey(), getEntryValue(), getType());
    }
}
