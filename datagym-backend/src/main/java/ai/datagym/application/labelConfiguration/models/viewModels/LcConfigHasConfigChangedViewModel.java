package ai.datagym.application.labelConfiguration.models.viewModels;

public class LcConfigHasConfigChangedViewModel {
    private boolean hasLabelConfigChanged = false;

    public LcConfigHasConfigChangedViewModel() {
    }

    public boolean isHasLabelConfigChanged() {
        return hasLabelConfigChanged;
    }

    public void setHasLabelConfigChanged(boolean hasLabelConfigChanged) {
        this.hasLabelConfigChanged = hasLabelConfigChanged;
    }
}
