import {LcEntry} from '../import';
import {LcEntryPointValue} from '../geometry/LcEntryPointValue';
import {LcEntryPointChange} from '../change/LcEntryPointChange';
import {Point} from '../geometry/Point';


export class LcEntryPointFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryPointValue{
    return new LcEntryPointValue(
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
      data?.point?.x || data?.x || null,
      data?.point?.y || data?.y || null,
      data.comment || null
    );
  }

  public static change(data: {[key: string]: any}): LcEntryPointChange {

    const asPoint = data as LcEntryPointChange;
    const point = !/*not*/!!asPoint.point
      ? new Point(asPoint.x, asPoint.y)
      : asPoint.point;

    return new LcEntryPointChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      point
    );
  }
}
