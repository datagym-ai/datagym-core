package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;

import java.util.ArrayList;
import java.util.List;

public class ChecklistChangeViewModel extends LcEntryChangeViewModel {

    private List<String> checkedValues = new ArrayList<>();

    public ChecklistChangeViewModel(String id, Integer frame,
                                    FrameType frameType,
                                    List<String> checkedValues) {
        super(id, frame, frameType, LcEntryType.CHECKLIST);
        this.checkedValues = checkedValues;
    }

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
