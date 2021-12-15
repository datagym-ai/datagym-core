import {ExpandVideoHelper} from './ExpandVideoHelper';
import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';


export class ExpandRightHelper extends ExpandVideoHelper {
  public readonly newChangeType = LcEntryChangeType.END;

  public getTargetFrameNumber(change: LcEntryChange): number {
    return this.currentFrameNumber <= change.frameNumber
      ? this.endFrame // Math.min(this.endFrame, change.frameNumber + this.offset)
      : this.currentFrameNumber;
  }

  public getChangeToCombine(geo: LcEntryGeometryValue, change: LcEntryChange, target: number): LcEntryChange|undefined {

    const frameNumbers = geo.change.map(c => c.frameNumber);
    const upper = frameNumbers.filter(frameNumber => frameNumber < target + 1);
    const boxed = upper.filter(frameNumber => change.frameNumber < frameNumber);

    if (boxed.length === 0) {
      return undefined;
    }

    const targetChangeFrameNumber = Math.min(...boxed);

    return geo.change.find(c => c.frameNumber === targetChangeFrameNumber);
  }

  /**
   * `change` must be either END or START_END.
   * Update to START or CHANGE.
   * @param change
   */
  public getChangeType(change: LcEntryChange): LcEntryChangeType {
    return change.frameType !== LcEntryChangeType.START_END
      ? LcEntryChangeType.CHANGE
      : LcEntryChangeType.START;
  }

  /**
   * `mergeChange` must be either START or START_END.
   * Update to CHANGE or END.
   * @param mergeChange
   */
  public getMergeChangeType(mergeChange: LcEntryChange): LcEntryChangeType {
    return mergeChange.frameType !== LcEntryChangeType.START_END
      ? LcEntryChangeType.CHANGE
      : LcEntryChangeType.END;
  }


}

