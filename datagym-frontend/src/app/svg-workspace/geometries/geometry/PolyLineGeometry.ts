import {BaseGeometryData} from '../geometry-data/BaseGeometryData';
import {PolyLikeGeometry} from './PolyLikeGeometry';
import {WorkspaceInternalService} from '../../service/workspace-internal.service';
import svgjs, {Element, PointArray} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import {AiSegCalculate, DEFAULT_NUM_POINTS} from '../../model/AiSegCalculate';
import {GeometryProperties} from '../GeometryProperties';


const strokeWidth = 25;
const strokeDelta = 3;

/**
 * This geometry type is not known within the backend.
 * It's a implementation detail used to simplify AISeg usage
 * by 'filling out' the area of interest.
 */
export class PolyLineGeometry extends PolyLikeGeometry {

  protected className = 'PolyLineGeometry';

  /**
   * Let the mousewheel change the stroke width
   */
  private strokeWidth: number = strokeWidth;

  /**
   * The mouse wheel event would impact the whole polyline, also
   * the already drawn events. The stroke width should only impact
   * new poly lines so store the previous lines here and start drawing
   * a new one with a new stroke width.
   */
  private polyLines: Element[] = [];
  private currentLine: Element;

  /**
   * 'Calculate' the stroke data on the fly.
   */
  private get strokeData(): {[key:string]: string|number} {
    return {
      width: this.calculateBrushWidth(),
      color: '#ff9900',
      opacity: .5,
      linecap: 'round'
    };
  }

  /**
   * PolyLine geometry is not a 'valid' geometry stored within the backend and so
   * it cannot handle an comment.
   *
   * @param workspace
   * @param geometryProperties
   */
  constructor(workspace: WorkspaceInternalService, geometryProperties: GeometryProperties) {
    super(workspace, geometryProperties);
    // we don't want borders to be drawn.
    this.geometryProperties.border = false;
  }

  /**
   * Override the svg properties from BaseGeometry to
   * fit our needs.
   */
  protected setSvgProperties(): void {
    // don't set the fill properties so we draw only the lines
    // this.svgObject.fill();
    this.svgObject.fill('none');
    this.svgObject.stroke(this.strokeData);
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    // We use the svg object only as event manager / emitter.
    this.svgObject = (svgLayer.polyline([]) as unknown as Element);
    this.svgObject.fill('none');
    this.svgObject.hide();
  }

  protected onDrawStopEvent(event): void {
    this.finishCurrentLine();
    this.inDrawingMode = false;
    const points: WorkspacePoint[] = [];
    this.polyLines.forEach((line: Element) => {
      points.push(...this.svgElementToPoints(line));
    });

    this.points = points;
  }

  public syncSvgToData(): void;
  public syncSvgToData(conversionHeight: number, conversionWidth: number): void;
  public syncSvgToData(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.points = this.points.map((point) => {
      return new WorkspacePoint(
        point.x * conversionHeight,
        point.y * conversionWidth
      );
    });
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, numPoints: number = DEFAULT_NUM_POINTS): AiSegCalculate {
    const aisegCalc = new AiSegCalculate();

    aisegCalc.positivePoints = this.points;
    aisegCalc.negativePoints = [];
    aisegCalc.numPoints = numPoints;
    aisegCalc.imageId = currentImageId;

    return aisegCalc;
  }

  public startDrawing(): void {
    /*
     * Start on first mouse down the drawing
     */
    document.addEventListener('mousedown', (ev) => {
      // To avoid errors in finish drawing, start drawing
      (this.svgObject as unknown as SVGType.WithDraw).draw();

      this.inDrawingMode = true;
      this.currentLine = this.newPolyLine(this.svgObject, ev);
    }, {once: true});
  }

  protected registerCustomDrawEvent(): void {

    const onWheelEvent = (ev: WheelEvent) => {
      if (!this.inDrawingMode) {
        // this.currentLine may be undefined.
        return;
      }
      this.finishCurrentLine();
      this.strokeWidth = this.calculateNewStrokeWidth(ev);
      this.currentLine = this.newPolyLine(this.currentLine, ev);
    };

    const onKeydown = (onkeydown: KeyboardEvent) => {
      if (onkeydown.key === 'Enter') {
        this.finishCurrentLine();
        this.finishDrawing();
      }
    };

    /*
     * Listen once on context menu to finish the polyline.
     */
    document.addEventListener('contextmenu', () => {
      this.finishCurrentLine();
      this.finishDrawing();
    }, {once: true});

    // Finish the polygon also with Enter pressed.
    document.addEventListener('keydown', onKeydown);
    // Let the mouse wheel change the stroke width
    document.addEventListener('wheel', onWheelEvent);

    /*
     * Don't forget to remove the event listeners after drawing or on cancel.
     */
    ['drawdone', 'drawstop', 'drawcancel'].forEach((eventName: string) => {
      this.svgObject.on(eventName, () => {
        this.finishCurrentLine();
        this.polyLines.forEach(line => (line as unknown as SVGType.WithOff).off());
        document.removeEventListener('keydown', onKeydown);
        document.removeEventListener('wheel', onWheelEvent);
        this.polyLines.forEach(line => line.hide());
      });
    });
  }

  /**
   * This method is not implemented because the polyline
   * is not known within the backend.
   */
  public getGeometryData(): BaseGeometryData {
    throw new Error(`getGeometryData is not supported on ${ this.className }`);
  }

  /**
   * This method is not implemented because the polyline
   * is not known within the backend.
   */
  public setGeometryData(data: BaseGeometryData): void {
    throw new Error(`setGeometryData is not supported on ${ this.className }`);
  }

  /**
   * Read the points array and return them as workspace point array.
   *
   * @param svg
   */
  protected svgElementToPoints(svg?: Element): WorkspacePoint[] {
    const points = [];
    const width = this.strokeWidth;
    const svgElement = svg === null || svg === undefined ? this.svgObject : svg;
    const drawedPoints: PointArray = (svgElement as unknown as SVGType.WithArray).array();

    for (const point of drawedPoints.value) {
      const pointX: number = point[0];
      const pointY: number = point[1];

      let tmpPoints = [];
      tmpPoints.push(new WorkspacePoint(pointX, pointY)); // middle point
      tmpPoints.push(new WorkspacePoint(pointX + width, pointY)); // top point
      tmpPoints.push(new WorkspacePoint(pointX, pointY + width)); // right point
      tmpPoints.push(new WorkspacePoint(pointX - width, pointY)); // bottom point
      tmpPoints.push(new WorkspacePoint(pointX, pointY - width)); // left point

      // Filter points outside of the workspace.
      tmpPoints = tmpPoints.filter(tmpPoint => !(
        tmpPoint.x < 0 || tmpPoint.x > this.workspace.currentMediaWidth ||
        tmpPoint.y < 0 || tmpPoint.y > this.workspace.currentMediaHeight
      ));

      if (tmpPoints.length > 0) {
        const randomPoint = tmpPoints[Math.floor(Math.random() * tmpPoints.length)];
        points.push(randomPoint);
      }
    }

    return points;
  }

  /**
   * Calculate the brush width.
   * This calculation is based on the method
   * `this.workspace.calculateStrokeWidth()`
   */
  private calculateBrushWidth(): number {
    return (this.strokeWidth / this.workspace.maxZoom) * (this.workspace.maxZoom - this.workspace.currentZoom);
  }

  private newPolyLine(svg: Element, startDrawingEvent: Event): Element {
    const polyLine = this.workspace.svgLayer.polyline([]).attr(svg.attr()) as unknown as Element;
    polyLine.stroke(this.strokeData).stroke({width: this.strokeWidth});
    polyLine.fill('none');
    (polyLine as unknown as SVGType.WithDraw).draw(startDrawingEvent);
    polyLine.on('mousemove', (ev) => {
      (polyLine as unknown as SVGType.WithDraw).draw(ev);
    });

    /*
     * Don't forget to remove the event listeners after drawing or on cancel.
     */
    ['drawdone', 'drawstop', 'drawcancel'].forEach((eventName: string) => {
      polyLine.on(eventName, () => {
        (polyLine as unknown as SVGType.WithOff).off();
      });
    });

    polyLine.show();
    return polyLine;
  }

  /**
   * Finish the current line and add it to the stack.
   */
  private finishCurrentLine(): void {
    if (this.currentLine === undefined) {
      return;
    }

    // Store the line in the stack.
    (this.currentLine as unknown as SVGType.WithDraw).draw('stop');
    (this.currentLine as unknown as SVGType.WithOff).off();
    this.polyLines.push(this.currentLine);
  }

  /**
   * Calculate the new stroke width depending on deltaY of the wheel event
   *
   * @param ev WheelEvent
   */
  private calculateNewStrokeWidth(ev: WheelEvent): number {
    const maxStrokeWidth = 50;
    const minStrokeWidth = 1;
    const delta = ev.deltaY < 0 ? -strokeDelta : +strokeDelta;
    const newStrokeWidth = this.strokeWidth + delta;
    return Math.max(minStrokeWidth, Math.min(maxStrokeWidth, newStrokeWidth));
  }
}
