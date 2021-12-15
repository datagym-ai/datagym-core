package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.geometry;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

import java.util.ArrayList;
import java.util.List;

public class JsonUploadErrorPolygonTO extends JsonUploadErrorTO {
    private List<JsonUploadErrorPointPojo> points = new ArrayList<>();

    public JsonUploadErrorPolygonTO() {
    }


    public List<JsonUploadErrorPointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<JsonUploadErrorPointPojo> points) {
        this.points = points;
    }
}
