import {InterpolationHandler} from './InterpolationHandler';
import {InterpolationType} from '../../../model/InterpolationType';
import {LcEntryGeometryValue} from '../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../model/change/LcEntryChange';
import {LcEntryChangeType} from '../../../model/change/LcEntryChangeType';
import {LcEntryType} from '../../../../label-config/model/LcEntryType';
import {LcEntryRectangleChange} from '../../../model/change/LcEntryRectangleChange';
import {LcEntryPointChange} from '../../../model/change/LcEntryPointChange';
import {LcEntryLineChange} from '../../../model/change/LcEntryLineChange';
import {LcEntryPolyChange} from '../../../model/change/LcEntryPolyChange';


export class LinearInterpolation implements InterpolationHandler {
  public readonly kind = InterpolationType.LINEAR;

  public interpolate(value: LcEntryGeometryValue, index: number): LcEntryChange|undefined {
    const keyFrames = value.frameNumbers;
    const lowerIndex = Math.max(...keyFrames.filter(frameNumber => frameNumber < index));
    const upperIndex = Math.min(...keyFrames.filter(frameNumber => frameNumber > index));

    if (lowerIndex < index) {
      const lowerChange = value.change.find(c => c.frameNumber === lowerIndex);
      if (lowerChange.frameType === LcEntryChangeType.END) {
        return undefined;
      }
    }

    const lower = value.change.find(c => c.frameNumber === lowerIndex);
    const upper = value.change.find(c => c.frameNumber === upperIndex);

    if (!/*not*/!!lower || !/*not*/!!upper) {
      return undefined;
    }

    // Interpolate between the surrounding keyframes.
    const handler = new InterpolateHandler(index, upperIndex - lowerIndex, index - lowerIndex);

    switch (value.kind) {
      case LcEntryType.RECTANGLE:
        return LinearInterpolation.interpolateRectangle(handler, lower as LcEntryRectangleChange, upper as LcEntryRectangleChange);
      case LcEntryType.POINT:
        return LinearInterpolation.interpolatePoint(handler, lower as LcEntryPointChange, upper as LcEntryPointChange);
      case LcEntryType.LINE:
        return LinearInterpolation.interpolateLine(handler, lower as LcEntryLineChange);
      case LcEntryType.POLYGON:
        return LinearInterpolation.interpolatePolygon(handler, lower as LcEntryPolyChange);
      default:
      /*
       * Classifications cannot be interpolated.
       * LcEntryType.IMAGE_SEGMENTATION is not supported.
       * LcEntryType.IMAGE_SEGMENTATION_ERASER is also not supported.
       *
       * So there is nothing to do.
       */
    }
    return undefined;
  }

  /**
   * 'Interpolation' for polygons mean 'use the last known geometry properties'.
   *
   * @param handler
   * @param lower
   * @private
   */
  private static interpolatePolygon(handler: InterpolateHandler, lower: LcEntryPolyChange): LcEntryPolyChange {
    return new LcEntryPolyChange(
      handler.id,
      handler.index,
      LcEntryChangeType.INTERPOLATED,
      [...lower.points]
    );
  }

  /**
   * 'Interpolation' for lines mean 'use the last known geometry properties'.
   *
   * @param handler
   * @param lower
   * @private
   */
  private static interpolateLine(handler: InterpolateHandler, lower: LcEntryLineChange): LcEntryLineChange {
    return new LcEntryLineChange(
      handler.id,
      handler.index,
      LcEntryChangeType.INTERPOLATED,
      [...lower.points]
    );
  }

  /**
   * Rectangles can be interpolated. Let's do it ;)
   *
   * @param handler
   * @param lower
   * @param upper
   * @private
   */
  private static interpolateRectangle(handler: InterpolateHandler, lower: LcEntryRectangleChange, upper: LcEntryRectangleChange): LcEntryRectangleChange {
    return new LcEntryRectangleChange(
      handler.id,
      handler.index,
      LcEntryChangeType.INTERPOLATED,
      handler.interpolate(lower.x, upper.x),
      handler.interpolate(lower.y, upper.y),
      handler.interpolate(lower.width, upper.width),
      handler.interpolate(lower.height, upper.height)
    );
  }

  /**
   * Points can be interpolated. Let's do it ;)
   *
   * @param handler
   * @param lower
   * @param upper
   * @private
   */
  private static interpolatePoint(handler: InterpolateHandler, lower: LcEntryPointChange, upper: LcEntryPointChange): LcEntryPointChange {
    return new LcEntryPointChange(
      handler.id,
      handler.index,
      LcEntryChangeType.INTERPOLATED,
      handler.interpolate(lower.x, upper.x),
      handler.interpolate(lower.y, upper.y)
    );
  }
}

class InterpolateHandler {
  /**
   * The interpolated change objects require an id.
   * This string is not known within the BE and would raise an error
   * if the interpolated change object would be send via POST or UPDATE request.
   */
  public readonly id: 'interpolated';

  /**
   * Interpolate for this setting:
   * @param index
   * @param stepsBetween
   * @param searchIndex
   */
  constructor(public readonly index: number, private readonly stepsBetween: number, private readonly searchIndex: number) {}

  public interpolate(lower: number, upper: number): number {

    if (lower === upper) {
      return lower;
    }

    return lower + (upper - lower) / this.stepsBetween * this.searchIndex;
  }
}
