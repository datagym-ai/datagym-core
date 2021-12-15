package ai.datagym.application.labelConfiguration.models.viewModels;

import java.util.ArrayList;
import java.util.List;

public class LabelConfigurationViewModel {
    private String id;

    private String projectId;

    private List<LcEntryViewModel> entries = new ArrayList<>();

    private Integer numberOfCompletedTasks;

    private Integer numberOfReviewedTasks;

    public LabelConfigurationViewModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LcEntryViewModel> getEntries() {
        return entries;
    }

    public void setEntries(List<LcEntryViewModel> entries) {
        this.entries = entries;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Integer getNumberOfCompletedTasks() { return numberOfCompletedTasks;}

    public void setNumberOfCompletedTasks(Integer numberOfCompletedTasks){ this.numberOfCompletedTasks = numberOfCompletedTasks;}

    public Integer getNumberOfReviewedTasks() { return numberOfReviewedTasks;}

    public void setNumberOfReviewedTasks(Integer numberOfReviewedTasks){ this.numberOfReviewedTasks = numberOfReviewedTasks;}
}
