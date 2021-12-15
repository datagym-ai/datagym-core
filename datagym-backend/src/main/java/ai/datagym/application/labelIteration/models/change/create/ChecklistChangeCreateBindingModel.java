package ai.datagym.application.labelIteration.models.change.create;

import java.util.ArrayList;
import java.util.List;

public class ChecklistChangeCreateBindingModel extends LcEntryChangeCreateBindingModel {

    private List<String> checkedValues = new ArrayList<>();

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }

    @Override
    public String toString() {
        return "ChecklistChangeCreateBindingModel{" +
                "checkedValues=" + checkedValues +
                '}';
    }
}
