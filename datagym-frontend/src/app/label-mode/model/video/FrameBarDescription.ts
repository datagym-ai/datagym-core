/**
 * Describe the (maybe) clickable frame bars.
 */
export class FrameBarDescription {

  public readonly hasPointer: boolean = false;

  /**
   * Some css styling applied via [ngClass]
   */
  public readonly barClasses: {
    future: boolean,
    past: boolean,
    active: boolean
  };

  public constructor(
    public readonly offset: number,
    currentFrame: number,
    framesBarsOffset: number,
    totalFrames: number
  ) {
    this.hasPointer = 0 < offset && currentFrame + framesBarsOffset < totalFrames;

    this.barClasses = {
      future: offset > 0,
      past: offset < 0,
      active: offset === 0,
      // hovered: hoveredNumberOffset == barOffset
    };
  }
}
