package ai.datagym.application.dummy.models.bindingModels.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyMediaViewModel {
    @JsonIgnore
    private String id;

    private String url;

    @JsonIgnore
    private Long timestamp;


    private String mediaSourceType;
    private String mediaName;

    @JsonIgnore
    private String width;

    @JsonIgnore
    private String height;

    public DummyMediaViewModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }
}
