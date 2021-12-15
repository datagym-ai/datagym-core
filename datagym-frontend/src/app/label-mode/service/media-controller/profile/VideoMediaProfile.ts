import {MediaProfile} from './MediaProfile';
import {LabelModeType} from '../../../model/import';

const DEFAULT_VIDEO_PANEL_HEIGHT = 30;

export class VideoMediaProfile implements MediaProfile{

  /**
   * The media media type has a default height of 30%.
   */
  public readonly videoPanelHeight = DEFAULT_VIDEO_PANEL_HEIGHT;
  public readonly showValueList = false;
  public readonly showVideoPanel = true;
  public readonly allowClassificationBar = true;
  public readonly mediaType: LabelModeType = LabelModeType.VIDEO;

}
