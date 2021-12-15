package ai.datagym.application.aiseg.model.preLabel;

import java.util.HashMap;
import java.util.Map;

public class PreLabelRequest {
    private String imageId;
    private String b64image;
    private String modelName;
    private Map<String,Map<String, String>> requestedClasses = new HashMap<>();
    private String environment;

    public PreLabelRequest(String imageId,
                           String b64image,
                           String modelName,
                           Map<String, Map<String, String>> requestedClasses,
                           String environment) {
        this.imageId = imageId;
        this.b64image = b64image;
        this.modelName = modelName;
        this.requestedClasses = requestedClasses;
        this.environment = environment;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getB64image() {
        return b64image;
    }

    public void setB64image(String b64image) {
        this.b64image = b64image;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Map<String, Map<String, String>> getRequestedClasses() {
        return requestedClasses;
    }

    public void setRequestedClasses(Map<String, Map<String, String>> requestedClasses) {
        this.requestedClasses = requestedClasses;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
