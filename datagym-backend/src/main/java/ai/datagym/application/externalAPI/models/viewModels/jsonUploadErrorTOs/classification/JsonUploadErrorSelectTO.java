package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.classification;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

public class JsonUploadErrorSelectTO extends JsonUploadErrorTO {
    private String selectKey;

    public JsonUploadErrorSelectTO() {
    }

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }
}
