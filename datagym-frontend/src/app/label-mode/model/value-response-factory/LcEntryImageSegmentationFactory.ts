import {PointsCollection} from '../../../svg-workspace/model/PointsCollection';
import {Point} from '../geometry/Point';
import {LcEntryImageSegmentationValue} from '../geometry/LcEntryImageSegmentationValue';
import {LcEntry} from '../import';
import {LcEntryChange} from '../change/LcEntryChange';


export class LcEntryImageSegmentationFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryImageSegmentationValue {
    return new LcEntryImageSegmentationValue(
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
      // Note: points is here a stack of points!
      (data.pointsCollection || []).map(collection => new PointsCollection(
        collection.points.filter(point => !!point.x && !!point.y).map(point => new Point(point.x, point.y)),
        collection.id)
      ),
      data.comment || null
    );
  }

  public static change(data: {[key: string]: any}): LcEntryChange {
    return undefined;
  }

}
