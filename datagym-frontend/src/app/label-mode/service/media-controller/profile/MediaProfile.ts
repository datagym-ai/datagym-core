import {LabelModeType} from '../../../model/import';

/**
 * Control the template with some constant settings depending on the type.
 */
export interface MediaProfile {
  readonly videoPanelHeight: number;
  readonly showValueList: boolean;
  readonly showVideoPanel: boolean;
  readonly allowClassificationBar: boolean;
  readonly mediaType: LabelModeType;
}
