package ai.datagym.application.prelLabeling.models.bindingModels;

public class PreLabelLabelMappingsBindingModel {
    private String preLabelClassKey;
    private String preLabelModel;

    public String getPreLabelClassKey() {
        return preLabelClassKey;
    }

    public void setPreLabelClassKey(String preLabelClassKey) {
        this.preLabelClassKey = preLabelClassKey;
    }

    public String getPreLabelModel() {
        return preLabelModel;
    }

    public void setPreLabelModel(String preLabelModel) {
        this.preLabelModel = preLabelModel;
    }
}
