package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;

public class TextChangeViewModel extends LcEntryChangeViewModel {

    private String text;

    public TextChangeViewModel(String id, Integer frame, FrameType frameType,
                               String text) {
        super(id, frame, frameType, LcEntryType.FREETEXT);
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
        return "TextChangeCreateBindingModel{" +
                "text='" + text + '\'' +
                '}';
    }
}
