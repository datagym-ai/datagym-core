package ai.datagym.application.dummy.models.bindingModels.labelConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyConfigBindingModel {
    private String id;

    private String projectId;

    private List<DummyLcEntryBindingModel> entries = new ArrayList<>();

    public DummyConfigBindingModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DummyLcEntryBindingModel> getEntries() {
        return entries;
    }

    public void setEntries(List<DummyLcEntryBindingModel> entries) {
        this.entries = entries;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
