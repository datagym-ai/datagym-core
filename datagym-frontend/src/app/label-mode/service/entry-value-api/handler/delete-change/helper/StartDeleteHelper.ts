import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {Observable, of} from 'rxjs';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {DeleteHelper} from './DeleteHelper';

/**
 * We would delete a START change object.
 * Update the following KeyFrame to START or START_END if it was of type END.
 */
export class StartDeleteHelper extends DeleteHelper {
  readonly kind = LcEntryChangeType.START;

  public handlePreviousChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {
    // Don't care about the previous change object. Must be of type END or START_END.
    return of(null);
  }

  public handleNextChange(geometry: LcEntryGeometryValue, targetFrameNumber: number): Observable<LcEntryChange> {

    const nextKeyFrameNumbers = geometry.frameNumbers.filter(f => f > targetFrameNumber);

    if (nextKeyFrameNumbers.length === 0) {
      /*
       * No following keyFrame.
       * Delete the full geometry instead?
       * Need some other architecture.
       */
      return of(null);
    }

    const nextKeyFrameNumber = Math.min(...nextKeyFrameNumbers); // Would produce -Infinity for empty array.
    const nextKeyFrame = geometry.change.find(c => c.frameNumber === nextKeyFrameNumber);

    if (!/*not*/!!nextKeyFrame) {
      /*
       * No following keyFrame.
       * Delete the full geometry instead?
       * Need some other architecture.
       */
      return of(null);
    }

    if (LcEntryChangeType.isStart(nextKeyFrame)) {
      // Nothing to do. All ok.
      return of(null);
    }

    /*
     * We delete an START change but the following keyFrame is neither START nor START_END. Update that one.
     */
    nextKeyFrame.frameType = nextKeyFrame.frameType === LcEntryChangeType.END
      ? LcEntryChangeType.START_END
      : LcEntryChangeType.START;

    return this.changeApi.update(nextKeyFrame);
  }
}
