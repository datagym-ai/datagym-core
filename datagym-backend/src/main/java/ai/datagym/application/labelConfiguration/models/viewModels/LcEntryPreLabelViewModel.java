package ai.datagym.application.labelConfiguration.models.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;

public class LcEntryPreLabelViewModel {
    private String id;
    private String entryKey;
    private String entryValue;
    private LcEntryType type;

    public LcEntryPreLabelViewModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }

    public LcEntryType getType() {
        return type;
    }

    public void setType(LcEntryType type) {
        this.type = type;
    }
}
