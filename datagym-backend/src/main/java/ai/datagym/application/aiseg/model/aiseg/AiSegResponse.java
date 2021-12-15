package ai.datagym.application.aiseg.model.aiseg;

import java.util.List;

public class AiSegResponse {
    private String imageId;

    private List<AiSegPoint> result;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public List<AiSegPoint> getResult() {
        return result;
    }

    public void setResult(List<AiSegPoint> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AiSegResponse{" +
                "imageId='" + imageId + '\'' +
                ", result=" + result +
                '}';
    }
}
