package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public class UrlImageViewModel extends MediaViewModel {
    private String url;

    public UrlImageViewModel() {
    }

    public UrlImageViewModel(String id,
                             Long timestamp,
                             String imageType,
                             String imageName,
                             boolean valid,
                             InvalidMediaReason reason,
                             String url) {
        super(id, timestamp, imageType, imageName, valid, reason);
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
