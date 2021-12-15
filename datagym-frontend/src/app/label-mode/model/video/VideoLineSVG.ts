import {Subject} from 'rxjs';
import {VideoBusEvent} from './VideoBusEvent';
import {LineObject} from './LineObject';
import {LcEntryGeometryValue} from '../geometry/LcEntryGeometryValue';
import {SVGObject} from './SVGObject';
import {VideoLineData} from './VideoLineData';
import {Point} from '../geometry/Point';


export class VideoLineSVG {

  public node: Element = SVGObject.create('svg', {height: '1em'});

  public get id(): string {
    return this.lineData.id;
  }

  /**
   * If the frame changes, just update the x position to change the position.
   *
   * @param newPosition
   */
  public set x(newPosition: number) {
    this.currentX = newPosition;
    const newX = (this.lineData.min - newPosition) * this.percentagePerFrame;
    this.node.setAttributeNS(null, 'x', `${newX}%`);
  }

  /**
   * If the frame index changes, just update the y position to change the line position.
   *
   * @param newIndex
   */
  public set y(newIndex: number) {
    if (this.currentY === newIndex) {
      return;
    }
    this.currentY = newIndex;
    const y = VideoLineSVG.calculateLineYOffset(newIndex);
    this.node.setAttributeNS(null, 'y', y);
  }

  /**
   * Just a shorthand to set the position.
   * @param point
   */
  public set position(point: Point) {
    this.x = point.x;
    this.y = point.y;
  }

  /**
   * Stacked the frames into chunks.
   * E.g. each junk starts with an START and ends with an END change object
   * or contains only one START_CHANGE object.
   *
   * Each change object is then mapped to it's frame number.
   */
  public get chunks(): number[][] {
    return this.lineData.chunks;
  }

  private currentX: number = undefined;
  private currentY: number = undefined;
  private lineObject: LineObject;
  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  private lineData: VideoLineData;

  constructor(
    public value: LcEntryGeometryValue,
    public readonly index: number,
    private readonly percentagePerFrame: number,
    private readonly bus: Subject<VideoBusEvent>
  ) {
    this.init(new VideoLineData(value, this.percentagePerFrame));
  }

  /**
   * Move the line to the given position.
   * @param position
   */
  public moveTo(position: Point): void {
    this.position = position;
  }

  public redraw(value: LcEntryGeometryValue): void;
  public redraw(value: LcEntryGeometryValue, position: Point): void;
  public redraw(value: LcEntryGeometryValue, xPosition: number): void;
  public redraw(value: LcEntryGeometryValue, p: number|Point = undefined): void {
    this.teardown(false);

    this.value = value;
    this.init(new VideoLineData(value, this.percentagePerFrame));

    if (p !== undefined) {
      if (typeof p === 'object' && p instanceof Point) {
        this.moveTo(p);
      }
      if (typeof p === 'number') {
        this.x = p;
      }
    }
  }

  private init(lineData: VideoLineData): void {
    this.lineData = lineData;
    this.lineObject = new LineObject(lineData, this.index, this.node, this.bus);
  }

  /**
   * Before deleting this object / removing from view window
   * call this `teardown` method to prevent memory leaks and open
   * subscriptions.
   *
   * Note: when used with deleted = true (the default case) this VideoLineSVG
   * cannot be used anymore until the `node` property is re-added to the svgLineLayer.
   *
   * @param deleted when the value line is deleted remove also it's svg node.
   */
  public teardown(deleted: boolean = true): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
    this.unsubscribe = new Subject<void>();
    this.nodeRemove(deleted);
  }

  /**
   * Remove all children of the svg container.
   *
   * @private
   */
  private nodeRemove(removeNode: boolean): void {
    while (this.node.firstChild) {
      this.node.lastChild.remove();
    }
    if (!!removeNode) {
      this.node.remove();
    }
  }

  public hide(): void {
    this.lineObject.hide();
  }

  public show(): void {
    this.lineObject.show();
  }

  /**
   * Calculate the y offset of the current value line.
   * The offset is always 0.5 em to the top line and hte
   * @param index
   * @private
   */
  public static calculateLineYOffset(index: number): string {
    const valueLineYOffset = 0.5;
    const valueLineIndexYOffset = 1.5;

    return `${ (index * valueLineIndexYOffset) + valueLineYOffset}em`;
  }
}
