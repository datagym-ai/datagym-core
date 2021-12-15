package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.LcEntryValue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "text_value")
public class LcEntryTextValue extends LcEntryValue {
    @Column(name = "text")
    private String text;

    public LcEntryTextValue() {
        super();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
