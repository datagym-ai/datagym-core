import {UpdateHandler} from './UpdateHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';

/**
 * A dummy implementation without any functionality.
 * Just as a precaution to prevent type error 'handler is undefined'.
 */
export class DummyUpdateHandler extends UpdateHandler {

  constructor() {
    super(null, null);
  }

  public updateGeometry(value: LcEntryGeometryValue): void {
    // Have some break. Here is nothing to do.
  }

}
