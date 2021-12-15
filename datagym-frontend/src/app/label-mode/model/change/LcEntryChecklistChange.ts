import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';

export class LcEntryChecklistChange extends LcEntryChange {
  public readonly kind = LcEntryType.CHECKLIST;

  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    public checkedValues: string[]
  ) {
    super();
  }

  public equalValues(other: LcEntryChecklistChange): boolean {
    // Note: the order of the checkedValues doesn't matter here.
    return !!other &&
      this.checkedValues.length === other.checkedValues.length &&
      other.checkedValues.find(c => !this.checkedValues.includes(c)) === undefined;
  }

}
