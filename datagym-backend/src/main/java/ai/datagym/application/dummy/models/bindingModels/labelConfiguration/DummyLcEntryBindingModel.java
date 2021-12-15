package ai.datagym.application.dummy.models.bindingModels.labelConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyLcEntryBindingModel {
    @JsonIgnore
    private String id;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 56)
    private String entryKey;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 56)
    private String entryValue;

    @NotNull
    @NotEmpty
    private String type;

    private String color;

    @Pattern(regexp = "^[0-9]{1}$")
    private String shortcut;

    @Min(1)
    private Integer maxLength;

    private Map<String, String> options = new LinkedHashMap<>();

    private DummyLcEntryBindingModel parentEntry;

    private String lcEntryParentId;

    private @Valid List<DummyLcEntryBindingModel> children = new ArrayList<>();

    private boolean required;

    @JsonIgnore
    private Long timestamp;


    public DummyLcEntryBindingModel() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public DummyLcEntryBindingModel getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(DummyLcEntryBindingModel parentEntry) {
        this.parentEntry = parentEntry;
    }

    public String getLcEntryParentId() {
        return lcEntryParentId;
    }

    public void setLcEntryParentId(String lcEntryParentId) {
        this.lcEntryParentId = lcEntryParentId;
    }

    public List<DummyLcEntryBindingModel> getChildren() {
        return children;
    }

    public void setChildren(List<DummyLcEntryBindingModel> children) {
        this.children = children;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
