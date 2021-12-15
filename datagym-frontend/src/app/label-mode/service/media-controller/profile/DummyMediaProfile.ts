import {MediaProfile} from './MediaProfile';
import {LabelModeType} from '../../../model/import';

const zero = 0;

export class DummyMediaProfile implements MediaProfile{

  public readonly videoPanelHeight = zero;
  public readonly showValueList = true;
  public readonly showVideoPanel = false;
  public readonly allowClassificationBar = true;
  public readonly mediaType: LabelModeType = LabelModeType.UNKNOWN;

}
