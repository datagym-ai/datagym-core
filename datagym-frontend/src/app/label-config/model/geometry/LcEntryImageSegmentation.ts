import {LcEntryGeometry} from './LcEntryGeometry';
import {LcEntryType} from '../LcEntryType';


export class LcEntryImageSegmentation extends LcEntryGeometry {

  static type: LcEntryType = LcEntryType.IMAGE_SEGMENTATION;

  constructor(lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(LcEntryImageSegmentation.type, lcEntryParentId, key, value, color, shortcut);
  }
}
