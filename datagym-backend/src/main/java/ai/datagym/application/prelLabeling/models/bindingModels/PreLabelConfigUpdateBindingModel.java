package ai.datagym.application.prelLabeling.models.bindingModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PreLabelConfigUpdateBindingModel {
    private boolean activateState = false;

    private Map<String, List<PreLabelLabelMappingsBindingModel>> mappings = new HashMap<>();

    public boolean isActivateState() {
        return activateState;
    }

    public void setActivateState(boolean activateState) {
        this.activateState = activateState;
    }

    public Map<String, List<PreLabelLabelMappingsBindingModel>> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, List<PreLabelLabelMappingsBindingModel>> mappings) {
        this.mappings = mappings;
    }
}
