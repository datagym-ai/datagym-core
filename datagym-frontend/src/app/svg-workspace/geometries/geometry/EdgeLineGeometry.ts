import {WorkspaceInternalService} from '../../service/workspace-internal.service';
import {PolyLineGeometry} from './PolyLineGeometry';
import {Element, PointArray} from 'svg.js';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import * as SVGType from '../../model/utility/SVGTypes';
import {AiSegCalculate} from '../../model/AiSegCalculate';
import {GeometryProperties} from '../GeometryProperties';

/**
 * @deprecated This Geometry type is not fully implemented. It should be used as another
 * tool for media segmentation. Its not supported by the select menu within the toolbar
 * and could not be used by the user. This code stays here to not start by zero on creating
 * this selection tool. To activate it just re-add the AiSegType.EDGE_LINE to the AisegTypes
 * stack within the ToolBarComponent and remove this deprecation annotation.
 */
export class EdgeLineGeometry extends PolyLineGeometry {

  protected readonly className = 'EdgeLineGeometry';

  /**
   * @override
   */
  protected get comment() : string{
    const errorMessage = `comment is not supported on ${ this.className }`;
    throw new Error(errorMessage);
  }

  /**
   * @param workspace
   * @param geometryProperties
   */
  constructor(workspace: WorkspaceInternalService, geometryProperties: GeometryProperties) {
    super(workspace, geometryProperties);
    // we don't want borders to be drawn.
    this.geometryProperties.border = false;
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, number: number = 0): AiSegCalculate {
    // this method should be implemented if the edge line geometry is added to the aiseg selection tool.
    throw new Error('createAiSegCalculationObject is not supported on PolyLineGeometry');
  }

  /**
   * Read the points array and return them as workspace point array.
   *
   * @param svg
   */
  protected svgElementToPoints(svg?: Element): WorkspacePoint[] {
    const svgElement = svg === null || svg === undefined ? this.svgObject : svg;
    const drawedPoints: PointArray = (svgElement as unknown as SVGType.WithArray).array();
    const points = [];
    for (const point of drawedPoints.value) {
      const pointX: number = point[0];
      const pointY: number = point[1];

      points.push(new WorkspacePoint(pointX, pointY)); // middle point
    }
    return points;
  }

}
