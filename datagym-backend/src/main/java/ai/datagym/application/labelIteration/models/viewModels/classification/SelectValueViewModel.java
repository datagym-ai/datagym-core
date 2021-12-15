package ai.datagym.application.labelIteration.models.viewModels.classification;

import ai.datagym.application.labelIteration.models.change.viewModels.SelectChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.List;

public class SelectValueViewModel extends LcEntryValueViewModel {
    private String selectKey;
    private boolean required = false;

    public SelectValueViewModel() {
    }

    public SelectValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                String lcEntryId,
                                Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                List<LcEntryValueViewModel> children, String selectKey, boolean required,
                                List<SelectChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.selectKey = selectKey;
        this.required = required;
    }

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
