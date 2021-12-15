import {InterpolationType} from '../../../model/InterpolationType';
import {LcEntryGeometryValue} from '../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../model/change/LcEntryChange';


export interface InterpolationHandler {

  readonly kind: InterpolationType;

  interpolate(value: LcEntryGeometryValue, index: number): LcEntryChange|undefined;

}
