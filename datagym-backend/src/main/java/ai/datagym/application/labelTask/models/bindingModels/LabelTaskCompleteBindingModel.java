package ai.datagym.application.labelTask.models.bindingModels;

import javax.validation.constraints.NotNull;

public class LabelTaskCompleteBindingModel {
    @NotNull
    private Long lastChangedConfig;

    public LabelTaskCompleteBindingModel() {
    }

    public Long getLastChangedConfig() {
        return lastChangedConfig;
    }

    public void setLastChangedConfig(Long lastChangedConfig) {
        this.lastChangedConfig = lastChangedConfig;
    }
}
