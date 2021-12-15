import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {Observable, of} from 'rxjs';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {DeleteHelper} from './DeleteHelper';

/**
 * Delete a CHANGE change object.
 * Just remove that and redraw the value line.
 */
export class ChangeDeleteHelper extends DeleteHelper {
  readonly kind = LcEntryChangeType.CHANGE;

  public handlePreviousChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {
    // Don't care about the previous change object. Must be of type START.
    return of(null);
  }
  public handleNextChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {
    // Don't care about the next change object. Must be of type CHANGE or END.
    return of(null);
  }
}
