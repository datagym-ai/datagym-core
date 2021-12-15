import {LcEntryValue} from '../LcEntryValue';
import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {Point} from './Point';
import {LcEntryChange} from '../change/LcEntryChange';


export abstract class LcEntryGeometryValue<C extends LcEntryChange = LcEntryChange> extends LcEntryValue<C> {

  public comment ?: string;

  /**
   * Get the bounding boxes from the current value.
   */
  public abstract getBoundingBox(): Point;

  /**
   * Check if the geometry is outside the Image Area
   */
  public abstract outOfBounds(size: Point): boolean;

  /**
   * Get the distance to the given point.
   * @param point
   */
  public abstract getDistance(point: Point): Point;

  /**
   * Move the geometry with the offset
   * @param offset
   */
  public abstract move(offset: Point): void;

  /**
   * Move the geometry to the given position.
   * @param position
   */
  public moveTo(position: Point): void {
    const offset = this.getDistance(position);
    this.move(offset);
  }

  /**
   * If the user created a new geometry but never set it's points,
   * the label mode would receive 'empty' geometry values without coordinates.
   * They cannot be drawn or handled otherwise. Filter them out.
   */
  public abstract hasCoordinates(): boolean;

  protected constructor(
    id: string | null,
    lcEntry: LcEntry | null,
    media: string | null,
    labelIteration: LabelIteration | string | null,
    timestamp: number | null,
    labeler: string | null,
    children: LcEntryValue[] | null,
    parent: string | null,
    valid: boolean,
    comment: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid);
    this.comment = comment;
  }
}
