import {BaseGeometry} from '../BaseGeometry';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import {PointGeometryData} from '../geometry-data/PointGeometryData';
import svgjs, {Element} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {AiSegCalculate, DEFAULT_NUM_POINTS} from '../../model/AiSegCalculate';
import {BaseGeometryData} from '../geometry-data/BaseGeometryData';


export class PointGeometry extends BaseGeometry {

  public startPointX: number = 0;
  public startPointY: number = 0;

  afterSelecting() {
  }

  beforeSelecting() {
  }

  beforeUnselecting() {
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    const size: number = 5;
    this.svgObject = (svgLayer.circle(size) as unknown as Element);
  }

  protected onBeforeDragEvent(event): void {
  }

  protected onDragStartEvent(event): void {
  }

  protected onDragEndEvent(event): void {
  }

  protected onDrawStopEvent(event): void {
  }

  protected onResizeDoneEvent(event): void {
  }

  protected onResizeStartEvent(event): void {
  }

  protected registerCustomDrawEvent(): void {
    this.svgObject.on('drawupdate', () => {
      // If user clicked at the svg layer, we know the specific coordinates,
      // abort the drawing and set a custom radius
      (this.svgObject as unknown as SVGType.WithDraw).draw('stop');
      (this.svgObject as unknown as SVGType.WithRadius).radius(this.workspace.calculateSelectCircleRadius(true));
    });
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, numPoints: number = DEFAULT_NUM_POINTS): AiSegCalculate {
    const aisegCalc = new AiSegCalculate();

    aisegCalc.negativePoints = [];
    aisegCalc.positivePoints = [new WorkspacePoint(this.startPointX, this.startPointY)];
    aisegCalc.numPoints = numPoints;
    aisegCalc.imageId = currentImageId;

    return aisegCalc;
  }

  public startDrawing(): void {
    (this.svgObject as unknown as SVGType.WithDraw).draw();
  }

  public syncDataToSvg(): void;
  public syncDataToSvg(conversionHeight: number, conversionWidth: number): void;
  public syncDataToSvg(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.svgObject.x(this.startPointX / conversionHeight)
      .y(this.startPointY / conversionWidth);
  }

  public syncSvgToData(): void;
  public syncSvgToData(conversionHeight: number, conversionWidth: number): void;
  public syncSvgToData(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.startPointX = this.svgObject.x() * conversionHeight;
    this.startPointY = this.svgObject.y() * conversionWidth;
  }

  getGeometryData(): BaseGeometryData {
    this.syncSvgToData(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
    return new PointGeometryData(new WorkspacePoint(this.startPointX, this.startPointY), this.comment);
  }

  setGeometryData(data: BaseGeometryData) {
    this.startPointX = (data as PointGeometryData).point.x;
    this.startPointY = (data as PointGeometryData).point.y;
    this.syncDataToSvg(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
  }

  public toDataString(): string {
    return `PointGeometry (
    id: ${this.svgObject.id()},
    startPointX: ${this.startPointX},
    startPointY: ${this.startPointY})`;
  }
}
