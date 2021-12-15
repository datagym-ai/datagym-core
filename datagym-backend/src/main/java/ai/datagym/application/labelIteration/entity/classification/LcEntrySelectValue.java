package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.LcEntryValue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "select_value")
public class LcEntrySelectValue extends LcEntryValue {
    @Column(name = "select_key")
    private String selectKey;

    public LcEntrySelectValue() {
        super();
    }

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }
}
