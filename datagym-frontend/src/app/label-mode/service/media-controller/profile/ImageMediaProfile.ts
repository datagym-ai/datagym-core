import {MediaProfile} from './MediaProfile';
import {LabelModeType} from '../../../model/import';

const zero = 0;

export class ImageMediaProfile implements MediaProfile{

  /**
   * The media media type has no video panel.
   */
  public readonly videoPanelHeight = zero;
  public readonly showValueList = true;
  public readonly showVideoPanel = false;
  public readonly allowClassificationBar = true;
  public readonly mediaType: LabelModeType = LabelModeType.IMAGE;

}
