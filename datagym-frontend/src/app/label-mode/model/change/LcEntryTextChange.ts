import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';

export class LcEntryTextChange extends LcEntryChange {
  public readonly kind = LcEntryType.FREE_TEXT;

  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    public text: string
  ) {
    super();
  }

  public equalValues(other: LcEntryTextChange): boolean {
    return !!other && this.text === other.text;
  }
}
