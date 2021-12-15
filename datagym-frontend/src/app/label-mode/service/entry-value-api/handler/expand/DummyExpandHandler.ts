import {ExpandHandler} from './ExpandHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';


/**
 * No media handler available.
 */
export class DummyExpandHandler extends ExpandHandler {

  public expandVideoValueLine(value: LcEntryGeometryValue, change: LcEntryChange, left: boolean): void {
    /* nothing to do here */
  }
}
