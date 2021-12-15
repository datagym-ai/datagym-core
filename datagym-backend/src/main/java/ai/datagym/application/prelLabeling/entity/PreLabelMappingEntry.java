package ai.datagym.application.prelLabeling.entity;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Map;

@Entity
@Table(name = "pre_label_mapping_entry", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class PreLabelMappingEntry {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;


    @Column(name = "pre_label_class_key", nullable = false)
    private String preLabelClassKey;

    @Column(name = "pre_label_model", nullable = false)
    private String preLabelModel;

    @JsonBackReference(value = "preLabelConfig_preLabelMapping")
    @ManyToOne(optional = false, targetEntity = PreLabelConfiguration.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "preLabelConfig_id", referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_prelabelmapping_prelabelconfig"))
    private PreLabelConfiguration preLabelConfig;

    @JsonBackReference(value = "lcEntry_preLabelMapping")
    @ManyToOne(optional = false, targetEntity = LcEntry.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "lcEntry_id", referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_prelabelmapping_lcentry"))
    private LcEntry lcEntry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreLabelClassKey() {
        return preLabelClassKey;
    }

    public void setPreLabelClassKey(String preLabelClassKey) {
        this.preLabelClassKey = preLabelClassKey;
    }

    public String getPreLabelModel() {
        return preLabelModel;
    }

    public void setPreLabelModel(String preLabelModel) {
        this.preLabelModel = preLabelModel;
    }

    public PreLabelConfiguration getPreLabelConfig() {
        return preLabelConfig;
    }

    public void setPreLabelConfig(PreLabelConfiguration preLabelConfig) {
        this.preLabelConfig = preLabelConfig;
    }

    public LcEntry getLcEntry() {
        return lcEntry;
    }

    public void setLcEntry(LcEntry lcEntry) {
        this.lcEntry = lcEntry;
    }


    public Map<String, String> getClassTypeMapping() {
        return Map.of(
                "name", this.preLabelModel,
                "geometry", this.lcEntry.getType().name().toLowerCase()
        );
    }
}
