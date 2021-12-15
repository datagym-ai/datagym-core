package ai.datagym.application.labelIteration.models.bindingModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LcEntryValueChangeValueClassBindingModel {
    @NotNull
    @NotEmpty
    private String newLcEntryId;

    public LcEntryValueChangeValueClassBindingModel() {
    }

    public String getNewLcEntryId() {
        return newLcEntryId;
    }

    public void setNewLcEntryId(String newLcEntryId) {
        this.newLcEntryId = newLcEntryId;
    }
}
