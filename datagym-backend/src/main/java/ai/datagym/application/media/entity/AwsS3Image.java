package ai.datagym.application.media.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "aws_s3_image")
public class AwsS3Image extends AwsS3Media {


}
