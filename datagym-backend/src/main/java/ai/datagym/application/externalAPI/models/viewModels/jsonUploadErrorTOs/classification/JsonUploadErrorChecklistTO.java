package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.classification;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

import java.util.ArrayList;
import java.util.List;

public class JsonUploadErrorChecklistTO extends JsonUploadErrorTO {
    private List<String> checkedValues = new ArrayList<>();

    public JsonUploadErrorChecklistTO() {
    }

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }
}
