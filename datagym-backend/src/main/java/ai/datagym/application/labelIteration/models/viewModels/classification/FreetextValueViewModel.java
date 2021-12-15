package ai.datagym.application.labelIteration.models.viewModels.classification;

import ai.datagym.application.labelIteration.models.change.viewModels.TextChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.List;

public class FreetextValueViewModel extends LcEntryValueViewModel {
    private String text;
    private boolean required = false;

    public FreetextValueViewModel() {
    }

    public FreetextValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                  String lcEntryId,
                                  Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                  String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                  List<LcEntryValueViewModel> children, String text, boolean required,
                                  List<TextChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.text = text;
        this.required = required;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
