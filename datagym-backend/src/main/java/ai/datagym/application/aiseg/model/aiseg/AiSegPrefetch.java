package ai.datagym.application.aiseg.model.aiseg;

public class AiSegPrefetch {

    private String imageId;

    private String b64image;

    public AiSegPrefetch(String imageId, String base64Image) {
        this.imageId = imageId;
        this.b64image = base64Image;
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

    @Override
    public String toString() {
        return "AiSegPrefetch{" +
                "imageId='" + imageId + '\'' +
                ", base64Image='" + b64image + '\'' +
                '}';
    }
}
