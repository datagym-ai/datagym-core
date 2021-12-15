import {AwsS3Image} from '../../basic/media/model/AwsS3Image';


export class AwsS3SyncStatusView {

  public addedS3Images: AwsS3Image[];
  public deletedS3Images: AwsS3Image[];
  public uploadFailedS3Images: AwsS3Image[];
  public syncError: string;
  public lastError: string;
  public lastErrorTimeStamp: number;
  public lastSynchronized: number;

}
