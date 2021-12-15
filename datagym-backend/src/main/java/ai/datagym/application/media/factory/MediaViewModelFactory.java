package ai.datagym.application.media.factory;

import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.models.viewModels.*;
import com.eforce21.lib.exception.GenericException;
import org.hibernate.Hibernate;

public class MediaViewModelFactory {
    private MediaViewModelFactory() {
    }

    public static MediaViewModel createImageViewModel(Media media) {
        String mediaId = media.getId();
        Long timestamp = media.getTimestamp();
        String mediaName = media.getMediaName();
        boolean valid = media.isValid();
        InvalidMediaReason invalidMediaReason = media.getInvalidMediaReason();

        MediaSourceType mediaSourceType = media.getMediaSourceType();

        switch (mediaSourceType) {
            case LOCAL:
                LocalImage localImage = (LocalImage) Hibernate.unproxy(media);
                int width = localImage.getWidth();
                int height = localImage.getHeight();
                return new LocalImageViewModel(mediaId,
                        timestamp,
                        mediaSourceType.name(),
                        mediaName,
                        valid,
                        invalidMediaReason,
                        width,
                        height);
            case SHAREABLE_LINK:
                UrlImage urlImage = (UrlImage) Hibernate.unproxy(media);
                String url = urlImage.getUrl();
                return new UrlImageViewModel(mediaId,
                        timestamp,
                        mediaSourceType.name(),
                        mediaName,
                        valid,
                        invalidMediaReason,
                        url);
            case AWS_S3:
                AwsS3Media awsS3Media = (AwsS3Media) Hibernate.unproxy(media);
                String awsKey = awsS3Media.getAwsKey();
                String lastError = awsS3Media.getLastError();
                Long lastErrorTimeStamp = awsS3Media.getLastErrorTimeStamp();
                if (awsS3Media instanceof AwsS3Image) {
                    return new AwsS3ImageViewModel(mediaId,
                            timestamp,
                            mediaSourceType.name(),
                            mediaName,
                            valid,
                            invalidMediaReason,
                            awsKey,
                            lastError,
                            lastErrorTimeStamp);
                } else if (awsS3Media instanceof AwsS3Video) {
                    AwsS3Video s3Video = (AwsS3Video) awsS3Media;
                    AwsS3VideoViewModel videoViewModel = new AwsS3VideoViewModel(mediaId,
                            timestamp,
                            mediaSourceType.name(),
                            mediaName,
                            valid,
                            invalidMediaReason,
                            awsKey,
                            lastError,
                            lastErrorTimeStamp);
                    videoViewModel.setHeight(s3Video.getHeight());
                    videoViewModel.setWidth(s3Video.getWidth());
                    videoViewModel.setTotalFrames(s3Video.getTotalFrames());
                    videoViewModel.setDuration(s3Video.getDuration());
                    videoViewModel.setCodecName(s3Video.getCodecName());
                    videoViewModel.setrFrameRate(s3Video.getrFrameRate());
                    videoViewModel.setFormatName(s3Video.getFormatName());
                    videoViewModel.setSize(s3Video.getSize());
                    return videoViewModel;
                }

            default:
                throw new GenericException("image_type_not_found", null, null, "image type");
        }
    }
}
