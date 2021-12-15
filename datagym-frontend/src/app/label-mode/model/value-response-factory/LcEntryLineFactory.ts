import {LcEntry} from '../import';
import {Point} from '../geometry/Point';
import {LcEntryLineValue} from '../geometry/LcEntryLineValue';
import {LcEntryLineChange} from '../change/LcEntryLineChange';


export class LcEntryLineFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryLineValue {
    return new LcEntryLineValue(
      // shared attributes
      data.id,
      lcEntry,
      data.mediaId,
      data.labelIterationId,
      data.timestamp,
      data.labeler,
      [],
      data.parent,
      data.valid,
      // custom attributes
      (data.points || []).filter(point => point.x !== null && point.y !== null).map(point => new Point(point.x, point.y)),
      data.comment || null
    );
  }

  public static change(data: {[key: string]: any}): LcEntryLineChange {
    return new LcEntryLineChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      (data as LcEntryLineChange).points
    );
  }
}
