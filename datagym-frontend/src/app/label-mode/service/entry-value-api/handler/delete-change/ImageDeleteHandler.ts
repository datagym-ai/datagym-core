import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {DeleteHandler} from './DeleteHandler';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';


export class ImageDeleteHandler extends DeleteHandler {

  public constructor() {
    super();
  }

  public deleteChange(value: LcEntryGeometryValue, frameNumber: number): void;
  public deleteChange(value: LcEntryGeometryValue, change: LcEntryChange): void;
  public deleteChange(value: LcEntryGeometryValue, arg: number|LcEntryChange = undefined): void {
    // Not implemented for media labeling.
  }
}
