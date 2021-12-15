package ai.datagym.application.labelConfiguration.entity.classification;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "check_list")
public class LcEntryChecklist extends LcEntryClassification implements Serializable {
    @ElementCollection
    @JoinTable(name = "lc_entry_check_list_options",
            joinColumns = @JoinColumn(name = "lc_entry_check_list_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_checklistoptions_checklist"))
    @MapKeyColumn(name = "lc_entry_check_list_key")
    @Column(name = "lc_entry_check_list_value")
    private Map<String, String> options = new LinkedHashMap<>();

    public LcEntryChecklist() {
        super();
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LcEntryChecklist)) return false;
        LcEntryChecklist that = (LcEntryChecklist) o;
        return getOptions().equals(that.getOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOptions());
    }
}
