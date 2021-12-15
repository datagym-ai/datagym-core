package ai.datagym.application.dummy.models.bindingModels.labelIteration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyLabelIterationViewModel {
    @JsonIgnore
    private String id;

    private String projectId;

    private int run;

    private List<DummyValueUpdateBindingModel> entryValues = new ArrayList<>();

    public DummyLabelIterationViewModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public List<DummyValueUpdateBindingModel> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(List<DummyValueUpdateBindingModel> entryValues) {
        this.entryValues = entryValues;
    }
}
