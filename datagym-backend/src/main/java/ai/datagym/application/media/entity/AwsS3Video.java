package ai.datagym.application.media.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "aws_s3_video")
public class AwsS3Video extends AwsS3Media {

    @Column(name = "height")
    private Integer height;

    @Column(name = "width")
    private Integer width;

    @Column(name = "total_frames")
    private Long totalFrames;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "codec_name")
    private String codecName;

    @Column(name = "r_frame_rate")
    private String rFrameRate;

    @Column(name = "format_name")
    private String formatName;

    @Column(name = "size")
    private Long size;

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
        return "AwsS3Video{" +
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
