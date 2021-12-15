import {ExpandVideoHelper} from './ExpandVideoHelper';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';


export class ExpandLeftHelper extends ExpandVideoHelper {
  public readonly newChangeType = LcEntryChangeType.START;

  public getTargetFrameNumber(change: LcEntryChange): number {
    return change.frameNumber <= this.currentFrameNumber
      ? this.startFrame // Math.max(this.startFrame, change.frameNumber - this.offset)
      : this.currentFrameNumber;
  }

  public getChangeToCombine(geo: LcEntryGeometryValue, change: LcEntryChange, target: number): LcEntryChange|undefined {

    const frameNumbers = geo.change.map(c => c.frameNumber);
    const lower = frameNumbers.filter(frameNumber => frameNumber < change.frameNumber);
    const boxed = lower.filter(frameNumber => target - 1 <= frameNumber);

    if (boxed.length === 0) {
      return undefined;
    }

    const targetChangeFrameNumber = Math.max(...boxed);

    return geo.change.find(c => c.frameNumber === targetChangeFrameNumber);
  }

  /**
   * `change` must be either START or START_END.
   * Update to CHANGE or END.
   * @param change
   */
  public getChangeType(change: LcEntryChange): LcEntryChangeType {
    return change.frameType !== LcEntryChangeType.START_END
      ? LcEntryChangeType.CHANGE
      : LcEntryChangeType.END;
  }

  /**
   * `mergeChange` must be either START or START_END.
   * Update to CHANGE or START.
   * @param mergeChange
   */
  public getMergeChangeType(mergeChange: LcEntryChange): LcEntryChangeType {
    return mergeChange.frameType !== LcEntryChangeType.START_END
      ? LcEntryChangeType.CHANGE
      : LcEntryChangeType.START;
  }


}
