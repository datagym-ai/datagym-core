import {LcEntryChange} from './LcEntryChange';


export enum LcEntryChangeType {

  /**
   * If a geometry is visible within more than one frame
   * this marks the START frame. A new drawn geometry will
   * start at the current frameNumber and end on the last
   * possible frameNumber.
   */
  START = 'START',
  /**
   * A CHANGE can only be 'wrapped' within a START & END change type
   * (ordered by frameNumber).
   */
  CHANGE = 'CHANGE',
  /**
   * The last frame of the value or the segment.
   */
  END = 'END',
  /**
   * Geometries that are only visible within one frame
   * are marked with the LcEntryChangeType.START_END.
   */
  START_END = 'START_END',
  /**
   * Just for FE internal usage. This type is not known within the BE.
   */
  INTERPOLATED = 'INTERPOLATED',
  /**
   * Just for FE internal usage. This type is not known within the BE.
   */
  IMAGE = 'IMAGE'
}

export namespace LcEntryChangeType {
  /**
   * Get the values of the enum.
   */
  export function values(): LcEntryChangeType[] {
    return Object.keys(LcEntryChangeType)
      // filter keys
      .filter(key => typeof LcEntryChangeType[key] === 'string')
      // map to enum types
      .map(key => LcEntryChangeType[key]);
  }

  export function inEnum(value2test: string|unknown): boolean {
    return typeof value2test === 'string' &&
      Object.keys(LcEntryChangeType)
        .filter(type => typeof type === 'string')
        .includes(value2test);
  }

  /**
   * Is the LcEntryChange object of any START type?
   * @param change
   */
  export function isStart(change: LcEntryChange): boolean;

  /**
   * Is the LcEntryChangeType object of any START type?
   * @param kind
   */
  export function isStart(kind: LcEntryChangeType): boolean;

  /**
   * Implementation of the above definitions.
   * @param arg
   */
  export function isStart(arg: LcEntryChange|LcEntryChangeType): boolean {
    const kind = typeof arg === 'object' ? arg.frameType : arg;

    return [LcEntryChangeType.START, LcEntryChangeType.START_END].includes(kind);
  }

  /**
   * Is the LcEntryChange object of any END type?
   * @param change
   */
  export function isEnd(change: LcEntryChange): boolean;

  /**
   * Is the LcEntryChangeType object of any END type?
   * @param kind
   */
  export function isEnd(kind: LcEntryChangeType): boolean;

  /**
   * Implementation of the above definitions.
   * @param arg
   */
  export function isEnd(arg: LcEntryChange|LcEntryChangeType): boolean {
    const kind = typeof arg === 'object' ? arg.frameType : arg;

    return [LcEntryChangeType.END, LcEntryChangeType.START_END].includes(kind);
  }
}
