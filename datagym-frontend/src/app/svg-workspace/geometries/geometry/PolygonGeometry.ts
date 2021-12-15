import {WorkspacePoint} from '../../model/WorkspacePoint';
import svgjs, {Circle, Element} from 'svg.js';
import {PolygonGeometryData} from '../geometry-data/PolygonGeometryData';
import {WorkspaceEventType} from '../../messaging/WorkspaceEventType';
import {PolyLikeGeometry} from './PolyLikeGeometry';
import {onDoubleEvent} from '../../model/utility/onDoubleEvent';
import {AiSegCalculate} from '../../model/AiSegCalculate';
import * as SVGType from '../../model/utility/SVGTypes';
import {BaseGeometryData} from '../geometry-data/BaseGeometryData';


export class PolygonGeometry extends PolyLikeGeometry {

  protected className = 'PolygonGeometry';

  /**
   * If the polygon was created via aiseg, store here the
   * selection geometry type so it may be possible to edit
   * that selection.
   */
  public aiSegCalculation: AiSegCalculate = undefined;

  public points: WorkspacePoint[] = [];
  public helpingLines: Array<svgjs.Line> = [];
  public inDrawingMode: boolean = false;
  private isResizing: boolean = false;
  private changedPoints: boolean = false;
  private oldPoints: WorkspacePoint[] = [];
  private static helpingPoint;

  /**
   * To simplify the polygon finishing, on the coordinates of the first polygon point a
   * <Circle> object is created.
   * This circle listens on the mouseover & mouseout events to visualise that the first
   * polygon point is selected. It also listens on the click event to finish the polygon.
   *
   * This helping point exists only on the svg layer and is not known on the workspace.
   * On the events 'drawcancel' or 'drawdone' of the polygon, this helping point is removed.
   */
  private firstPointOverlay: Circle = null;

  public syncPoints(newPoints: WorkspacePoint[]): void {
    this.points = newPoints;
    this.syncDataToSvg(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    this.svgObject = (svgLayer.polygon([]) as unknown as Element);
    this.createCircle();
  }

  protected registerCustomDrawEvent(): void {

    const onKeydown = (onkeydown: KeyboardEvent) => {
      if (onkeydown.key === 'Enter') {
        this.finishDrawing();
      }
    };

    const onContextMenu = () => {
      this.finishDrawing();
    };

    this.svgObject.on('drawstart', () => {
      this.inDrawingMode = true;
      /*
       * Listen once on context menu to finish the polygon on right click.
       */
      document.addEventListener('contextmenu', onContextMenu, {once: true});

      /*
       * Finish the polygon also with Enter pressed.
       */
      document.addEventListener('keydown', onKeydown);
      this.workspace.rescaleSvgElements();
    });

    this.svgObject.on('drawpoint', () => {
      // The polygon-points are created inside the draw-plugin but are on full-zoom way to big, so we rescale them
      this.workspace.rescaleSvgElements();

      const minRequiredPoints = 3;
      if (this.svgPointList.length > minRequiredPoints) {
        /*
         * If we have at least 3 points (re)create on every point
         * the firstPointOverlay circle object.
         *
         * Note: The firstPointOverlay must be created on every
         * point click elsewhere the first polygon point 'sits'
         * above the firstPointOverlay and the events may not emitted
         * as expected.
         */
        if (this.firstPointOverlay !== null) {
          this.firstPointOverlay.remove();
        }
        this.firstPointOverlay = this.createFirstPointOverlay();
      }
    });

    /*
     * Don't forget to remove the firstPointOverlay after drawing or on cancel.
     */
    ['drawdone', 'drawcancel'].forEach((eventName: string) => {
      this.svgObject.on(eventName, () => {
        document.removeEventListener('keydown', onKeydown);
        document.removeEventListener('contextmenu', onContextMenu);
        if (this.firstPointOverlay !== null) {
          this.firstPointOverlay.remove();
        }
      });
    });

    /*
     * Finish the polygon by double click.
     *
     * Because we are in drawing mode, we must listen on
     * .on('drawpoint') instead of .on('click').
     */
    onDoubleEvent(this.svgObject, 'drawpoint').subscribe(() => {
      const points: SVGPointList = this.svgPointList;

      const minLength = 4; // at least 3 points drawn + 'garbage' point
      if (points.length < minLength) {
        return;
      }

      /*
       * Accept a small radius around the first point
       * if the last point fits into the area of the first on
       * finish drawing and close the polygon.
       */
      const areaRadius = 3;
      /*
       * The *very* last point is somehow garbage. Use the next-to-last.
       * Note: points[points.length - 1] would be the last one, so -2 is right here!
       */
      const lastPointOffset = 2;
      const preLastPointOffset = 3;
      const lastPoint = points[points.length - lastPointOffset];
      const preLastPoint = points[points.length - preLastPointOffset];

      const xDiff = lastPoint.x - preLastPoint.x;
      const yDiff = lastPoint.y - preLastPoint.y;

      if (-areaRadius < xDiff && xDiff < areaRadius &&
        -areaRadius < yDiff && yDiff < areaRadius) {

        // The last point is a duplicate -> remove it.
        (this.svgObject as unknown as []).pop();

        /*
         * The last point fits into the area of the first one.
         */
        this.finishDrawing();
      }
    });
  }

  public beforeSelecting() {
    this.drawHelpingLines();
  }

  public afterSelecting() {
    this.registerDblClickToRemove();
  }

  public beforeUnselecting() {
    this.removeHelpingLines();
  }

  protected onDragStartEvent(event): void {
    this.removeHelpingLines();
  }

  protected onDragEndEvent(event): void {
    this.drawHelpingLines();
    this.reselect();
  }

  protected onResizeStartEvent(event): void {
    this.points.forEach(point => {
      this.oldPoints.push(new WorkspacePoint(point.x, point.y));
    });
    //This flag temporarily disables helping point generation while a resize event is ongoing
    this.isResizing = true;
  }

  protected onResizeDoneEvent(event): void {
    this.isResizing = false;
    let counter: number = 0;
    if (this.oldPoints.length > 0) {
      (this.workspace.getGeometryDataByIdentifier(this.identifier) as PolygonGeometryData).points.forEach(point => {
        if (point.x !== this.oldPoints[counter].x || point.y !== this.oldPoints[counter].y) {
          this.changedPoints = true;
        }
        counter++;
      });
    }
    if (this.changedPoints) {
      this.reselect();
      this.changedPoints = false;
    } else {
      //Allows for a double click,by not re-selecting in case that a polygon(line) point was actually clicked
      //and not dragged
      this.registerDblClickToRemove();
      this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
    }
    this.oldPoints = [];
  }

  /**
   * Remove all of the helping lines, that trigger a helping point on mousemove event
   */
  public removeHelpingLines(): void {
    this.helpingLines.forEach(line => {
      line.remove();
    });
    this.helpingLines = [];
  }

  /**
   * Create the new circle.
   *
   * @private
   */
  private createCircle(){
    if(!!PolygonGeometry.helpingPoint){
      return;
    }
    const circleSize = 7;
    const strokeFactor = 3;
    const circle = this.workspace.svgLayer.circle(circleSize);
    circle.addClass('svg_select_points');
    circle.addClass('helpingpoint');
    circle.stroke({width: this.workspace.calculateStrokeWidth() * strokeFactor, color: '#f1c609'});
    circle.fill('#000000');
    circle.radius(this.workspace.calculateSelectCircleRadius());
    PolygonGeometry.helpingPoint = circle;
    circle.hide();
  }

  /**
   * Draw helping points for polygons and polylines.
   * Helping lines are drawn, that trigger a mousemove event, upon which
   * a new helping point is drawn. That way the use can use edge hovering to
   * create a new helping point. Polylines have one less helping line than polygons,
   * since the line between start and end point is missing.
   */
  public drawHelpingLines(): void {
    if (this.inDrawingMode) {
      return;
    }

    // array like: [ [p1, p2], [p2, p3], ... ]
    const helpingLinePoints = (this.svgObject as unknown as SVGType.WithGetHelpingLinePoints).getHelpingLinePoints();
    helpingLinePoints.forEach(([startPoint, endPoint], i) => {
      const line = this.workspace.svgLayer.line(startPoint[0], startPoint[1], endPoint[0], endPoint[1]);
      line.fill('none');
      line.style('cursor: copy');
      line.stroke({ color: '#f06', width: 1, linecap: 'round', linejoin: 'round' });
      line.opacity(0.0);
      line.on('click', (click) => {
        // "Convert" helping point to polygon-point BUT be aware of the adding-position in the array! =)
        (this.svgObject as unknown as SVGType.insertPoint).insertPoint(i + 1, [click.offsetX, click.offsetY]);
        this.reselect();
        this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
      });
      this.helpingLines.push(line);
    });
  }

  public startDrawing(): void {
    // Hide data definition because the svg-plugins sadly doesnt have type definitions
    (this.svgObject as unknown as SVGType.WithDraw).draw();
  }

  public getGeometryData(): BaseGeometryData {
    this.syncSvgToData(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
    return new PolygonGeometryData(this.points, this.comment);
  }

  public setGeometryData(data: BaseGeometryData) {
    this.points = (data as PolygonGeometryData).points;
    this.syncDataToSvg(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
  }

  /**
   * Finish drawing
   */
  protected finishDrawing(): void {
    if (this.inDrawingMode === true) {
      // Hide data definition because the svg-plugins sadly doesnt have type definitions
      (this.svgObject as unknown as SVGType.WithDraw).draw('done');
      (this.svgObject as unknown as SVGType.WithOff).off('drawstart');
      if (this.firstPointOverlay !== null) {
        this.firstPointOverlay.remove();
        this.firstPointOverlay = null;
      }
    }
  }

  private reselect(): void {
    this.workspace.unselectGeometry(this);
    this.workspace.selectGeometry(this);
  }

  /**
   * Removes a polygon point after double clicking it
   */
  private registerDblClickToRemove(): void {
    // Hookup into the svg.select.js plugin to register the double click event
    const selectHandler = this.svgObject.remember('_selectHandler');
    if (selectHandler !== undefined) {
      // Get the svg internal polygon array

      selectHandler.pointSelection.set.each(el => {
        /**
         * Todo: Use `svg.on('dblclick')` instead!
         */
        // Sadly the double-click events doesnt work, so we need our custom implementation of double-click
        onDoubleEvent(selectHandler.pointSelection.set.members[el]).subscribe((point) => {

          const minRequiredPoints = 3;
          if ((this.svgObject as unknown as SVGType.count).count() <= minRequiredPoints) {
            return;
          }

          // We need to use relative points, because the panning and zooming has impact the absolute ones
          const bbox = (this.svgObject as unknown as SVGType.WithArray).array().bbox();

          // Shorten the float value due matching issues
          const clickedPointX = point.target.cx.baseVal.value.toFixed(1);
          const clickedPointY = point.target.cy.baseVal.value.toFixed(1);

          (this.svgObject as unknown as SVGType.filterPoints).filterPoints(p => {
            const relativePointX = (p[0] - bbox.x).toFixed(1);
            const relativePointY = (p[1] - bbox.y).toFixed(1);
            return !(relativePointX === clickedPointX && relativePointY === clickedPointY);
          });

          this.reselect();
          this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
        });
      });
    }
  }

  /**
   * Create a new circle object on top of the first polygon point to
   * access some events on that position.
   *
   * Register then some event listeners on 'mouseover' & 'mouseout'
   * for a visual feedback and on 'click' to finish drawing.
   */
  private createFirstPointOverlay(): Circle {

    const startPoint: SVGPoint = this.svgPointList[0];
    const circle = this.workspace.svgLayer.circle(1);
    circle.stroke({
      width: this.workspace.calculateStrokeWidth(),
      color: this.geometryProperties.borderColor
    });
    circle.fill({
      color: this.geometryProperties.fillColor,
      opacity: this.geometryProperties.fillOpacity
    });
    circle.radius(this.workspace.calculateSelectCircleRadius());
    circle.center(startPoint.x, startPoint.y);

    /*
     * register some event listeners
     */
    circle.on('click', (e: Event) => {
      this.finishDrawing();
      e.stopPropagation();
    });
    circle.on('mouseover', () => {
      circle.fill({opacity: 1});
    });
    circle.on('mouseout', () => {
      circle.fill({opacity: this.geometryProperties.fillOpacity});
    });

    return circle;
  }
}
