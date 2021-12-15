package ai.datagym.application.labelIteration.models.viewModels.classification;

import ai.datagym.application.labelIteration.models.change.viewModels.ChecklistChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChecklistValueViewModel extends LcEntryValueViewModel {
    private List<String> checkedValues = new ArrayList<>();
    private boolean required = false;

    public ChecklistValueViewModel() {
    }

    public ChecklistValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                   String lcEntryId,
                                   Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                   String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                   List<LcEntryValueViewModel> children, List<String> checkedValues, boolean required,
                                   List<ChecklistChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.checkedValues = checkedValues;
        this.required = required;
    }

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
