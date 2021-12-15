package ai.datagym.application.labelTask.models.viewModels;

public class LabelTaskCompleteViewModel {
    private boolean hasLabelConfigChanged = false;
    private String currentTaskId;

    public LabelTaskCompleteViewModel() {
    }

    public boolean isHasLabelConfigChanged() {
        return hasLabelConfigChanged;
    }

    public void setHasLabelConfigChanged(boolean hasLabelConfigChanged) {
        this.hasLabelConfigChanged = hasLabelConfigChanged;
    }

    public String getCurrentTaskId() {
        return currentTaskId;
    }

    public void setCurrentTaskId(String currentTaskId) {
        this.currentTaskId = currentTaskId;
    }
}
