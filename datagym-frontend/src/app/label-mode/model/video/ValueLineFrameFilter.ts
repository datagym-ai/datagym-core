import {LcEntryGeometryValue} from '../geometry/LcEntryGeometryValue';

/**
 * Filter class used within the `VideoMediaController.initFrame()` to filter
 * the stack of available video value lines before initiating the `EntryValueService`.
 */
export class ValueLineFrameFilter {

  /**
   * minFrame & maxFrame define the current viewport.
   */
  public readonly minFrame: number = undefined;
  public readonly maxFrame: number = undefined;

  /**
   * @param frameNumber within the viewport.
   * @param range within the viewport.
   * @param limit the maximum number of available frames.
   */
  constructor(readonly frameNumber: number, private readonly range: number, readonly limit: number = undefined) {
    this.minFrame = Math.max(0, frameNumber - range);
    this.maxFrame = Math.min(frameNumber + range, limit);
  }

  /**
   * Used as callback via `Array.filter()`;
   *
   * @param value
   */
  public filter(value: LcEntryGeometryValue): boolean {

    const keyFrames = value.frameNumbers;
    const startFrame = Math.min(...keyFrames);
    const endFrame = Math.max(...keyFrames);

    // the frame is greater than the viewport
    return startFrame <= this.minFrame && this.maxFrame <= endFrame ||
      // the start frame of the value is within the viewport
      this.minFrame <= startFrame && startFrame <= this.maxFrame ||
      // the end frame of the value is within the viewport
      this.minFrame <= endFrame && endFrame <= this.maxFrame;
  }
}
