package ai.datagym.application.dataset.models.video;

public class ExtractedVideoMetadataTO {

    private Integer height;
    private Integer width;
    private Long totalFrames;
    private Double duration;
    private String codecName;
    private String rFrameRate;
    private String formatName;
    private Long size;

    public ExtractedVideoMetadataTO() {
    }

    public ExtractedVideoMetadataTO(Integer height, Integer width, Long totalFrames, Double duration,
                                    String codecName, String rFrameRate, String formatName, Long size) {
        this.height = height;
        this.width = width;
        this.totalFrames = totalFrames;
        this.duration = duration;
        this.codecName = codecName;
        this.rFrameRate = rFrameRate;
        this.formatName = formatName;
        this.size = size;
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
        return "ExtractedVideoMetadataTO{" +
                "height=" + height +
                ", width=" + width +
                ", totalFrames=" + totalFrames +
                ", duration=" + duration +
                ", codecName='" + codecName + '\'' +
                ", rFrameRate='" + rFrameRate + '\'' +
                ", formatName='" + formatName + '\'' +
                ", size=" + size +
                '}';
    }
}
