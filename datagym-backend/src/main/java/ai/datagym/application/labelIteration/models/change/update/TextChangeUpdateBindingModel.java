package ai.datagym.application.labelIteration.models.change.update;

public class TextChangeUpdateBindingModel extends LcEntryChangeUpdateBindingModel {

    private String text;

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
