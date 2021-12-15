import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryGeometryValue} from './LcEntryGeometryValue';
import {LcEntryValue} from '../LcEntryValue';
import {Point} from './Point';
import {LcEntryRectangleChange} from '../change/LcEntryRectangleChange';
import {LcEntryType} from '../import';

export class LcEntryRectangleValue extends LcEntryGeometryValue<LcEntryRectangleChange> {
  readonly kind = LcEntryType.RECTANGLE;

  public x: number;
  public y: number;
  public width: number;
  public height: number;

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
    width: number,
    height: number,
    comment?: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid, comment);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Create a fully copy of the current LcEntryRectangleValue.
   */
  public createClone(): LcEntryRectangleValue {
    return new LcEntryRectangleValue(
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
      this.width,
      this.height,
      this.comment
    );
  }

  public withChange(change: LcEntryRectangleChange): LcEntryRectangleValue {
    const clone = this.clone() as LcEntryRectangleValue;
    if (!!change) {
      clone.x = change.x;
      clone.y = change.y;
      clone.width = change.width;
      clone.height = change.height;
    }
    return clone;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntryRectangleValue): void {
    this.x = source.x;
    this.y = source.y;
    this.width = source.width;
    this.height = source.height;
    this.comment = source.comment;
  }

  public hasCoordinates(): boolean {
    // check if the points x & y and properties width & height are set.
    return !(
      (!this.x === null) ||
      (!this.y === null) ||
      (!this.width && this.width !== 0) ||
      (!this.height && this.height !== 0)
    );
  }

  protected isThisEntryValid(): boolean {
    return this.height !== undefined
      && this.width !== undefined
      && this.x !== undefined
      && this.y !== undefined;
  }

  public move(offset: Point): void {
    this.x += offset.x;
    this.y += offset.y;
  }

  public moveTo(position: Point): void {
    this.x = position.x;
    this.y = position.y;
  }

  public getDistance(point: Point): Point {
    return new Point(this.x - point.x, this.y - point.y);
  }

  public outOfBounds(size: Point): boolean {
    return this.x < 0 || this.x + this.width > size.x ||
      this.y < 0 || this.y + this.height > size.y;
  }

  public getBoundingBox(): Point {
    return new Point(this.width, this.height);
  }
}
