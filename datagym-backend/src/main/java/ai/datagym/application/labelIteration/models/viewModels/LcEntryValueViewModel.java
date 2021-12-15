package ai.datagym.application.labelIteration.models.viewModels;

import ai.datagym.application.labelIteration.models.change.viewModels.LcEntryChangeViewModel;

import java.util.ArrayList;
import java.util.List;

public abstract class LcEntryValueViewModel {
    private String id;
    private String lcEntryValueParentId;
    private String labelIterationId;
    private String mediaId;
    private String lcEntryId;
    private Long timestamp;
    private String labeler;
    private boolean valid = false;

    private String configurationId;
    private String entryTypeLcEntry;
    private String entryKeyLcEntry;
    private String entryValueLcEntry;

    private String comment;
    private List<? extends LcEntryChangeViewModel> change;

    private List<LcEntryValueViewModel> children = new ArrayList<>();

    public LcEntryValueViewModel() {
    }

    public LcEntryValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                 String lcEntryId,
                                 Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                 String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                 List<LcEntryValueViewModel> children, List<? extends LcEntryChangeViewModel> change) {
        this.id = id;
        this.lcEntryValueParentId = lcEntryValueParentId;
        this.labelIterationId = labelIterationId;
        this.mediaId = mediaId;
        this.lcEntryId = lcEntryId;
        this.timestamp = timestamp;
        this.labeler = labeler;
        this.configurationId = configurationId;
        this.entryTypeLcEntry = entryTypeLcEntry;
        this.entryKeyLcEntry = entryKeyLcEntry;
        this.entryValueLcEntry = entryValueLcEntry;
        this.children = children;
        this.change = change;
        this.valid = valid;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLcEntryValueParentId() {
        return lcEntryValueParentId;
    }

    public void setLcEntryValueParentId(String lcEntryValueParentId) {
        this.lcEntryValueParentId = lcEntryValueParentId;
    }

    public String getLabelIterationId() {
        return labelIterationId;
    }

    public void setLabelIterationId(String labelIterationId) {
        this.labelIterationId = labelIterationId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getLcEntryId() {
        return lcEntryId;
    }

    public void setLcEntryId(String lcEntryId) {
        this.lcEntryId = lcEntryId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabeler() {
        return labeler;
    }

    public void setLabeler(String labeler) {
        this.labeler = labeler;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getEntryTypeLcEntry() {
        return entryTypeLcEntry;
    }

    public void setEntryTypeLcEntry(String entryTypeLcEntry) {
        this.entryTypeLcEntry = entryTypeLcEntry;
    }

    public String getEntryKeyLcEntry() {
        return entryKeyLcEntry;
    }

    public void setEntryKeyLcEntry(String entryKeyLcEntry) {
        this.entryKeyLcEntry = entryKeyLcEntry;
    }

    public String getEntryValueLcEntry() {
        return entryValueLcEntry;
    }

    public void setEntryValueLcEntry(String entryValueLcEntry) {
        this.entryValueLcEntry = entryValueLcEntry;
    }

    public List<? extends LcEntryChangeViewModel> getChange() {
        return change;
    }

    public void setChange(
            List<? extends LcEntryChangeViewModel> change) {
        this.change = change;
    }

    public List<LcEntryValueViewModel> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryValueViewModel> children) {
        this.children = children;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
