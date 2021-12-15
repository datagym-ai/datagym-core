import {Point} from '../geometry/Point';


export class ContextMenuConfig {

  public readonly displayDeleteKeyFrameAction: boolean = false;
  public readonly expandLeftPossible: boolean = false;
  public readonly expandRightPossible: boolean = false;

  constructor(
    public readonly position: Point,
    public readonly valueId: string,
    public readonly chunkOffset: number,
    public readonly frameNumber: number,
    private readonly options: {[key: string]: number|boolean}
  ) {

    const defaultOptions = {
      // onExpandLeft
      startFrame: 0,
      endFrame: 0,
      min: 0,
      max: 0,
    };

    const tmpOptions = {...defaultOptions, ...options};

    this.expandLeftPossible = tmpOptions.startFrame < tmpOptions.min;
    this.expandRightPossible = tmpOptions.max < tmpOptions.endFrame;

    this.displayDeleteKeyFrameAction = this.frameNumber !== undefined;
  }

  public withOptions(options: {[key: string]: number|boolean}): ContextMenuConfig {
    return new ContextMenuConfig(
      this.position,
      this.valueId,
      this.chunkOffset,
      this.frameNumber,
      {...this.options, ...options}
    );
  }

  public moveTo(newPosition: Point): ContextMenuConfig {

    return new ContextMenuConfig(
      newPosition,
      this.valueId,
      this.chunkOffset,
      this.frameNumber,
      this.options
    );

  }

}
