package ai.datagym.application.dataset.models.dataset.bindingModels;

import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.utils.PageParam;

public class DatasetFilterAndPageParam extends PageParam {
    private String mediaName;
    private MediaSourceType mediaSourceType;

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public MediaSourceType getMediaSourceType() {
        return mediaSourceType;
    }

    public void setMediaSourceType(MediaSourceType mediaSourceType) {
        this.mediaSourceType = mediaSourceType;
    }

    @Override
    public String toString() {
        return "DatasetFilterAndPageParam{" +
                "mediaName='" + mediaName + '\'' +
                ", mediaSourceType=" + mediaSourceType +
                '}';
    }
}
