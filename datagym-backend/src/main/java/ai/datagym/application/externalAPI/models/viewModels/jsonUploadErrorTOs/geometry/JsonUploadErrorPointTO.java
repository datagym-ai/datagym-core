package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.geometry;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

public class JsonUploadErrorPointTO extends JsonUploadErrorTO {
    private String x;
    private String y;

    public JsonUploadErrorPointTO() {
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
