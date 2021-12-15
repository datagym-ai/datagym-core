import {DeleteHandler} from './DeleteHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';


export class DummyDeleteHandler extends DeleteHandler {

  constructor() {
    super();
  }

  public deleteChange(value: LcEntryGeometryValue, frameNumber: number): void;
  public deleteChange(value: LcEntryGeometryValue, change: LcEntryChange): void;
  public deleteChange(value: LcEntryGeometryValue, arg: number|LcEntryChange = undefined): void {
    // Just a dummy implementation. Do nothing.
  }
}
