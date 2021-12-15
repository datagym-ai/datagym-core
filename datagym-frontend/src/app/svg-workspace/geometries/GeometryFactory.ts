import {GeometryProperties} from './GeometryProperties';
import {BaseGeometry} from './BaseGeometry';
import {GeometryType} from './GeometryType';
import {RectangleGeometry} from './geometry/RectangleGeometry';
import {PolygonGeometry} from './geometry/PolygonGeometry';
import {LineGeometry} from './geometry/LineGeometry';
import {PointGeometry} from './geometry/PointGeometry';
import {ImageSegmentationGeometry} from './geometry/ImageSegmentationGeometry';
import {PolyLineGeometry} from './geometry/PolyLineGeometry';
import {PointsGeometry} from './geometry/PointsGeometry';
import {EdgeLineGeometry} from './geometry/EdgeLineGeometry';
import {WorkspaceInternalService} from '../service/workspace-internal.service';

export class GeometryFactory {

  /**
   * Create the workspace geometry depending on the geometry type.
   *
   * @param workspace to access the workspace service within the geometry.
   * @param geometryProperties pass to the geometry.
   */
  public static create(workspace: WorkspaceInternalService, geometryProperties: GeometryProperties): BaseGeometry {

    let latestCreatedGeometry: BaseGeometry;
    switch (geometryProperties.geometryType) {
      case GeometryType.RECTANGLE:
        latestCreatedGeometry = new RectangleGeometry(workspace, geometryProperties);
        break;
      case GeometryType.POLYGON:
        latestCreatedGeometry = new PolygonGeometry(workspace, geometryProperties);
        break;
      case GeometryType.LINE:
        latestCreatedGeometry = new LineGeometry(workspace, geometryProperties);
        break;
      case GeometryType.POINT:
        latestCreatedGeometry = new PointGeometry(workspace, geometryProperties);
        break;
      case GeometryType.IMAGE_SEGMENTATION:
        latestCreatedGeometry = new ImageSegmentationGeometry(workspace, geometryProperties);
        break;
      /**
       * The eraser is just a frontend tool.
       */
      case GeometryType.IMAGE_SEGMENTATION_ERASER:
        // this uses also the ImageSegmentationGeometry type
        latestCreatedGeometry = new ImageSegmentationGeometry(workspace, geometryProperties);
        (latestCreatedGeometry as ImageSegmentationGeometry).isEraser = true;
        break;
      /**
       * The following geometry types are not known within the backend. They are only used
       * with the AISeg tool to select the area of interest.
       */
      case GeometryType.POLYLINE:
        latestCreatedGeometry = new PolyLineGeometry(workspace, geometryProperties);
        break;
      case GeometryType.POINTS:
        latestCreatedGeometry = new PointsGeometry(workspace, geometryProperties);
        break;
      case GeometryType.APPROXIMATE_EDGES:
        latestCreatedGeometry = new EdgeLineGeometry(workspace, geometryProperties);
        break;
    }

    return latestCreatedGeometry;
  }

}
