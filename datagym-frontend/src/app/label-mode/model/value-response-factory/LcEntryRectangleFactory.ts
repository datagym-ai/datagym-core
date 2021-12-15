import {LcEntry} from '../import';
import {LcEntryRectangleValue} from '../geometry/LcEntryRectangleValue';
import {LcEntryRectangleChange} from '../change/LcEntryRectangleChange';


export class LcEntryRectangleFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryRectangleValue{
    return new LcEntryRectangleValue(
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
      data?.width || null,
      data?.height || null,
      data.comment || null
    );
  }

  public static change(data: {[key: string]: any}): LcEntryRectangleChange {
    return new LcEntryRectangleChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      (data as LcEntryRectangleChange).x,
      (data as LcEntryRectangleChange).y,
      (data as LcEntryRectangleChange).width,
      (data as LcEntryRectangleChange).height
    );
  }
}
