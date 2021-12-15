import {LineGeometryData} from '../geometry-data/LineGeometryData';
import svgjs, {Element, PointArray} from 'svg.js';
import * as SVGType from '../../model/utility/SVGTypes';
import {BaseGeometryData} from '../geometry-data/BaseGeometryData';
import {onDoubleEvent} from '../../model/utility/onDoubleEvent';
import {PolygonGeometry} from './PolygonGeometry';


/**
 * Since Sprint #17 the line geometry uses also a polyline
 * as drawable geometry. Don't confuse this line (is known
 * within the backend as line geometry) and the polyline.
 * (Is not known within the backend and is only used as
 * AI selection tool.)
 */
export class LineGeometry extends PolygonGeometry {

  protected readonly className = 'LineGeometry';

  protected setSvgProperties(): void {
    if (this.geometryProperties.border) {
      this.svgObject.stroke({
        width: this.workspace.calculateStrokeWidth(),
        color: this.geometryProperties.borderColor
      });
    }

    this.svgObject.fill('none');
  }

  protected createSvgObject(svgLayer: svgjs.Container): void {
    this.svgObject = (svgLayer.polyline([]) as unknown as Element);
  }

  protected registerCustomDrawEvent(): void {

  }

  public startDrawing(): void {

    // Finish the line also with Enter pressed.
    const onKeydown = (onkeydown: KeyboardEvent) => {
      if (onkeydown.key === 'Enter') {
        this.finishDrawing();
      }
    };

    const mouseDown = (ev: MouseEvent) => {
      if (ev.button !== 0) {
        return;
      }

      (this.svgObject as unknown as SVGType.WithDraw).draw();
    };
    document.addEventListener('mousedown', mouseDown);

    this.svgObject.on('drawstart', () => {
      this.inDrawingMode = true;

      // Listen once on context menu to finish the polyline.
      document.addEventListener('contextmenu', () => {
        this.finishDrawing();
      }, {once: true});

      document.addEventListener('keydown', onKeydown);
    });

    onDoubleEvent(this.svgObject, {
      eventName: 'drawpoint',
      finish: true
    }).subscribe(() => {
      const points: SVGPointList = this.svgPointList;

      const minLength = 2;
      if (points.length < minLength) {
        return;
      }

      this.finishDrawing();
    });

    /*
     * Don't forget to remove the event listeners after drawing or on cancel.
     */
    ['drawdone', 'drawstop', 'drawcancel'].forEach((eventName: string) => {
      this.svgObject.on(eventName, () => {
        document.removeEventListener('keydown', onKeydown);
        document.removeEventListener('mousedown', mouseDown);
      });
    });

  }

  public getGeometryData(): BaseGeometryData {
    this.syncSvgToData(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
    return new LineGeometryData([...this.points], this.comment);
  }

  public setGeometryData(data: LineGeometryData) {
    this.points = [...data.points];
    this.syncDataToSvg(this.workspace.resizeOffsetHeight, this.workspace.resizeOffsetWidth);
  }

  /**
   * Get here the the list of all points that should have an
   * helping point between them.
   */
  protected getPointArrayForHelpingPoints(): PointArray {
    return (this.svgObject as unknown as SVGType.WithArray).array();
  }

}
