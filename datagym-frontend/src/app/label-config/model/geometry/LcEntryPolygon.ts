import { LcEntryGeometry } from './LcEntryGeometry';
import { LcEntryType } from '../LcEntryType';

export class LcEntryPolygon extends LcEntryGeometry {
  static type: LcEntryType = LcEntryType.POLYGON;

  constructor(lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(LcEntryPolygon.type, lcEntryParentId, key, value, color, shortcut);
  }
}
