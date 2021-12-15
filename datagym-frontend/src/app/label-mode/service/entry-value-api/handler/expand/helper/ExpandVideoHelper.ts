import {LcEntryChange} from '../../../../../model/change/LcEntryChange';
import {LcEntryGeometryValue} from '../../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChangeFactory} from '../../../../../model/change/LcEntryChangeFactory';
import {LcEntryChangeType} from '../../../../../model/change/LcEntryChangeType';


export abstract class ExpandVideoHelper {

  public abstract readonly newChangeType: LcEntryChangeType;

  public abstract getTargetFrameNumber(change: LcEntryChange): number;

  /**
   * Check if a change object exists around the target frame number that could be used as merge object.
   *
   * @param geo
   * @param change
   * @param target
   */
  public abstract getChangeToCombine(geo: LcEntryGeometryValue, change: LcEntryChange, target: number): LcEntryChange|undefined;

  public abstract getChangeType(change: LcEntryChange): LcEntryChangeType;

  public abstract getMergeChangeType(mergeChange: LcEntryChange): LcEntryChangeType;

  /**
   *
   * @param startFrame default 0 as start frameNumber
   * @param endFrame the max. possible frameNumber
   * @param currentFrameNumber
   */
  public constructor(protected startFrame: number, protected endFrame: number, protected currentFrameNumber: number) {}

  public copyChange2frame(src: LcEntryChange, frameNumber: number, type: LcEntryChangeType): LcEntryChange {
    return (new LcEntryChangeFactory({frameNumber, type})).fromChangeObject(src);
  }
}
