import {Point} from './Point';
import {LcEntryLineChange} from '../change/LcEntryLineChange';
import {LcEntry, LcEntryType} from '../import';
import {LcEntryGeometryValue} from './LcEntryGeometryValue';
import {LabelIteration} from '../LabelIteration';
import {LcEntryValue} from '../LcEntryValue';

export class LcEntryLineValue extends LcEntryGeometryValue<LcEntryLineChange> {
  readonly kind = LcEntryType.LINE;

  public points: Point[] = [];

  public constructor(
    id: string | null,
    lcEntry: LcEntry | null,
    media: string | null,
    labelIteration: LabelIteration | string | null,
    timestamp: number | null,
    labeler: string | null,
    children: LcEntryValue[] | null,
    parent: string | null,
    valid: boolean,
    points: Point[] | null,
    comment?: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid, comment);
    this.points = points || [];
  }

  /**
   * Create a fully copy of the current LcEntryPolyValue.
   */
  public createClone(): LcEntryLineValue {
    return new LcEntryLineValue(
      this.id,
      this.lcEntry,
      this.mediaId,
      this.labelIteration,
      this.timestamp,
      this.labeler,
      this.children.map(c => c.clone()),
      this.lcEntryValueParentId,
      this.valid,
      this.points.map(p => p.clone()),
      this.comment
    );
  }

  public withChange(change: LcEntryLineChange): LcEntryLineValue {
    const clone = this.clone() as LcEntryLineValue;
    if (!!change) {
      clone.points = [...change.points];
    }
    return clone;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntryLineValue): void {
    this.points = source.points.map(p => p.clone());
    this.comment = source.comment;
  }

  public hasCoordinates(): boolean {
    const minRequiredPoints = 2;
    return this.points !== undefined && this.points.length >= minRequiredPoints;
  }

  protected isThisEntryValid(): boolean {
    const minRequiredPoints = 2;
    return this.points !== undefined && this.points.length >= minRequiredPoints;
  }

  public move(offset: Point): void {
    this.points = this.points.map(point => new Point(point.x + offset.x, point.y + offset.y));
  }

  public getDistance(position: Point): Point {
    const diffX = this.points[0].x - position.x;
    const diffY = this.points[0].y - position.y;

    return new Point(diffX, diffY);
  }

  public outOfBounds(size: Point): boolean {
    for (const point of this.points) {
      if (point.x > size.x || point.x < 0 ||
        point.y > size.y || point.y < 0) {
        return true;
      }
    }
    return false;
  }

  public getBoundingBox(): Point {

    const xes = this.points.map(point => point.x);
    const yes = this.points.map(point => point.y);

    return new Point(
      Math.max(...xes) - Math.min(...xes),
      Math.max(...yes) - Math.min(...yes)
    );
  }
}
