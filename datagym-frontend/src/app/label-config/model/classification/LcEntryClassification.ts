import { LcEntry } from '../LcEntry';
import { LcEntryType } from '../LcEntryType';

export abstract class LcEntryClassification extends LcEntry {
  public required: boolean;

  protected constructor(type: LcEntryType, lcEntryParentId: string | null, required: boolean, key?: string, value?: string) {
    super(type, lcEntryParentId, key, value);
    this.required = required;
  }
}
