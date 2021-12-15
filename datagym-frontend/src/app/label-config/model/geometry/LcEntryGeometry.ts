import { LcEntry } from '../LcEntry';
import { LcEntryType } from '../LcEntryType';

export abstract class LcEntryGeometry extends LcEntry {
  color: string;
  shortcut?: string;

  protected constructor(type: LcEntryType, lcEntryParentId: string | null, key?: string, value?: string, color?: string, shortcut?: string) {
    super(type, lcEntryParentId, key, value);
    this.color = color;
    this.shortcut = shortcut;
  }
}
