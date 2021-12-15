package ai.datagym.application.labelIteration.entity.classification;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "text_change_value")
public class LcEntryTextChangeValue extends LcEntryValueChange {
    @Column(name = "text")
    private String text;

    public LcEntryTextChangeValue() {
        super();
    }

    public LcEntryTextChangeValue(LcEntryValue lcEntryValue,
                                  LcEntryValue lcEntryRootValue,
                                  Integer frame, FrameType frameType,
                                  String labeler, String text) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "LcEntryTextChangeValue{" +
                "text='" + text + '\'' +
                '}';
    }
}
