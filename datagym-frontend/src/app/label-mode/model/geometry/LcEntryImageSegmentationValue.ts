import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryGeometryValue} from './LcEntryGeometryValue';
import {PointsCollection} from '../../../svg-workspace/model/PointsCollection';
import {LcEntryValue} from '../LcEntryValue';
import {Point} from './Point';
import {LcEntryType} from '../import';
import {LcEntryChange} from '../change/LcEntryChange';

export class LcEntryImageSegmentationValue extends LcEntryGeometryValue {
  readonly kind: LcEntryType.IMAGE_SEGMENTATION|LcEntryType.IMAGE_SEGMENTATION_ERASER = LcEntryType.IMAGE_SEGMENTATION;

  public pointsCollection: PointsCollection[];

  public constructor(
    id: string | null,
    lcEntry: LcEntry | null,
    media: string | null,
    labelIteration: LabelIteration | string | null,
    timestamp: number | null,
    labeler: string | null,
    children: LcEntryValue[] | null,
    parent: string | null,
    valid: boolean,
    pointsCollection: PointsCollection[] | null,
    comment?: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid, comment);
    this.pointsCollection = pointsCollection; // || [];
    // Note: the comment is ignored and not supported on this geometry type.
  }

  public createClone(): LcEntryImageSegmentationValue {
    const errorMessage = 'Method `clone` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public withChange(change: LcEntryChange): LcEntryImageSegmentationValue {
    const errorMessage = 'Method `withChange` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public eatValues(_: LcEntryValue): void {
    const errorMessage = 'Method `eatValues` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public hasCoordinates(): boolean {
    return this.pointsCollection && this.pointsCollection.length > 0;
  }

  protected isThisEntryValid(): boolean {
    return this.pointsCollection !== undefined;
  }

  public move(offset: Point): void {
    const errorMessage = 'Method `move` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public moveTo(position: Point): void {
    const errorMessage = 'Method `move` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public getDistance(position: Point): Point {
    const errorMessage = 'Method `getDistance` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public outOfBounds(size: Point): boolean {
    const errorMessage = 'Method `outOfBounds` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

  public getBoundingBox(): Point {
    const errorMessage = 'Method `getBoundingBox` is not supported on LcEntryImageSegmentationValue';
    throw new Error(errorMessage);
  }

}
