/**
 * button = VideoControlButton.
 *
 * Use within the template like:
 *
 *  <app-action-icon class="action-icon" [icon]="button.icon"
 *    [active]="button.frameSkipPossible"
 *    (onClick)="videoController.seekFrame(-button.offset)"></app-action-icon>
 */
export class VideoControlButton {

  /**
   * Is at the target index a possible frame?
   */
  public readonly frameSkipPossible: boolean = undefined;

  public constructor(
    public readonly icon: string,
    public readonly offset: number,
    private totalFrames: number,
    currentFrameNumber: number,
  ) {
    const newFrame = currentFrameNumber + offset;
    this.frameSkipPossible = 0 <= newFrame && newFrame <= totalFrames;
  }

  /**
   * A simpler update mechanism.
   *
   * This doesn't change the reference if the frameSkipPossible doesn't change.
   * Changes of other properties is not supported with this method.
   *
   * @param currentFrameNumber
   */
  public forFrame(currentFrameNumber: number): VideoControlButton {
    const newInstance = new VideoControlButton(this.icon, this.offset, this.totalFrames, currentFrameNumber);
    return this.frameSkipPossible !== newInstance.frameSkipPossible
      ? newInstance
      : this;
  }

  public static FastBackward(): VideoControlButton;
  public static FastBackward(totalFrames: number, currentFrameNumber: number): VideoControlButton;
  public static FastBackward(totalFrames: number = 0, currentFrameNumber: number = 0): VideoControlButton {
    const offset = -10;
    return new VideoControlButton('fa-backward', offset, totalFrames, currentFrameNumber);
  }

  public static Backward(): VideoControlButton;
  public static Backward(totalFrames: number, currentFrameNumber: number): VideoControlButton;
  public static Backward(totalFrames: number = 0, currentFrameNumber: number = 0): VideoControlButton {
    const offset = -1;
    return new VideoControlButton('fa-step-backward', offset, totalFrames, currentFrameNumber);
  }

  public static Forward(): VideoControlButton;
  public static Forward(totalFrames: number, currentFrameNumber: number): VideoControlButton;
  public static Forward(totalFrames: number = 0, currentFrameNumber: number = 0): VideoControlButton {
    const offset = 1;
    return new VideoControlButton('fa-step-forward', offset, totalFrames, currentFrameNumber);
  }

  public static FastForward(): VideoControlButton;
  public static FastForward(totalFrames: number, currentFrameNumber: number): VideoControlButton;
  public static FastForward(totalFrames: number = 0, currentFrameNumber: number = 0): VideoControlButton {
    const offset = 10;
    return new VideoControlButton('fa-forward', offset, totalFrames, currentFrameNumber);
  }

}
