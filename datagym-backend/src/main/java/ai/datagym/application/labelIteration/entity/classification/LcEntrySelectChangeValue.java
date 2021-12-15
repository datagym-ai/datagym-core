package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "select_change_value")
public class LcEntrySelectChangeValue extends LcEntryValueChange {
    @Column(name = "select_key")
    private String selectKey;

    public LcEntrySelectChangeValue() {
        super();
    }

    public LcEntrySelectChangeValue(LcEntryValue lcEntryValue,
                                    LcEntryValue lcEntryRootValue,
                                    Integer frame, FrameType frameType, String labeler, String selectKey) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
        this.selectKey = selectKey;
    }

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }
}
