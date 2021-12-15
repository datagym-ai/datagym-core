import {Point} from '../geometry/Point';
import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';


export class LcEntryPolyChange extends LcEntryChange {
  public readonly kind = LcEntryType.POLYGON;

  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    public points: Point[]
  ) {
    super();
  }

  public equalValues(other: LcEntryPolyChange): boolean {
    if (!/*not*/!!other) {
      return false;
    }

    if (this.points.length !== other.points.length) {
      return false;
    }

    for (let i = 0; i < this.points.length; i++) {
      if (!this.points[i].equals(other.points[i])) {
        return false;
      }
    }

    return true;
  }
}
