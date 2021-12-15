package ai.datagym.application.labelIteration.models.change.create;

public class TextChangeCreateBindingModel extends LcEntryChangeCreateBindingModel {

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
