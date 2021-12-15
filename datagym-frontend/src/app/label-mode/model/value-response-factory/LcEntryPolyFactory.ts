import {LcEntry} from '../import';
import {Point} from '../geometry/Point';
import {LcEntryPolyValue} from '../geometry/LcEntryPolyValue';
import {LcEntryPolyChange} from '../change/LcEntryPolyChange';


export class LcEntryPolyFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryPolyValue {
    return new LcEntryPolyValue(
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

  public static change(data: {[key: string]: any}): LcEntryPolyChange {
    return new LcEntryPolyChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      (data as LcEntryPolyChange).points
    );
  }
}
