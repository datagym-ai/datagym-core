package ai.datagym.application.prelLabeling.models.viewModels;

public class PreLabelMappingEntryViewModel {
    private String preLabelMappingId;
    private String preLabelClassKey;
    private String preLabelModel;
    private String preLabelConfigId;
    private String lcEntryId;

    public String getPreLabelMappingId() {
        return preLabelMappingId;
    }

    public void setPreLabelMappingId(String preLabelMappingId) {
        this.preLabelMappingId = preLabelMappingId;
    }

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

    public String getPreLabelConfigId() {
        return preLabelConfigId;
    }

    public void setPreLabelConfigId(String preLabelConfigId) {
        this.preLabelConfigId = preLabelConfigId;
    }

    public String getLcEntryId() {
        return lcEntryId;
    }

    public void setLcEntryId(String lcEntryId) {
        this.lcEntryId = lcEntryId;
    }
}
