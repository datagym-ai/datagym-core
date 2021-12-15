import { RectangleGeometryData } from '../geometry-data/RectangleGeometryData';
import { WorkspacePoint } from '../../model/WorkspacePoint';
import svgjs, {Element} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {AiSegCalculate, DEFAULT_NUM_POINTS} from '../../model/AiSegCalculate';
import {PointGeometry} from './PointGeometry';


export class RectangleGeometry extends PointGeometry {

  public width: number = 0;
  public height: number = 0;

  /**
   * Sets the specific start position
   * @param startX The specific x-position
   * @param startY THe specific y-position
   * @param internal Should the svg-object also been updated
   */
  public setStartPoint(startX: number, startY: number, internal: boolean = false) {
    this.startPointX = startX;
    this.startPointY = startY;

    if (internal) {
      this.svgObject.x(startX);
      this.svgObject.y(startY);
    }
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    this.svgObject = (svgLayer.rect() as unknown as Element);
  }

  public startDrawing(): void {
    // Hide data definition because the svg-plugins sadly doesnt have type definitions
    (this.svgObject as unknown as SVGType.WithDraw).draw();
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, numPoints: number = DEFAULT_NUM_POINTS): AiSegCalculate {
    const half = 2;
    const aisegCalc = new AiSegCalculate();

    const topLeft = new WorkspacePoint(this.startPointX, this.startPointY);
    const topRight = new WorkspacePoint(this.startPointX + this.width, this.startPointY);
    const bottomLeft = new WorkspacePoint(this.startPointX, this.startPointY + this.height);
    const bottomRight = new WorkspacePoint(this.startPointX + this.width, this.startPointY + this.height);
    aisegCalc.negativePoints = [topLeft, topRight, bottomLeft, bottomRight];
    // Calculate mid-point of rectangle
    aisegCalc.positivePoints = [new WorkspacePoint((bottomLeft.x + bottomRight.x) / half, (topLeft.y + bottomRight.y) / half)];

    aisegCalc.numPoints = numPoints;
    aisegCalc.imageId = currentImageId;

    return aisegCalc;
  }

  /**
   * Register events to update width-height
   */
  protected registerCustomDrawEvent(): void {
    // Don't use PointGeometries registerCustomDrawEvent method.
  }

  public syncDataToSvg(): void;
  public syncDataToSvg(conversionHeight: number, conversionWidth: number): void;
  public syncDataToSvg(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.svgObject.x(this.startPointX / conversionHeight)
      .y(this.startPointY / conversionWidth)
      .width(this.width / conversionWidth)
      .height(this.height / conversionHeight);
  }

  public syncSvgToData(): void;
  public syncSvgToData(conversionHeight: number, conversionWidth: number): void;
  public syncSvgToData(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.width = this.svgObject.width() * conversionWidth;
    this.height = this.svgObject.height() * conversionHeight;
    this.startPointX = this.svgObject.x() * conversionHeight;
    this.startPointY = this.svgObject.y() * conversionWidth;
  }

  getGeometryData(): RectangleGeometryData {
    this.syncSvgToData(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
    return new RectangleGeometryData(new WorkspacePoint(this.startPointX, this.startPointY), this.height, this.width, this.comment);
  }

  setGeometryData(data: RectangleGeometryData) {
    this.height = data.height;
    this.width = data.width;
    this.startPointX = data.startPoint.x;
    this.startPointY = data.startPoint.y;
    this.syncDataToSvg(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
  }


  public toDataString(): string {
    return `RectangleGeometry (
    id: ${this.svgObject.id()},
    startPointX: ${this.startPointX},
    startPointY: ${this.startPointY},
    height: ${this.height},
    width: ${this.width},
    )`;
  }
}
