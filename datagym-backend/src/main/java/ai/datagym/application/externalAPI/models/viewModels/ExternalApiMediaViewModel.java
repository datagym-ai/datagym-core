package ai.datagym.application.externalAPI.models.viewModels;

public class ExternalApiMediaViewModel {
    private String id;
    private Long timestamp;
    private String mediaSourceType;
    private String mediaName;

    public ExternalApiMediaViewModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMediaSourceType() {
        return mediaSourceType;
    }

    public void setMediaSourceType(String mediaSourceType) {
        this.mediaSourceType = mediaSourceType;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }
}
