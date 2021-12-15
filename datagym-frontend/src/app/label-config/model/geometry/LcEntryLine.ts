import { LcEntryGeometry } from './LcEntryGeometry';
import { LcEntryType } from '../LcEntryType';

export class LcEntryLine extends LcEntryGeometry {
  static type: LcEntryType = LcEntryType.LINE;

  constructor(lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(LcEntryLine.type, lcEntryParentId, key, value, color, shortcut);
  }
}
