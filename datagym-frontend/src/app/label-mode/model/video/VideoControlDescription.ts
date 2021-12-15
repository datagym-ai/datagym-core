

/**
 * Describe the video control component.
 */
export class VideoControlDescription {

  /**
   * This values are required within the template of the video controller and to calculate
   * the labels within the current view port.
   */
  public readonly framesBars: number[] = [];
  public readonly framesNumbers: number[] = [];
  public readonly frameBarRange: number;
  public readonly frameWidthPercentage: number;
  public readonly framesBarsOffset: number;

  constructor(frameBarCounter: number, frameNumberCounter: number) {
    const two = 2;
    const hundred = 100;

    /*
     * Create the `displayFrames` array depending on the following `frames2display` number.
     * The resulting array has always an odd length and zero is the middle item. The range
     * depends on `frames2display`.
     */

    this.framesBars = this.createOffsetArrays(frameBarCounter);
    this.framesBarsOffset = this.framesBars[0];
    this.framesNumbers = this.createOffsetArrays(frameNumberCounter)
      .map((v, i, s) => Math.round(v * this.framesBars.length / s.length));

    this.frameBarRange = (this.framesBars.length - 1) / two;
    this.frameWidthPercentage = hundred / this.framesBars.length;
  }

  /**
   * Create an array with n items where zero is always the middle item and the range
   * is from -n to n.
   *
   * Note: On even numbers, n is always set to the next higher odd number.
   *
   * E.g.
   * numberOfItems = 13 => [-6, ... 0, ... 6]
   * numberOfItems = 14 => [-7, ... 0, ... 7]
   * numberOfItems = 15 => [-7, ... 0, ... 7]
   */
  private createOffsetArrays(numberOfItems: number): number[] {
    const two = 2;

    const length = numberOfItems + 1 - numberOfItems % two;
    const half = (length - 1) / two;

    return Array.from({length})
      .map((v, i) => i - half);
  }
}
