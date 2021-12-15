import { LcEntryClassification } from './LcEntryClassification';
import { LcEntryType } from '../LcEntryType';
import { HasOptionsMap } from './HasOptionsMap';

export class LcEntrySelect extends LcEntryClassification implements HasOptionsMap {
  static type: LcEntryType = LcEntryType.SELECT;
  options: Map<string, string>;

  constructor(lcEntryParentId: string | null, required: boolean, key?: string, value?: string, options?: Map<string, string>) {
    super(LcEntrySelect.type, lcEntryParentId, required, key, value);
    this.options = options || new Map<string, string>();
  }
}
