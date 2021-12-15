import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';

export class LcEntrySelectChange extends LcEntryChange {
  public readonly kind = LcEntryType.SELECT;

  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    public selectKey: string
  ) {
    super();
  }

  public equalValues(other: LcEntrySelectChange): boolean {
    return !!other && this.selectKey === other.selectKey;
  }
}
