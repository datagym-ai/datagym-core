package ai.datagym.application.labelConfiguration.models.bindingModels;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LcEntryUpdateBindingModel {
    @Size(min = 1, max = 36)
    private String id;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 56, message = "Entry Key must be less than 56 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ -]*$")
    private String entryKey;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 56, message = "Entry value must be less than 56 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ !@#$%^&*()?\\-àâçéèêëîïôûùüÿñæœäöüß]*$")
    private String entryValue;

    @NotNull
    @NotEmpty
    private String type;

    private String color;

    @Pattern(regexp = "^[0-9]{1}$")
    private String shortcut;

    @Min(1)
    @Max(255)
    private Integer maxLength;

    private Map<String, String> options = new LinkedHashMap<>();

    private LcEntryUpdateBindingModel parentEntry;

    private String lcEntryParentId;

    private @Valid List<LcEntryUpdateBindingModel> children = new ArrayList<>();

    private boolean required;

    public LcEntryUpdateBindingModel() {}

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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
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

    public LcEntryUpdateBindingModel getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(LcEntryUpdateBindingModel parentEntry) {
        this.parentEntry = parentEntry;
    }

    public List<LcEntryUpdateBindingModel> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryUpdateBindingModel> children) {
        this.children = children;
    }

    public String getLcEntryParentId() {
        return lcEntryParentId;
    }

    public void setLcEntryParentId(String lcEntryParentId) {
        this.lcEntryParentId = lcEntryParentId;
    }
}
