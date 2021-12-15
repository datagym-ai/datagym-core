package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.LcEntryValue;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(value = "checklist_value")
public class LcEntryCheckListValue extends LcEntryValue {
    @ElementCollection
    @CollectionTable(name = "lc_entry_checklist_values", joinColumns = @JoinColumn(name = "checklist_value_id"),
            foreignKey = @ForeignKey(name = "fk_checklistvalues_checklistvalue"))
    @Column(name = "checked_value")
    private List<String> checkedValues = new ArrayList<>();

    public LcEntryCheckListValue() {
        super();
    }

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }
}
