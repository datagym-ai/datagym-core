
/**
 * Describe the displayed numbers (or minus sign) above the frame bars
 * as immutable objects.
 */
export class FrameNumberDescription {

  public readonly frameClasses: {[key: string]: boolean} = {};

  /**
   * To display the frame number or a minus sign if the frame would be out of bounds.
   */
  public readonly label: string|number = undefined;

  public constructor(
    // offset like -3, 0, 6
    public readonly frameNumberOffset: number,
    public readonly framesBarsOffset: number,
    currentFrameNumber: number,
    totalFrames: number,
  ) {
    const frameByOffset = currentFrameNumber + frameNumberOffset;
    const outOfBounds = frameByOffset < 0 || frameByOffset > totalFrames;

    this.label = !outOfBounds ? frameByOffset : '-';

    const classes = {
      'dg-primary-color': this.frameNumberOffset === 0,
      'dg-light': outOfBounds,
      'out-of-bounds': outOfBounds,
      'pointer': !outOfBounds
    };

    classes[`frame-${frameByOffset}`] = true;
    classes[`frame-offset-${this.frameNumberOffset}`] = true;

    this.frameClasses = classes;
  }
}

