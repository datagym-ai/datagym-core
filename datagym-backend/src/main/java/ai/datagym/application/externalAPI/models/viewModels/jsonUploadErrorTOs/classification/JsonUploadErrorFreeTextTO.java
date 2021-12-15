package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.classification;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

public class JsonUploadErrorFreeTextTO extends JsonUploadErrorTO {
    private String text;

    public JsonUploadErrorFreeTextTO() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
