import { LcEntryClassification } from './LcEntryClassification';
import { LcEntryType } from '../LcEntryType';

export class LcEntryText extends LcEntryClassification {
  static type: LcEntryType = LcEntryType.FREE_TEXT;
  maxLength: number;

  constructor(lcEntryParentId: string | null, required: boolean, key?: string, value?: string, maxLength?: number) {
    super(LcEntryText.type, lcEntryParentId, required, key, value);
    this.maxLength = maxLength || null;
  }
}
