import {GeometryProperties} from './GeometryProperties';
import {GeometryType} from './GeometryType';
import {Container, Element} from 'svg.js';
import {WorkspaceInternalService} from '../service/workspace-internal.service';
import {WorkspaceEvent} from '../messaging/WorkspaceEvent';
import {WorkspaceEventType} from '../messaging/WorkspaceEventType';
import {BaseGeometryData} from './geometry-data/BaseGeometryData';
import {Point} from '../../label-mode/model/geometry/Point';
import {AiSegCalculate} from '../model/AiSegCalculate';
import {WorkspaceListEvent} from '../messaging/WorkspaceListEvent';
import {WithDraggable, WithGetScreenCTM, WithResize} from '../model/utility/SVGTypes';
import {CommentDecorator} from './decorator/CommentDecorator';
import {NoCommentDecorator} from './decorator/NoCommentDecorator';
import {WithCommentDecorator} from './decorator/WithCommentDecorator';
import {LabelModeType} from '../../label-mode/model/import';


export abstract class BaseGeometry {
  public readonly geometryProperties: GeometryProperties;

  public svgObject: Element;

  protected commentDecorator: CommentDecorator = new NoCommentDecorator();

  /*
      If this parameter is false, there will be no message-bus events emitted.
      It was introduced for the AiSeg-functionality to draw a rectangle which is not registered
      in the valueStack of the Label-Mode.
   */
  public sendMessageBusEvents: boolean = true;

  /**
   * Let the geometry type itself decide, which properties
   * should go into the AiSegCalculate TO.
   *
   * @param currentImageId
   * @param numPoints: number optional, default should be 40;
   */
  public abstract createAiSegCalculationObject(currentImageId: string, numPoints?: number): AiSegCalculate;

  /**
   * Info: To be able to use the svg.js plugins we need to create the geometries above
   * the SVG-Container; otherwise the Plugins can not recognize the elements
   * TL;DR: Dont create svg-Elements with 'new Rect()'
   * @param svgLayer
   */
  protected abstract createSvgObject(svgLayer: Container): void;

  /**
   * Sets all geometry specific parameter with the svg-object (like x,y and positions)
   */
  public abstract syncDataToSvg(): void;
  public abstract syncDataToSvg(conversionHeight: number, conversionWidth: number): void;

  /**
   * Synchronizes the svg object with the data object
   */
  public abstract syncSvgToData(): void;
  public abstract syncSvgToData(conversionHeight: number, conversionWidth: number): void;

  /**
   * With this method you can attach custom register events. Reason: Different shapes/geometries
   * belong to different update behaviours / callbacks
   */
  protected abstract registerCustomDrawEvent(): void;

  /**
   * Register drawstop-event from 'svg.draw.js'-Plugin
   */
  protected abstract onDrawStopEvent(event): void;

  /**
   * Register beforedrag-event from 'svg.draggable.js'-Plugin
   */
  protected abstract onBeforeDragEvent(event): void;

  /**
   * Register dragstart-event from 'svg.draggable.js'-Plugin
   */
  protected abstract onDragStartEvent(event): void;

  /**
   * Register dragend-event from 'svg.draggable.js'-Plugin
   */
  protected abstract onDragEndEvent(event): void;

  /**
   * Register resizestart-event from 'svg.resize.js'-Plugin
   */
  protected abstract onResizeStartEvent(event): void;

  /**
   * Register dragend-event from 'svg.resize.js'-Plugin
   */
  protected abstract onResizeDoneEvent(event): void;

  /**
   * Gets fired before the element gets selected
   */
  public abstract beforeSelecting();

  /**
   * Gets fired after the element gets selected
   */
  public abstract afterSelecting();

  /**
   * Gets fired before the element gets unselected
   */
  public abstract beforeUnselecting();

  /**
   * Should print the raw data of an geometry of the LAST SYNC!
   * - If no sync was executed the values are empty
   */
  public abstract toDataString(): string;

  /**
   * Returns the data of an individual geometry
   */
  public abstract getGeometryData(): BaseGeometryData;

  /**
   * Sets the data of an individual geometry
   * @param data
   */
  public abstract setGeometryData(data: BaseGeometryData);

  public abstract startDrawing(): void;

  protected get comment() : string{
    return this.geometryProperties.comment;
  }

  protected get identifier(): string {
    return this.geometryProperties.identifier;
  }

  /**
   * The svg plugin creates for each svg object native svg-id.
   * This id is emitted via the svg events.
   */
  public get svgId(): string {
    return this.svgObject !== undefined
      ? this.svgObject.id()
      : '';
  }

  /**
   * In some situations the svg object may not be set.
   */
  public get hasSvgObject(): boolean {
    return this.svgObject !== undefined;
  }

  /**
   * Create a new workspace geometry.
   * Pass either the GeometryProperties object or the id as string to the ctr.
   * If no properties argument is passed a random uuid would be created.
   *
   * @param workspace
   * @param properties
   */
  constructor(protected workspace: WorkspaceInternalService, properties: GeometryProperties) {
    this.geometryProperties = properties;
    this.createSvgObject(this.workspace.svgLayer);

    /*
     * For more options see the svg.resize.js plugin for svg.js
     */
    const constraintBoxResize = {
      constraint: this.workspace.constraint_box,
      snapToGrid: 0.1
    };

    (this.svgObject as unknown as WithResize).resize(constraintBoxResize);
    this.registerDrawEvents();
    this.setSvgProperties();
    this.workspace.currentGeometries.push(this);
  }

  public get geometryType(): GeometryType {
    return this.geometryProperties.geometryType;
  }

  /**
   * Remove this svg object from svg layer.
   * Override if some other svg objects may be
   * created during the runtime.
   */
  public deleteFromSVGLayer(): void {
    // Remove object from svg layer
    if (this.svgObject !== undefined) {
      this.svgObject.remove();
    }
    this.commentDecorator.remove();
  }

  /**
   * Set a lower opacity to the background and change the border to a dashed one.
   */
  public suppress(): void {
    this.svgObject.stroke({dasharray: '5,5'});
    this.svgObject.fill({opacity: 0.1});
  }

  /**
   * Restore the default opacity value and remove the dashed border.
   */
  public stopSuppressing(): void {
    this.svgObject.stroke({dasharray: 'none'});
    this.svgObject.fill({opacity: 0.2});
  }

  public show(): void {
    if (this.svgObject !== undefined) {
      this.svgObject.show();
    }
    this.commentDecorator.show();
  }

  public hide(): void {
    if (this.svgObject !== undefined) {
      this.svgObject.hide();
    }
    this.commentDecorator.hide();
  }

  /**
   * Note: this method doesn't care about
   * state changes like 'show' from outside.
   */
  public highlight(): void {
    const ms = 300;
    Array.from({length: 6}).forEach((_, i) => {
      setTimeout(() => {
        if (!/*not*/!!this.svgObject) {
          return;
        }
        this.svgObject.visible()
          ? this.hide()
          : this.show();
      }, i * ms);
    });
  }

  /**
   * Add a commentDecorator depending on the comment.
   *
   * @param comment
   */
  public addCommentDecorator(comment: string): void {
    this.geometryProperties._comment = comment;

    this.commentDecorator.remove();
    if (!/*not*/!!comment) {
      this.commentDecorator = new NoCommentDecorator();
      return;
    }

    const position = this.getCommentDecoratorPosition();
    this.commentDecorator = this.workspace.mediaType === LabelModeType.VIDEO
      ? new WithCommentDecorator(this.workspace.svgLayer, position, (e: Element) => this.setSvgProperties(e))
      : new NoCommentDecorator();
  }

  /**
   * Make the geometry draggable within the svg area.
   *
   * This should be called once for the geometry if it gets selected to let
   * the user drag it within the constraint_box.
   */
  public makeDraggable(): void {
    (this.svgObject as unknown as WithDraggable).draggable(this.workspace.constraint_box);
  }

  /**
   * Remove the geometry draggability.
   *
   * This should be called if the geometry gets unselected or if more than one geometry
   * is selected to not allow dragging any of them.
   */
  public removeDraggability(): void {
    (this.svgObject as unknown as WithDraggable).draggable(false);
  }

  /**
   * Check if the current Geometry is outside the Image Area
   */
  public outOfBounds(): boolean {
    const points: Point[] = this.getCornerPoints();
    for (const point of points) {
      if (point.x > this.workspace.currentMediaWidth || point.x < 0 ||
        point.y > this.workspace.currentMediaHeight || point.y < 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Bind some properties to the svg object itself so they can be
   * accessed from the svg object itself. They may be used to filter
   * all geometries listed within the workspace svg layer.
   */
  protected bindData(): void {
    this.svgObject.data('type', this.geometryType);
    this.svgObject.data('identifier', this.geometryProperties.identifier);
  }

  /**
   * Throw an error if the svg object could not be created.
   * Extracted from `registerDrawEvents()` to prevent undefined
   * errors.
   */
  protected ensureSvgObject(): void {
    if (this.svgObject === undefined) {
      const identifier = this.geometryProperties.identifier;
      const type = GeometryType[this.geometryType].toString();
      throw new Error(`'Could not create geometry ${ type } with id ${ identifier }`);
    }
  }

  /**
   * Short hand for triggering the workspace event bus.
   *
   * @param type: WorkspaceEventType
   * @param payload: any
   */
  protected nextWorkspaceEvent(type: WorkspaceEventType, payload = undefined): void {
    if (this.sendMessageBusEvents) {
      this.workspace.internalWorkspaceEventBus.next(new WorkspaceEvent(this.geometryProperties.identifier, type, payload));
    }
  }

  /**
   * Short hand for triggering the workspace event bus.
   *
   * @param type: WorkspaceEventType
   * @param geometryIds: the id's to delete
   * @param payload: any
   */
  protected nextWorkspaceListEvent(type: WorkspaceEventType, geometryIds: string[], payload = undefined): void {
    geometryIds = geometryIds.filter(geometryId => !!geometryId);
    if (this.sendMessageBusEvents && geometryIds.length > 0) {
      this.workspace.internalWorkspaceEventBus.next(new WorkspaceListEvent(geometryIds, type, payload));
    }
  }

  /**
   * Listen to svg plugin-events to update the specific positions/svg-properties
   */
  protected registerDrawEvents() {
    this.svgObject.click(cl => {
      // Finds a base geometry by its native id
      const foundGeometry = this.workspace.currentGeometries.find(geo => geo.svgObject.id() === cl.target.id);
      if (this.workspace.selectedGeometries.indexOf(foundGeometry) === -1) {
        // Check for multi-select functionality
        if (!cl.ctrlKey) {
          this.workspace.unselectAllGeometries(this.workspace.selectedGeometries.length === 1);
        }
        // Avoid "wrong" clicks if the user is drawing a polygon
        if(!this.workspace.isUserDrawing){
          this.workspace.selectGeometry(foundGeometry);
        }
      }
    });
    this.svgObject.on('beforedrag.namespace', cl => {
      // Allow object moving if shift key is pressed
      if (!cl.detail.event.shiftKey || this.workspace.isUserDrawing) {
        cl.preventDefault();
      }
      this.onBeforeDragEvent(cl);
    });
    this.svgObject.on('drawstop', event => {
      this.onDrawStopEvent(event);
      this.workspace.disableDrawingMode();
      this.workspace.selectGeometry(this);
      this.nextWorkspaceEvent(WorkspaceEventType.DRAW_FINISHED);
      this.workspace.currentGeometries.forEach(geo => geo.stopSuppressing());
    });
    this.svgObject.on('dragstart.namespace', event => {
      this.commentDecorator.hide();
      this.onDragStartEvent(event);
    });
    this.svgObject.on('dragend.namespace', event => {
      this.onDragEndEvent(event);
      const position = this.getCommentDecoratorPosition();
      this.commentDecorator.move(position);
      this.commentDecorator.show();
      this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
    });
    this.svgObject.on('resizestart', event => {
      this.commentDecorator.hide();
      this.onResizeStartEvent(event);
    });

    this.svgObject.on('resizedone', event => {
      this.commentDecorator.show();
      this.onResizeDoneEvent(event);
      this.nextWorkspaceEvent(WorkspaceEventType.DATA_UPDATED);
    });

    this.svgObject.on('contextmenu', event => {
      this.workspace.onGeometryContextMenu(event, this);
    });
    this.registerCustomDrawEvent();
  }

  /**
   * Set border and fill properties
   */
  protected setSvgProperties(element: unknown = this.svgObject): void {
    this.setSvgBorderProperties(element);
    this.setSvgFillProperties(element);
  }

  protected setSvgBorderProperties(element: unknown = this.svgObject): void {
    if (this.geometryProperties.border) {
      (element as Element).stroke({
        width: this.workspace.calculateStrokeWidth(),
        color: this.geometryProperties.borderColor
      });
    }
  }

  protected setSvgFillProperties(element: unknown = this.svgObject): void {
    if (this.geometryProperties.fill) {
      (element as Element).fill({
        color: this.geometryProperties.fillColor,
        opacity: this.geometryProperties.fillOpacity
      });
    }
  }

  /**
   * Syncs EVERY PARAMETER with the svg-object, is useful after creating a new
   */
  public syncSvgObject(conversionHeight: number, conversionWidth: number): void;
  public syncSvgObject(
    conversionHeight: number = this.workspace.resizeOffsetHeight,
    conversionWidth: number = this.workspace.resizeOffsetWidth
  ): void {
    this.syncDataToSvg(conversionHeight, conversionWidth);
    this.setSvgProperties();
  }

  /**
   * Update the screen matrix (for the svg plugins) for zooming while drawing/resizing
   */
  public updateZoomState(): void {
    if (this.svgObject !== undefined && this.svgObject.remember !== undefined) {
      const paintHandler = this.svgObject.remember('_paintHandler');
      if (paintHandler !== undefined) {
        paintHandler.m = (this.svgObject.node as unknown as WithGetScreenCTM).getScreenCTM().inverse();
      }
      const resizeHandler = this.svgObject.remember('_resizeHandler');
      if (resizeHandler !== undefined) {
        resizeHandler.m = (this.svgObject.node as unknown as WithGetScreenCTM).getScreenCTM().inverse();
      }
    }
  }

  /**
   * Get Corner Points of the current BoundingBox around the SVG Object
   */
  public getCornerPoints(): Point[] {
    const bbox = this.svgObject.bbox();

    // Math.floor to round down pixel coordinates. Required because bbox adds tiny float numbers due to stroke-width
    const p0 = new Point(Math.floor(bbox['x']), Math.floor(bbox['y']));
    const p1 = new Point(Math.floor(bbox['x']), Math.floor(bbox['y'] + bbox['h']));
    const p2 = new Point(Math.floor(bbox['x'] + bbox['w']), Math.floor(bbox['y']));
    const p3 = new Point(Math.floor(bbox['x'] + bbox['w']), Math.floor(bbox['y'] + bbox['h']));

    return [p0, p1, p2, p3];
  }

  /**
   * Get the position for the comment icon.
   * Default: the top right corner.
   *
   * Here without corner checks using the bounding box.
   *
   * Override this method to change the position of the comment box.
   *
   * @protected
   */
  protected getCommentDecoratorPosition(): Point {

    const width = 16; // px
    const height = 16; // px
    const space = 5;

    const blIndex = 1;
    const trIndex = 2;
    const cornerPoints = this.getCornerPoints();
    const bl = cornerPoints[blIndex];
    const tr = cornerPoints[trIndex];

    const maxSize = new Point(
      this.workspace.currentMediaWidth,
      this.workspace.currentMediaHeight
    );

    return new Point(
      Math.max(...[bl.x - width - space, tr.x + space].filter(x => x < maxSize.x - width)),
      Math.min(...[tr.y - height - space, bl.y - height - space].filter(y => y > 0))
    );
  }
}
