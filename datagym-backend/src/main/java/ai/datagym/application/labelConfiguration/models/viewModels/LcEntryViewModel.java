package ai.datagym.application.labelConfiguration.models.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LcEntryViewModel {
    private String id;
    private String lcEntryParentId;
    private String entryKey;
    private String entryValue;
    private LcEntryType type;
    private String color;
    private String shortcut;
    private Long timestamp;
    private Integer maxLength;
    private boolean isRequired = false;
    private List<LcEntryViewModel> children = new ArrayList<>();
    private Map<String, String> options = new LinkedHashMap<>();

    public LcEntryViewModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LcEntryViewModel> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryViewModel> children) {
        this.children = children;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getLcEntryParentId() {
        return lcEntryParentId;
    }

    public void setLcEntryParentId(String lcEntryParentId) {
        this.lcEntryParentId = lcEntryParentId;
    }

}
