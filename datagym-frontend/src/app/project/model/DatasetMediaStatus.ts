import {MediaSourceType} from '../../basic/media/model/MediaSourceType';

export class DatasetMediaStatus {

  public datasetName: string;
  public invalidMediaCount: number;
  public mediaStatus: Record<MediaSourceType, number>;

}
