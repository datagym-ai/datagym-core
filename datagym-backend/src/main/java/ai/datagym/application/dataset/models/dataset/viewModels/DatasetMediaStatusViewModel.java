package ai.datagym.application.dataset.models.dataset.viewModels;

import ai.datagym.application.media.entity.MediaSourceType;

import java.util.Map;

public class DatasetMediaStatusViewModel {

    private String datasetName;
    private long invalidMediaCount;
    private Map<MediaSourceType, Long> mediaStatus;

    public DatasetMediaStatusViewModel(String datasetName, long invalidMediaCount, Map<MediaSourceType, Long> mediaStatus) {
        this.datasetName = datasetName;
        this.invalidMediaCount = invalidMediaCount;
        this.mediaStatus = mediaStatus;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public long getInvalidMediaCount() {
        return invalidMediaCount;
    }

    public void setInvalidMediaCount(long invalidMediaCount) {
        this.invalidMediaCount = invalidMediaCount;
    }

    public Map<MediaSourceType, Long> getMediaStatus() {
        return mediaStatus;
    }

    public void setMediaStatus(Map<MediaSourceType, Long> mediaStatus) {
        this.mediaStatus = mediaStatus;
    }
}

