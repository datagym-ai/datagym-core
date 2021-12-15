import {PointsCollection} from '../../model/PointsCollection';


export class ImageSegmentationGeometryData {

  public get comment(): string {
    return undefined;
  }

  public set comment(_: string) {
    const errorMessage = 'ImageClassificationGeometryData has no comment';
    throw new Error(errorMessage);
  }

  constructor(
    public pointsCollection: PointsCollection[],
  ) {}

}
