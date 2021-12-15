import {Media} from './Media';
import {MediaUploadStatus} from './MediaUploadStatus';

export class AwsS3Image extends Media {

  awsKey: string;
  mediaUploadStatus: MediaUploadStatus;
  lastError: string | null;
  lastErrorTimeStamp: number | null;

}
