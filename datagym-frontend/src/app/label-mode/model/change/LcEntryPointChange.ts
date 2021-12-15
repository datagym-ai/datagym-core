import {LcEntryChange} from './LcEntryChange';
import {LcEntryType} from '../import';
import {LcEntryChangeType} from './LcEntryChangeType';
import {Point} from '../geometry/Point';


export class LcEntryPointChange extends LcEntryChange {
  public readonly kind = LcEntryType.POINT;

  /**
   * @deprecated Use `point` property instead.
   */
  public get x(): number {
    return this.point.x;
  }

  /**
   * @deprecated Use `point` property instead.
   */
  public get y(): number {
    return this.point.y;
  }

  public point: Point = undefined;

  /**
   * @param id
   * @param frameNumber
   * @param frameType
   * @param x
   * @param y
   * @deprecated Use the ctr with point as argument.
   */
  constructor(id: string, frameNumber: number, frameType: LcEntryChangeType, x: number, y :number);

  /**
   * @param id
   * @param frameNumber
   * @param frameType
   * @param point
   */
  public constructor(id: string, frameNumber: number, frameType: LcEntryChangeType, point: Point);

  /**
   * @param id
   * @param frameNumber
   * @param frameType
   * @param point
   */
  public constructor(id: string, frameNumber: number, frameType: LcEntryChangeType, point: {x:number, y: number});

  /**
   * Implementation of the above definitions.
   */
  public constructor(
    public id: string,
    public frameNumber: number,
    public frameType: LcEntryChangeType,
    a: Point|{x:number, y: number}|number,
    b ?:number
  ) {
    super();
    if (typeof a === 'object') {
      this.point = new Point(a.x, a.y);
    }
    if (typeof a === 'number' && typeof b === 'number') {
      this.point = new Point(a, b);
    }
  }

  public equalValues(other: LcEntryPointChange): boolean {
    return !!other && this.point.equals(other.point);
  }
}
