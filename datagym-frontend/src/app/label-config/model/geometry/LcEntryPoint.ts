import { LcEntryGeometry } from './LcEntryGeometry';
import { LcEntryType } from '../LcEntryType';

export class LcEntryPoint extends LcEntryGeometry {
  static type: LcEntryType = LcEntryType.POINT;

  constructor(lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(LcEntryPoint.type, lcEntryParentId, key, value, color, shortcut);
  }
}
