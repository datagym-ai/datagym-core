import {LineGeometryData} from './LineGeometryData';
import {PointGeometryData} from './PointGeometryData';
import {PolygonGeometryData} from './PolygonGeometryData';
import {RectangleGeometryData} from './RectangleGeometryData';
import {ImageSegmentationGeometryData} from './ImageSegmentationGeometryData';

export type BaseGeometryData = LineGeometryData
  | PointGeometryData
  | PolygonGeometryData
  | RectangleGeometryData
  | ImageSegmentationGeometryData
  ;
