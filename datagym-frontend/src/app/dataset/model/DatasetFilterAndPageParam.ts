import {PageParam} from './PageParam';
import {MediaSourceType} from '../../basic/media/model/MediaSourceType';

export class DatasetFilterAndPageParam extends PageParam {
  owner: string;
  mediaName: string;
  mediaSourceType: MediaSourceType;
}
