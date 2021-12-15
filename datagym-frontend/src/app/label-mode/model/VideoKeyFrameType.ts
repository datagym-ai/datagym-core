
export enum VideoKeyFrameType {

  START = 'START',

  CHANGE = 'CHANGE',

  END = 'END',

  /**
   * Interpolation is only supported for rectangles.
   * Maybe also for points. Not sure now if a separate
   * KeyFrameType is necessary for that.
   */
  INTERPOLATION = 'INTERPOLATION'
}

export namespace VideoKeyFrameType {
  /**
   * Get the values of the enum.
   */
  export function values(): VideoKeyFrameType[] {
    return Object.keys(VideoKeyFrameType)
      // filter keys
      .filter(key => typeof VideoKeyFrameType[key] === 'string')
      // map to enum types
      .map(key => VideoKeyFrameType[key]);
  }

  export function inEnum(value2test: string|unknown): boolean {
    return typeof value2test === 'string' &&
      Object.keys(VideoKeyFrameType)
      .filter(type => typeof type === 'string')
      .includes(value2test);
  }
}
