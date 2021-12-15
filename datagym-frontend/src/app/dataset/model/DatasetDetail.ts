import {MediaSourceType} from '../../basic/media/model/MediaSourceType';
import {BasicDataset} from './BasicDataset';

export class DatasetDetail extends BasicDataset {
  public media: MediaSourceType[];
  public projectCount: number = 0;
  public allowPublicUrls: boolean;

}
