package ai.datagym.application.labelIteration.models.change.update;

import java.util.ArrayList;
import java.util.List;

public class ChecklistChangeUpdateBindingModel extends LcEntryChangeUpdateBindingModel {

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
