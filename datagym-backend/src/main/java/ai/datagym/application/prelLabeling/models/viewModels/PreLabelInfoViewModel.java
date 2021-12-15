package ai.datagym.application.prelLabeling.models.viewModels;

import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryPreLabelViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreLabelInfoViewModel {
    boolean activePreLabeling = false;
    long countReadyTasks;
    long countWaitingTasks;
    long countFinishedTasks;
    long countFailedTasks;
    private int aiSegLimit;
    private int aiSegRemaining;
    List<LcEntryPreLabelViewModel> availableGeometries = new ArrayList<>();
    Map<String, String> availableNetworkClasses = new LinkedHashMap<>();
    List<PreLabelMappingEntryViewModel> preLabelMappings = new ArrayList<>();

    public boolean isActivePreLabeling() {
        return activePreLabeling;
    }

    public void setActivePreLabeling(boolean activePreLabeling) {
        this.activePreLabeling = activePreLabeling;
    }

    public long getCountReadyTasks() { return countReadyTasks; }

    public void setCountReadyTasks(long countReadyTasks) {  this.countReadyTasks = countReadyTasks; }

    public long getCountWaitingTasks() {
        return countWaitingTasks;
    }

    public void setCountWaitingTasks(long countWaitingTasks) {
        this.countWaitingTasks = countWaitingTasks;
    }

    public long getCountFinishedTasks() {
        return countFinishedTasks;
    }

    public void setCountFinishedTasks(long countFinishedTasks) {
        this.countFinishedTasks = countFinishedTasks;
    }

    public long getCountFailedTasks() {
        return countFailedTasks;
    }

    public void setCountFailedTasks(long countFailedTasks) {
        this.countFailedTasks = countFailedTasks;
    }

    public int getAiSegLimit() {
        return aiSegLimit;
    }

    public void setAiSegLimit(int aiSegLimit) {
        this.aiSegLimit = aiSegLimit;
    }

    public int getAiSegRemaining() {
        return aiSegRemaining;
    }

    public void setAiSegRemaining(int aiSegRemaining) {
        this.aiSegRemaining = aiSegRemaining;
    }

    public List<LcEntryPreLabelViewModel> getAvailableGeometries() {
        return availableGeometries;
    }

    public void setAvailableGeometries(List<LcEntryPreLabelViewModel> availableGeometries) {
        this.availableGeometries = availableGeometries;
    }

    public Map<String, String> getAvailableNetworkClasses() {
        return availableNetworkClasses;
    }

    public void setAvailableNetworkClasses(Map<String, String> availableNetworkClasses) {
        this.availableNetworkClasses = availableNetworkClasses;
    }

    public List<PreLabelMappingEntryViewModel> getPreLabelMappings() {
        return preLabelMappings;
    }

    public void setPreLabelMappings(List<PreLabelMappingEntryViewModel> preLabelMappings) {
        this.preLabelMappings = preLabelMappings;
    }
}
