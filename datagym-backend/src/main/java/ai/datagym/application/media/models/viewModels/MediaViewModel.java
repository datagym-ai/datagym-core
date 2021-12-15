package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public abstract class MediaViewModel {
    private String id;
    private Long timestamp;
    private String mediaSourceType;
    private String mediaName;
    private boolean valid = true;
    private InvalidMediaReason reason;
    private String url;

    public MediaViewModel() {
    }

    public MediaViewModel(String id,
                          Long timestamp,
                          String mediaSourceType,
                          String mediaName,
                          boolean valid,
                          InvalidMediaReason reason) {
        this.id = id;
        this.timestamp = timestamp;
        this.mediaSourceType = mediaSourceType;
        this.mediaName = mediaName;
        this.valid = valid;
        this.reason = reason;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public InvalidMediaReason getReason() {
        return reason;
    }

    public void setReason(InvalidMediaReason reason) {
        this.reason = reason;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MediaViewModel{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", mediaSourceType='" + mediaSourceType + '\'' +
                ", mediaName='" + mediaName + '\'' +
                ", valid=" + valid +
                ", reason=" + reason +
                ", url='" + url + '\'' +
                '}';
    }
}
