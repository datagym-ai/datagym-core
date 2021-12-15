import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {Observable, of} from 'rxjs';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {DeleteHelper} from './DeleteHelper';


/**
 * We would delete a END change object.
 * Update the previous one to END or START_END if it was of type START.
 */
export class EndDeleteHelper extends DeleteHelper {
  readonly kind = LcEntryChangeType.END;

  public handlePreviousChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {

    const previousKeyFrameNumbers = geometry.frameNumbers.filter(f => f < targetFrameNumber);

    if (previousKeyFrameNumbers.length === 0) {
      /*
       * No previous keyFrame.
       * Delete the full geometry instead?
       * Need some other architecture.
       */
      return of(null);
    }

    const previousKeyFrameNumber = Math.max(...previousKeyFrameNumbers);  // Would produce -Infinity for empty array.
    const previousKeyFrame = geometry.change.find(c => c.frameNumber === previousKeyFrameNumber);

    if (!/*not*/!!previousKeyFrame || LcEntryChangeType.isEnd(previousKeyFrame)) {
      /*
       * No previous keyFrame.
       * Delete the full geometry instead?
       * Need some other architecture.
       */
      return of(null);
    }

    /*
     * We delete an END change but the previous keyFrame is neither END nor START_END. Update that one.
     */
    previousKeyFrame.frameType = previousKeyFrame.frameType === LcEntryChangeType.START
      ? LcEntryChangeType.START_END
      : LcEntryChangeType.END;

    return this.changeApi.update(previousKeyFrame);
  }

  public handleNextChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {
    // Don't care about the next change object. Must be of type START or START_END.
    return of(null);
  }
}
