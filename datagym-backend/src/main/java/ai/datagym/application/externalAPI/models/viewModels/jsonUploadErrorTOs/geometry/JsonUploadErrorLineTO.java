package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.geometry;

import ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs.JsonUploadErrorTO;

import java.util.ArrayList;
import java.util.List;

public class JsonUploadErrorLineTO extends JsonUploadErrorTO {
    private List<JsonUploadErrorPointPojo> points = new ArrayList<>();

    public JsonUploadErrorLineTO() {
    }


    public List<JsonUploadErrorPointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<JsonUploadErrorPointPojo> points) {
        this.points = points;
    }
}
