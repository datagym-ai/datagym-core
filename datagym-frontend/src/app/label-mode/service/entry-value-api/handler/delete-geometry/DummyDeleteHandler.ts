import {DeleteHandler} from './DeleteHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {DeleteGeometryConfig} from './DeleteGeometryConfig';


export class DummyDeleteHandler extends DeleteHandler {

  constructor() {
    super(null, null, null);
  }

  public deleteGeometry(value: LcEntryGeometryValue, config: DeleteGeometryConfig): void {
    // Just a dummy implementation. Do nothing.
  }
}
