package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;

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
@DiscriminatorValue(value = "checklist_change_value")
public class LcEntryCheckListChangeValue extends LcEntryValueChange {

    @ElementCollection
    @CollectionTable(name = "lc_entry_checklist_change_values", joinColumns = @JoinColumn(name = "checklist_change_value_id"),
            foreignKey = @ForeignKey(name = "fk_checklistchangevalues_checklistvalue"))
    @Column(name = "checked_value")
    private List<String> checkedValues = new ArrayList<>();

    public LcEntryCheckListChangeValue() {
        super();
    }

    public LcEntryCheckListChangeValue(LcEntryValue lcEntryValue,
                                       LcEntryValue lcEntryRootValue,
                                       Integer frame, FrameType frameType, String labeler,
                                       List<String> checkedValues) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
        this.checkedValues = checkedValues;
    }

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }
}
