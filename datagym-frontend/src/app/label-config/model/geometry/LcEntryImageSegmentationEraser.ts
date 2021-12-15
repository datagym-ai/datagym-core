import {LcEntryGeometry} from './LcEntryGeometry';
import {LcEntryType} from '../LcEntryType';


export class LcEntryImageSegmentationEraser extends LcEntryGeometry {

  static type: LcEntryType = LcEntryType.IMAGE_SEGMENTATION_ERASER;

  constructor() {
    super(
      LcEntryImageSegmentationEraser.type,
      null,
      'IMAGE_SEGMENTATION_ERASER',
      'GLOBAL.BACKGROUND',
      'black',
      null
    );
  }

}
