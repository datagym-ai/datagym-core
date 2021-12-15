import {BaseGeometry} from '../BaseGeometry';
import {PointArray} from 'svg.js';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import * as SVGType from '../../model/utility/SVGTypes';
import {AiSegCalculate} from '../../model/AiSegCalculate';


export abstract class PolyLikeGeometry extends BaseGeometry {

  /**
   * The class name may be used for some logging / error messages.
   */
  protected abstract readonly className: string = '';

  public points: WorkspacePoint[] = [];

  // This flag should be public, because aiseg polygon uses this flag.
  public inDrawingMode: boolean = false;

  protected onDrawStopEvent(event): void {
    this.inDrawingMode = false;
  }

  public afterSelecting(): void {
  }

  public beforeSelecting(): void {
  }

  public beforeUnselecting(): void {
  }

  protected onBeforeDragEvent(event): void {
  }

  protected onDragEndEvent(event): void {
  }

  protected onDragStartEvent(event): void {
  }

  protected onResizeDoneEvent(event): void {
  }

  protected onResizeStartEvent(event): void {
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, number: number = 0): AiSegCalculate {
    const errorMessage = `createAiSegCalculationObject is not supported on ${ this.className }`;
    throw new Error(errorMessage);
  }

  public toDataString(): string {
    return `${ this.className } (
    id: ${this.svgObject.id()},
    points: ${JSON.stringify(this.points)},
    )`;
  }

  /**
   * Sets all geometry specific parameter with the svg-object (like x,y and positions)
   */
  public syncDataToSvg(): void;
  public syncDataToSvg(conversionHeight: number, conversionWidth: number): void;
  public syncDataToSvg(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    const plotArray: WorkspacePoint[] = this.points.map(pos => new WorkspacePoint(pos.x / conversionHeight, pos.y / conversionWidth));
    (this.svgObject as unknown as SVGType.WithPlot).plot(plotArray);
  }

  /**
   * Synchronizes the svg object with the data object
   */
  public syncSvgToData(): void;
  public syncSvgToData(conversionHeight: number, conversionWidth: number): void;
  public syncSvgToData(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    const drawedPoints: PointArray = (this.svgObject as unknown as SVGType.WithArray).array();
    const points = [];
    for (const drawedPoint of drawedPoints.value) {
      const x = drawedPoint[0] * conversionHeight;
      const y = drawedPoint[1] * conversionWidth;
      points.push(new WorkspacePoint(x, y));
    }
    this.points = points;
  }

  /**
   * In (some) drawing events, the property this.points is not set yet.
   * So access the points direct from the svg object.
   */
  protected get svgPointList(): SVGPointList {
    return ((this.svgObject as unknown as SVGType.WithNode).node?.points || []) as SVGPointList;
  }

  /**
   * Finish drawing
   */
  protected finishDrawing(): void {
    if (!this.inDrawingMode) {
      return;
    }

    this.inDrawingMode = false;
    (this.svgObject as unknown as SVGType.WithDraw).draw('stop');
    (this.svgObject as unknown as SVGType.WithDraw).draw('done');
    (this.svgObject as unknown as SVGType.WithOff).off('drawstart');
  }
}
