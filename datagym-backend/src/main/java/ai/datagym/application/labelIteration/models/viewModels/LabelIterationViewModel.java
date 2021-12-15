package ai.datagym.application.labelIteration.models.viewModels;

import java.util.ArrayList;
import java.util.List;

public class LabelIterationViewModel {
    private String id;

    private String projectId;

    private int run;

    private List<LcEntryValueViewModel> entryValues = new ArrayList<>();

    public LabelIterationViewModel() {}

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

    public List<LcEntryValueViewModel> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(List<LcEntryValueViewModel> entryValues) {
        this.entryValues = entryValues;
    }
}
