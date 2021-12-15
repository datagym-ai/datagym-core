package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.geometry;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

public class JsonUploadErrorRectangleTO extends JsonUploadErrorTO {
    private String x;
    private String y;
    private String w;
    private String h;

    public JsonUploadErrorRectangleTO() {
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

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }
}
