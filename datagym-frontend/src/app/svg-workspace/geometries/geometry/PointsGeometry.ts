import {BaseGeometryData} from '../geometry-data/BaseGeometryData';
import svgjs, {Element} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import {PolyLikeGeometry} from './PolyLikeGeometry';
import {onDoubleEvent} from '../../model/utility/onDoubleEvent';
import {AiSegCalculate, DEFAULT_NUM_POINTS} from '../../model/AiSegCalculate';


/**
 * This geometry type is not known within the backend.
 * It's a implementation detail used to simplify AISeg usage
 * by selecting some points within the area of interest and
 * some outside it.
 */
export class PointsGeometry extends PolyLikeGeometry {

  protected readonly className = 'PointsGeometry';

  // This flag should be public, because aiseg polygon uses this flag.
  public inDrawingMode: boolean = false;

  public readonly positivePointsColor = '#66FF33';
  public readonly negativePointsColor = '#FB110D';

  private positiveElements: Element[] = [];
  private negativeElements: Element[] = [];

  private positivePoints: WorkspacePoint[] = [];
  private negativePoints: WorkspacePoint[] = [];

  /**
   * @override
   */
  protected get comment() : string{
    const errorMessage = `comment is not supported on ${ this.className }`;
    throw new Error(errorMessage);
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
    throw new Error('syncDataToSvg is not supported on PointsGeometry');
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

    const synchronizeStack = (stack: Element[]): WorkspacePoint[] => {
      const newStack = [];
      stack.forEach(point => {
        newStack.push(new WorkspacePoint(
          point.x() * conversionHeight,
          point.y() * conversionWidth
        ));
      });
      return newStack;
    };

    this.positivePoints = synchronizeStack(this.positiveElements);
    this.negativePoints = synchronizeStack(this.negativeElements);
  }

  public createAiSegCalculationObject(currentImageId: string): AiSegCalculate;
  public createAiSegCalculationObject(currentImageId: string, numPoints: number = DEFAULT_NUM_POINTS): AiSegCalculate {
    const aisegCalc = new AiSegCalculate();

    aisegCalc.positivePoints = this.positivePoints;
    aisegCalc.negativePoints = this.negativePoints;
    aisegCalc.numPoints = numPoints;
    aisegCalc.imageId = currentImageId;

    return aisegCalc;
  }

  afterSelecting() {
  }

  beforeSelecting() {
  }

  beforeUnselecting() {
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    // We use the svg object only as event manager / emitter.
    const size: number = 5;
    this.svgObject = (svgLayer.circle(size) as unknown as Element);
    this.svgObject.hide();
  }

  getGeometryData(): BaseGeometryData {
    throw new Error('setGeometryData is not supported on PointsGeometry');
  }

  public setGeometryData(data: BaseGeometryData): void {
    throw new Error('setGeometryData is not supported on PointsGeometry');
  }

  protected onBeforeDragEvent(event): void {
  }

  protected onDragStartEvent(event): void {
  }

  protected onDragEndEvent(event): void {
  }

  protected onResizeDoneEvent(event): void {
  }

  protected onResizeStartEvent(event): void {
  }

  protected onDrawStopEvent(event): void {
    this.positiveElements.forEach(point => {
      this.positivePoints.push(new WorkspacePoint(point.x(), point.y()));
      point.hide();
    });
    this.negativeElements.forEach(point => {
      this.negativePoints.push(new WorkspacePoint(point.x(), point.y()));
      point.hide();
    });
  }

  protected registerCustomDrawEvent(): void {

    const onKeydown = (onkeydown: KeyboardEvent) => {
      if (onkeydown.key === 'Enter') {
        this.finishDrawing();
      }
    };

    // Right button
    const onContextMenu = (contextMenu: MouseEvent) => {
      this.inDrawingMode = true;
      const circle = this.createPoint(contextMenu, this.negativePointsColor);
      this.negativeElements.push(circle);
      this.finishDrawing();
    };

    const onMouseUp = (ev: MouseEvent) => {
      // Left button (primary)
      if (ev.button !== 0) {
        return;
      }
      if (ev.target === this.workspace.svgLayer.node ||
        (ev.target as any).parentElement === this.workspace.svgLayer.node) {
        this.inDrawingMode = true;
        const circle = this.createPoint(ev, this.positivePointsColor);
        this.positiveElements.push(circle);
        this.finishDrawing();
      }
    };

    // Listen once on context menu to finish the polyline.
    document.addEventListener('contextmenu', onContextMenu);
    // Finish the polygon also with Enter pressed.
    document.addEventListener('keydown', onKeydown);
    // Let the mouse wheel change the stroke width
    document.addEventListener('mouseup', onMouseUp);

    /*
     * Don't forget to remove the event listeners after drawing or on cancel.
     */
    ['drawdone', 'drawstop', 'drawcancel'].forEach((eventName: string) => {
      this.svgObject.on(eventName, () => {
        document.removeEventListener('keydown', onKeydown);
        document.removeEventListener('contextmenu', onContextMenu);
        document.removeEventListener('mouseup', onMouseUp);

        this.positiveElements.forEach(positiveElement => positiveElement.hide());
        this.negativeElements.forEach(negativeElement => negativeElement.hide());
      });
    });
  }

  public startDrawing(): void {
    (this.svgObject as unknown as SVGType.WithDraw).draw();
  }

  public toDataString(): string {
    return `PointsGeometry (
    id: ${this.svgObject.id()},
    positivePoints: ${JSON.stringify(this.positivePoints)},
    negativePoints: ${JSON.stringify(this.negativePoints)},
    )`;
  }

  /**
   * Create a new point to push to the stack.
   *
   * @param event
   * @param color
   */
  private createPoint(event: Event, color: string): Element {
    const defaultSize =  this.workspace.calculateSelectCircleRadius();
    const circle = this.workspace.svgLayer.circle(defaultSize) as unknown as Element;
    circle.stroke({
      width: this.workspace.calculateStrokeWidth(),
      color
    });
    circle.fill({
      opacity: this.geometryProperties.fillOpacity,
      color
    });
    (circle as unknown as SVGType.WithDraw).draw(event);
    (circle as unknown as SVGType.WithDraw).draw('stop');

    circle.on('mouseover', () => {
      const factor = 2;
      (circle as unknown as SVGType.WithRadius).radius(factor * this.workspace.calculateSelectCircleRadius());
      circle.fill({opacity: 0.7});
    });
    circle.on('mouseout', () => {
      (circle as unknown as SVGType.WithRadius).radius(this.workspace.calculateSelectCircleRadius());
      circle.fill({opacity: this.geometryProperties.fillOpacity});
    });

    circle.on('mouseup', (e: Event) => {
      e.stopPropagation();
    });

    onDoubleEvent(circle, 'mouseup').subscribe((e: Event) => {
      e.stopPropagation();

      this.positiveElements = this.positiveElements.filter(positiveElement => positiveElement !== circle);
      this.negativeElements = this.negativeElements.filter(negativeElement => negativeElement !== circle);

      circle.remove();
    });

    (circle as unknown as SVGType.WithRadius).radius(this.workspace.calculateSelectCircleRadius());
    circle.fill({opacity: this.geometryProperties.fillOpacity});

    return circle;
  }
}
