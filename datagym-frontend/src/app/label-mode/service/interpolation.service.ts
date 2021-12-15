import {EventEmitter, Injectable} from '@angular/core';
import {LcEntryChange} from '../model/change/LcEntryChange';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {InterpolationType} from '../model/InterpolationType';
import {InterpolationHandler} from './interpolate/handler/InterpolationHandler';
import {LinearInterpolation} from './interpolate/handler/LinearInterpolation';
import {NoInterpolation} from './interpolate/handler/NoInterpolation';
import {LcEntryChangeType} from '../model/change/LcEntryChangeType';


@Injectable({
  providedIn: 'root'
})
export class InterpolationService {

  /**
   * On interpolation type change redraw the geometries within the current frame.
   */
  public readonly onInterpolationTypeChanged: EventEmitter<void> = new EventEmitter<void>();

  public set interpolationType(type: InterpolationType) {

    if (this.handler.kind === type) {
      // nothing to do.
      return;
    }

    switch (type) {
      case InterpolationType.LINEAR:
        this.handler = new LinearInterpolation();
        break;
      case InterpolationType.NONE:
        // fall through:
      default:
        this.handler = new NoInterpolation();
    }

    this.onInterpolationTypeChanged.next();
  }

  private handler: InterpolationHandler = new LinearInterpolation();

  constructor() { }

  public getGeometryForIndex(value: LcEntryGeometryValue, index: number, interpolate: boolean = true): LcEntryGeometryValue | undefined {

    const keyFrames = value.change.map(c => c.frameNumber);
    const startFrame = Math.min(...keyFrames);
    const endFrame = Math.max(...keyFrames);

    if (index > endFrame || index < startFrame) {
      // Frame outside of this bar but the bar is within the view box.
      return undefined;
    }
    if (!/*not*/!!interpolate) {
      // If not interpolate: find the last keyframe before the requested index.
      index = Math.max(...keyFrames.filter(frameNumber => frameNumber <= index));
    }
    if (index === undefined) {
      // just a precaution, should not be possible.
      return undefined;
    }
    if (keyFrames.includes(index)) {
      // The index points to a keyframe. Return that geometry.
      const changeObject = value.change.find(c => c.frameNumber === index);
      return value.withChange(changeObject) as LcEntryGeometryValue;
    }

    const interpolatedChange = this.interpolate(value, index);
    if (interpolatedChange === undefined) {
      return undefined;
    }
    return value.withChange(interpolatedChange) as LcEntryGeometryValue;
  }

  /**
   * Calculate the interpolation.
   *
   * @param value
   * @param index
   */
  public interpolate(value: LcEntryGeometryValue, index: number): LcEntryChange;

  /**
   * Calculate the interpolation.
   *
   * @param value
   * @param index
   * @param type default: LcEntryChangeType.INTERPOLATED
   */
  public interpolate(value: LcEntryGeometryValue, index: number, type: LcEntryChangeType): LcEntryChange;

  /**
   * Implementation of the above definitions.
   *
   * @param value
   * @param index
   * @param type
   */
  public interpolate(value: LcEntryGeometryValue, index: number, type: LcEntryChangeType = LcEntryChangeType.INTERPOLATED): LcEntryChange|undefined {

    if (!InterpolationService.isIndexPossible(value, index)) {
      return undefined;
    }

    const response = this.handler.interpolate(value, index);
    if (!!response && !!type && LcEntryChangeType.inEnum(type)) {
      response.frameType = type;
    }
    return response;
  }

  /**
   * A interpolated value must be between the first and the last
   * keyframe (e.G START and END).
   *
   * Note: this method doesn't check inner START & END tags. It depends
   * only on the keyFrames numbers.
   *
   * @param value
   * @param index
   * @private
   */
  private static isIndexPossible(value: LcEntryGeometryValue, index: number): boolean {
    const keyFrames = value.frameNumbers;
    const startFrame = Math.min(...keyFrames);
    const endFrame = Math.max(...keyFrames);

    return !(index < startFrame || endFrame < index);
  }
}
