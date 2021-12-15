package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public class AwsS3VideoViewModel extends AwsS3MediaViewModel {
    private Integer height;
    private Integer width;
    private Long totalFrames;
    private Double duration;
    private String codecName;
    private String rFrameRate;
    private String formatName;
    private Long size;

    public AwsS3VideoViewModel(String id, Long timestamp, String imageType, String imageName, boolean valid,
                               InvalidMediaReason reason, String awsKey,
                               String lastError, Long lastErrorTimeStamp) {
        super(id, timestamp, imageType, imageName, valid, reason, awsKey, lastError, lastErrorTimeStamp);
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Long getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(Long totalFrames) {
        this.totalFrames = totalFrames;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getrFrameRate() {
        return rFrameRate;
    }

    public void setrFrameRate(String rFrameRate) {
        this.rFrameRate = rFrameRate;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "AwsS3VideoViewModel{" +
                "height=" + height +
                ", width=" + width +
                ", totalFrames=" + totalFrames +
                ", duration=" + duration +
                ", codecName='" + codecName + '\'' +
                ", formatName='" + formatName + '\'' +
                ", size=" + size +
                '}';
    }
}
