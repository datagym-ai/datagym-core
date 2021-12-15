import {CreateHandler} from './CreateHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';

export class DummyCreateHandler extends CreateHandler {

  constructor() {
    super(null);
  }

  public createGeometry(value: LcEntryGeometryValue): void {
    // Nothing to do.
  }
}
