import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryGeometryValue} from './LcEntryGeometryValue';
import {LcEntryValue} from '../LcEntryValue';
import {Point} from './Point';
import {LcEntryPointChange} from '../change/LcEntryPointChange';
import {LcEntryType} from '../import';

export class LcEntryPointValue extends LcEntryGeometryValue<LcEntryPointChange> {
  readonly kind = LcEntryType.POINT;

  public x: number;
  public y: number;

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
    x: number,
    y: number,
    comment?: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid, comment);
    this.x = x;
    this.y = y;
  }

  /**
   * Create a fully copy of the current LcEntryPolyValue.
   */
  public createClone(): LcEntryPointValue {
    return new LcEntryPointValue(
      this.id,
      this.lcEntry,
      this.mediaId,
      this.labelIteration,
      this.timestamp,
      this.labeler,
      this.children.map(c => c.clone()),
      this.lcEntryValueParentId,
      this.valid,
      this.x,
      this.y,
      this.comment
    );
  }

  public withChange(change: LcEntryPointChange): LcEntryPointValue {
    const clone = this.clone() as LcEntryPointValue;
    if (!!change) {
      clone.x = change.x;
      clone.y = change.y;
    }
    return clone;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntryPointValue): void {
    this.x = source.x;
    this.y = source.y;
    this.comment = source.comment;
  }

  public hasCoordinates(): boolean {
    // check if the points x & y are set.
    return !(
      (!this.x === null) ||
      (!this.y === null)
    );
  }

  protected isThisEntryValid(): boolean {
    return this.x !== undefined && this.y !== undefined;
  }

  public move(offset: Point): void {
    this.x += offset.x;
    this.y += offset.y;
  }

  public getDistance(point: Point): Point {
    return new Point(this.x - point.x, this.y - point.y);
  }

  public outOfBounds(size: Point): boolean {
    return this.x > size.x || this.x < 0 ||
      this.y > size.y || this.y < 0;
  }

  public getBoundingBox(): Point {
    return new Point(1, 1);
  }
}
