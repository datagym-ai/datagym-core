package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public class LocalImageViewModel extends MediaViewModel {
    private int width;
    private int height;

    public LocalImageViewModel() {
    }

    public LocalImageViewModel(String id, Long timestamp,
                               String imageType,
                               String imageName,
                               boolean valid,
                               InvalidMediaReason reason,
                               int width,
                               int height) {
        super(id, timestamp, imageType, imageName, valid, reason);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
