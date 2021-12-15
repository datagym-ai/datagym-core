import { LcEntryGeometry } from './LcEntryGeometry';
import { LcEntryType } from '../LcEntryType';

export class LcEntryRectangle extends LcEntryGeometry {
  static type: LcEntryType = LcEntryType.RECTANGLE;

  constructor(lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(LcEntryRectangle.type, lcEntryParentId, key, value, color, shortcut);
  }
}
