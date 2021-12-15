package ai.datagym.application.aiseg.model.aiseg;

import java.util.List;

public class AiSegCalculate {

    private String imageId;

    private Integer frameNumber;

    private List<AiSegPoint> negativePoints;

    private List<AiSegPoint> positivePoints;

    private int numPoints;

    private String environment;

    private String userSessionUUID;

    public AiSegCalculate() {
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Integer getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(Integer frameNumber) {
        this.frameNumber = frameNumber;
    }

    public List<AiSegPoint> getNegativePoints() {
        return negativePoints;
    }

    public void setNegativePoints(List<AiSegPoint> negativePoints) {
        this.negativePoints = negativePoints;
    }

    public List<AiSegPoint> getPositivePoints() {
        return positivePoints;
    }

    public void setPositivePoints(List<AiSegPoint> positivePoints) {
        this.positivePoints = positivePoints;
    }

    public int getNumPoints() {
        return numPoints;
    }

    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getUserSessionUUID() {
        return userSessionUUID;
    }

    public void setUserSessionUUID(String userSessionUUID) {
        this.userSessionUUID = userSessionUUID;
    }

    @Override
    public String toString() {
        return "AiSegCalculate{" +
                "imageId='" + imageId + '\'' +
                ", frameNumber=" + frameNumber +
                ", negativePoints=" + negativePoints +
                ", positivePoints=" + positivePoints +
                ", numPoints=" + numPoints +
                ", environment='" + environment + '\'' +
                ", userSessionUUID='" + userSessionUUID + '\'' +
                '}';
    }
}
