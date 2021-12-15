import {LcEntryType} from '../import';
import {LcEntryValue} from '../LcEntryValue';
import {LcEntryChange} from './LcEntryChange';
import {LcEntryChangeType} from './LcEntryChangeType';
import {LcEntrySelectValue} from '../classification/LcEntrySelectValue';
import {LcEntrySelectChange} from './LcEntrySelectChange';
import {LcEntryPointValue} from '../geometry/LcEntryPointValue';
import {LcEntryPointChange} from './LcEntryPointChange';
import {LcEntryRectangleValue} from '../geometry/LcEntryRectangleValue';
import {LcEntryRectangleChange} from './LcEntryRectangleChange';
import {LcEntryLineValue} from '../geometry/LcEntryLineValue';
import {LcEntryLineChange} from './LcEntryLineChange';
import {LcEntryPolyValue} from '../geometry/LcEntryPolyValue';
import {LcEntryPolyChange} from './LcEntryPolyChange';
import {LcEntryTextValue} from '../classification/LcEntryTextValue';
import {LcEntryTextChange} from './LcEntryTextChange';
import {LcEntryChecklistValue} from '../classification/LcEntryChecklistValue';
import {LcEntryChecklistChange} from './LcEntryChecklistChange';
import {Point} from '../geometry/Point';


const DEFAULT_FRAME_NUMBER = 0;

/**
 * Immutable factory object.
 */
export class LcEntryChangeFactory {

  private readonly id: string = null;
  // handCrafted is not known within the BE and therefore not set via ctr.
  private readonly handCrafted: boolean = true;
  private readonly frameNumber: number = DEFAULT_FRAME_NUMBER;
  private readonly frameType: LcEntryChangeType = LcEntryChangeType.CHANGE;

  constructor({
                id = null,
                frameNumber = 0,
                // @deprecated: the argument type should be removed.
                type = LcEntryChangeType.CHANGE,
                frameType = undefined,
                handCrafted = true} = {}) {
    this.id = id;
    this.frameNumber = frameNumber;
    this.frameType = type === LcEntryChangeType.CHANGE && frameType !== undefined ? frameType : type;
    this.handCrafted = handCrafted;
  }

  public select(selectKey: string): LcEntrySelectChange{
    const select = new LcEntrySelectChange(
      this.id,
      this.frameNumber,
      this.frameType,
      selectKey
    );
    select.handCrafted = this.handCrafted;
    return select;
  }

  public polygon(points: Point[]) {
    const polygon = new LcEntryPolyChange(
      this.id,
      this.frameNumber,
      this.frameType,
      points
    );
    polygon.handCrafted = this.handCrafted;
    return polygon;
  }
  public point(x: Point): LcEntryPointChange;
  public point(x: number, y: number): LcEntryPointChange;
  public point(x: Point|number, y?: number): LcEntryPointChange {
    y = typeof x == 'object' ? x.y : y;
    x = typeof x == 'object' ? x.x : x;

    const point = new LcEntryPointChange(
      this.id,
      this.frameNumber,
      this.frameType,
      {x, y}
    );
    point.handCrafted = this.handCrafted;
    return point;
  }

  public line(points: Point[]): LcEntryLineChange {
    const line = new LcEntryLineChange(
      this.id,
      this.frameNumber,
      this.frameType,
      points,
    );
    line.handCrafted = this.handCrafted;
    return line;
  }

  public text(text: string): LcEntryTextChange {
    const textChange = new LcEntryTextChange(
      this.id,
      this.frameNumber,
      this.frameType,
      text,
    );
    textChange.handCrafted = this.handCrafted;
    return textChange;
  }

  public checklist(checkedValues: string[]): LcEntryChecklistChange {
    const check = new LcEntryChecklistChange(
      this.id,
      this.frameNumber,
      this.frameType,
      checkedValues,
    );
    check.handCrafted = this.handCrafted;
    return check;
  }

  public rect(x: number, y: number, width: number, height: number,): LcEntryRectangleChange {
    const rect = new LcEntryRectangleChange(
      this.id,
      this.frameNumber,
      this.frameType,
      x,
      y,
      width,
      height
    );
    rect.handCrafted = this.handCrafted;
    return rect;
  }

  /**
   * Create the change object just by passing the value.
   *
   * The kind of value is used to select the right change type.
   *
   * @param value
   */
  public fromValue(value: LcEntryValue): LcEntryChange {
    /**
     * Not supported:
     * - LcEntryType.IMAGE_SEGMENTATION
     * - LcEntryType.IMAGE_SEGMENTATION_ERASER
     */
    switch (value.kind) {
      case LcEntryType.SELECT:
        return this.select((value as LcEntrySelectValue).selectKey);
      case LcEntryType.POLYGON:
        return this.polygon((value as LcEntryPolyValue).points);
      case LcEntryType.POINT:
        return this.point(
          (value as LcEntryPointValue).x,
          (value as LcEntryPointValue).y
        );
      case LcEntryType.LINE:
        return this.line((value as LcEntryLineValue).points);
      case LcEntryType.FREE_TEXT:
        return this.text((value as LcEntryTextValue).text);
      case LcEntryType.CHECKLIST:
        return this.checklist((value as LcEntryChecklistValue).checkedValues);
      case LcEntryType.RECTANGLE:
        return this.rect(
          (value as LcEntryRectangleValue).x,
          (value as LcEntryRectangleValue).y,
          (value as LcEntryRectangleValue).width,
          (value as LcEntryRectangleValue).height
        );
      default:
        return undefined;
    }
  }

  /**
   * Create the change object from JSON.
   * This is required for values returned from the BE that
   * never had passed an ctr.
   *
   * @param value
   */
  public fromChangeObject(value: LcEntryChange): LcEntryChange {
    /**
     * Not supported:
     * - LcEntryType.IMAGE_SEGMENTATION
     * - LcEntryType.IMAGE_SEGMENTATION_ERASER
     */
    switch (value.kind) {
      case LcEntryType.SELECT:
        return this.select((value as LcEntrySelectChange).selectKey);
      case LcEntryType.POLYGON:
        return this.polygon((value as LcEntryPolyChange).points);
      case LcEntryType.POINT:
        const asPoint = value as LcEntryPointChange;
        const point = !/*not*/!!asPoint.point
          ? new Point(asPoint.x, asPoint.y)
          : asPoint.point;
        return this.point(point);
      case LcEntryType.LINE:
        return this.line((value as LcEntryLineChange).points);
      case LcEntryType.FREE_TEXT:
        return this.text((value as LcEntryTextChange).text);
      case LcEntryType.CHECKLIST:
        return this.checklist((value as LcEntryChecklistChange).checkedValues);
      case LcEntryType.RECTANGLE:
        return this.rect(
          (value as LcEntryRectangleChange).x,
          (value as LcEntryRectangleChange).y,
          (value as LcEntryRectangleChange).width,
          (value as LcEntryRectangleChange).height
        );
      default:
        return undefined;
    }
  }
}
