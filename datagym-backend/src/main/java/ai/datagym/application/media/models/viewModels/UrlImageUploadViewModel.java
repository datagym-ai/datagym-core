package ai.datagym.application.media.models.viewModels;

public class UrlImageUploadViewModel {
    private String internal_media_ID;

    private String external_media_ID;

    private String imageUrl;

    private String mediaUploadStatus;

    public UrlImageUploadViewModel() {
    }

    public String getInternal_media_ID() {
        return internal_media_ID;
    }

    public void setInternal_media_ID(String internal_media_ID) {
        this.internal_media_ID = internal_media_ID;
    }

    public String getExternal_media_ID() {
        return external_media_ID;
    }

    public void setExternal_media_ID(String external_media_ID) {
        this.external_media_ID = external_media_ID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMediaUploadStatus() {
        return mediaUploadStatus;
    }

    public void setMediaUploadStatus(String mediaUploadStatus) {
        this.mediaUploadStatus = mediaUploadStatus;
    }
}
