import {InterpolationType} from '../../../model/InterpolationType';
import {InterpolationHandler} from './InterpolationHandler';
import {LcEntryGeometryValue} from '../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../model/change/LcEntryChange';


export class NoInterpolation implements InterpolationHandler {

  public readonly kind = InterpolationType.NONE;

  public interpolate(value: LcEntryGeometryValue, index: number): LcEntryChange|undefined {

    const keyFrames = value.frameNumbers;
    const lowerIndex = Math.min(...keyFrames.filter(frameNumber => frameNumber <= index));

    return value.change.find(c => c.frameNumber === lowerIndex);
  }
}
