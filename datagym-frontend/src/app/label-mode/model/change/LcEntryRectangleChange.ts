import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';


export class LcEntryRectangleChange extends LcEntryChange {
  public readonly kind = LcEntryType.RECTANGLE;

  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    public x: number,
    public y: number,
    public width: number,
    public height: number,
  ) {
    super();
  }

  public equalValues(other: LcEntryRectangleChange): boolean {
    return !!other &&
      this.x === other.x &&
      this.y === other.y &&
      this.width === other.width &&
      this.height === other.width;
  }
}
