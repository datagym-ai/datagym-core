import {EventEmitter, Injectable} from '@angular/core';
import {WorkspaceUtilityService} from './workspace-utility.service';
import {ScaledImageDimensions} from '../model/utility/ScaledImageDimensions';
import {BaseGeometry} from '../geometries/BaseGeometry';
import {GeometryType} from '../geometries/GeometryType';
import {Container, Element} from 'svg.js';
import {PolygonGeometry} from '../geometries/geometry/PolygonGeometry';
import {WorkspacePoint} from '../model/WorkspacePoint';
import {BehaviorSubject, ReplaySubject} from 'rxjs';
import {AisegApiService} from './aiseg-api.service';
import {NgxSmartModalService} from 'ngx-smart-modal';
import {WorkspaceEvent} from '../messaging/WorkspaceEvent';
import {WorkspaceEventType} from '../messaging/WorkspaceEventType';
import {BaseGeometryData} from '../geometries/geometry-data/BaseGeometryData';
import {WorkspaceListEvent} from '../messaging/WorkspaceListEvent';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {MediaStyleFilterService} from './media-style-filter.service';
import {ContextMenuService} from './context-menu.service';
import {WithDraw, WithRadius, WithSelectize} from '../model/utility/SVGTypes';
import {ContextMenuEvent} from '../messaging/ContextMenuEvent';
import {ContextMenuEventType} from '../messaging/ContextMenuEventType';
import {GeometryProperties} from '../geometries/GeometryProperties';
import {GeometryConfiguration} from '../model/GeometryConfiguration';
import {GeometryFactory} from '../geometries/GeometryFactory';
import {LabelModeType} from '../../label-mode/model/import';


declare const SVG: any;

@Injectable({
  providedIn: 'root'
})
export class WorkspaceInternalService {

  /**
   * A lightweight flatted version of the `LcEntryGeometry`
   * holding just the information necessary for the workspace.
   *
   * This information are used to create the geometry properties and
   * have be set before the first geometry is created
   */
  public configuration: GeometryConfiguration[] = [];

  public internalWorkspaceEventBus = new ReplaySubject<WorkspaceEvent>(1);

  /**
   * Switch between IMAGE & VIDEO labeling.
   */
  public mediaType: LabelModeType = undefined;
  public readonly LabelModeType = LabelModeType;

  // Current loaded media metadata
  public currentMediaId: string;
  public currentImageUrl: string;
  public currentMediaWidth: number;
  public currentMediaHeight: number;
  public resizeOffsetWidth: number;
  public resizeOffsetHeight: number;

  // zoom state
  public currentZoom: number;
  public maxZoom: number;

  // Holds current drawn geometries
  public currentGeometries: BaseGeometry[] = [];

  public selectedGeometries: BaseGeometry[] = [];

  public copiedGeometry: BaseGeometry;

  public drawingGeometry: BaseGeometry;

  public svgLayer: Container;

  /**
   * If the user selected a tool, this variable should be <true> util the
   * draw is finished (recognized by callbacks)
   */
  public isUserDrawing: boolean = false;
  public isUserDrawingSub: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  // properties to hold AISeg state
  public hiddenAISegPoly: PolygonGeometry;
  public currentAISegGeometry: BaseGeometry;


  get aiSegActive(): boolean {
    return this.aisegApiService.aiSegActive;
  }

  set aiSegActive(state: boolean) {
    this.aisegApiService.aiSegActive = state;
  }

  get aiSegActivationEvent(): EventEmitter<boolean> {
    return this.aisegApiService.onActivationEvent;
  }

  constructor(
    private contextMenu: ContextMenuService,
    private aisegApiService: AisegApiService,
    private utility: WorkspaceUtilityService,
    private modalService: NgxSmartModalService,
    public mediaStyleFilter: MediaStyleFilterService,
    private labNotificationService: LabNotificationService
  ) {
  }

  private id2copy: string;
  private mousePosition: WorkspacePoint = new WorkspacePoint(0, 0);

  /**
   * The constraint box is used within the geometries to
   * limit the draggable area as argument passed to the
   * 'svg.draggable.js' plugin.
   */
  public get constraint_box() {
    return {
      minX: 0,
      minY: 0,
      maxX: this.currentMediaWidth,
      maxY: this.currentMediaHeight
    };
  }

  public loadMedia(src: string, id: string, type: LabelModeType): void {
    this.mediaStyleFilter.resetFilters();
    this.currentImageUrl = src;
    this.currentMediaId = id;
    this.mediaType = type;
  }

  public resetMedia(): void {
    this.mediaStyleFilter.resetFilters();
    this.currentImageUrl = '';
    this.currentMediaId = '';
  }

  /**
   * Gets triggered after the workspace-component has successfully loaded the provided media
   *
   * @param element The specific media element
   * @param containerWidth The outer container width
   * @param containerHeight The outer container height
   */
  public afterMediaLoaded(element: EventTarget, containerWidth: number, containerHeight: number): void {
    this.scaleMedia(element, containerWidth, containerHeight);
    this.recreateSvg();
  }

  /**
   * Trigger on any event that should close the video value context menu.
   */
  public closeVideoContextMenu(): void {
    const ctx = new WorkspaceEvent(null, WorkspaceEventType.CLOSE_VIDEO_CONTEXT_MENU);
    this.internalWorkspaceEventBus.next(ctx);
  }

  /**
   * Gets triggered on right click (context menu) on a geometry.
   * @param event: MouseEvent / ContextMenuEvent
   * @param geometry
   */
  public onGeometryContextMenu(event: UIEvent, geometry: BaseGeometry) {
    this.closeVideoContextMenu();

    if (this.isUserDrawing) {
      return;
    }

    /*
     * Make sure, the geometry that opened the context menu is selected
     */
    if (!this.selectedGeometries.includes(geometry)) {
      this.selectGeometry(geometry);
    }

    this.contextMenu.open(WorkspacePoint.fromEvent(event));

    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * @param geometryProperties The specific description of the geometry
   * @param init if <true> should be used if you want to initialize geometries by code (user NOT drawing)
   */
  public drawGeometry(
    geometryProperties: GeometryProperties,
    init: boolean = false,
  ): BaseGeometry {
    if (!init) {
      this.isUserDrawing = true;
      this.isUserDrawingSub.next(true);
      // Unselect geometries when user is drawing a new one
      this.unselectAllGeometries();
    }
    const latestCreatedGeometry = GeometryFactory.create(this, geometryProperties);

    if (!init) {
      this.drawingGeometry = latestCreatedGeometry;
    }
    return latestCreatedGeometry;
  }

  public disableDrawingMode(): void {
    this.drawingGeometry = undefined;
    this.isUserDrawing = false;
    this.isUserDrawingSub.next(false);
  }

  /**
   * - Adds an geometry to the selected geometries
   * - Enable geometry-selection (for resizing)
   * @param geometry
   */
  public selectGeometry(geometry: BaseGeometry): void {
    if (this.selectedGeometries.indexOf(geometry) === -1) {
      geometry.beforeSelecting();
      if (geometry.geometryType === GeometryType.POLYGON) {
        (geometry.svgObject as unknown as WithSelectize).selectize({
          rotationPoint: false,
          // if enabled, you can select every single point to resize
          deepSelect: true,
          pointSize: this.calculateSelectCircleRadius()
        });
      } else if (geometry.geometryType === GeometryType.LINE) {
        (geometry.svgObject as unknown as WithSelectize).selectize({
          deepSelect: true,
          pointSize: this.calculateSelectCircleRadius()
        });
      } else if (geometry.geometryType === GeometryType.POINT) {
        // No selection for point so far
        (geometry.svgObject as unknown as WithSelectize).selectize({
          rotationPoint: false,
          points: [],
          classRect: 'svg_select_point_rect',
          pointSize: this.calculateSelectCircleRadius()
        });
      } else if (geometry.geometryType === GeometryType.IMAGE_SEGMENTATION) {
        // Do nothing
      } else if (geometry.geometryType === GeometryType.IMAGE_SEGMENTATION_ERASER) {
        // Do nothing
      } else {
        (geometry.svgObject as unknown as WithSelectize).selectize({
          rotationPoint: false,
          pointSize: this.calculateSelectCircleRadius()
        });
      }
      this.selectedGeometries.push(geometry);
      this.rescaleSvgElements();
      geometry.afterSelecting();

      /*
       * Make the selected geometry draggable. But only if just one geometry is selected.
       * Otherwise, if more than one geometry are selected remove the draggability from
       * all of them.
       */
      if (this.selectedGeometries.length === 1) {
        this.selectedGeometries.forEach(selected => selected.makeDraggable());
      } else {
        this.selectedGeometries.forEach(selected => selected.removeDraggability());
      }

      if (geometry.sendMessageBusEvents) {
        this.internalWorkspaceEventBus.next(new WorkspaceEvent(geometry.geometryProperties.identifier, WorkspaceEventType.SELECTED));
      }
    }
  }

  public unselectGeometry(geometry: BaseGeometry): void;
  public unselectGeometry(geometry: BaseGeometry, noSplice: boolean, emitEvent: boolean): void;
  /**
   * Unselect a geometry
   * @param geometry
   * @param noSplice Should the element get removed from selected array
   * @param emitEvent if false: UNSELECTED event won't be emitted
   */
  public unselectGeometry(geometry: BaseGeometry, noSplice = false, emitEvent = false): void {
    geometry.beforeUnselecting();
    geometry.removeDraggability();
    const selectedGeometryIdx = this.selectedGeometries.indexOf(geometry);
    if (selectedGeometryIdx !== -1) {
      if (geometry.sendMessageBusEvents && emitEvent !== false) {
        this.internalWorkspaceEventBus.next(new WorkspaceEvent(geometry.geometryProperties.identifier, WorkspaceEventType.UNSELECTED));
      }
      if (geometry.svgObject !== undefined) {
        (geometry.svgObject as unknown as WithSelectize).selectize(false, {
          deepSelect: true
        });
        (geometry.svgObject as unknown as WithSelectize).selectize(false);
      }
      if (!noSplice) {
        this.selectedGeometries.splice(selectedGeometryIdx, 1);
      }
    }
  }

  public deleteAllGeometries() {
    this.unselectAllGeometries();
    this.currentGeometries.forEach(geometry => {
      geometry.deleteFromSVGLayer();
    });
    this.currentGeometries = [];
  }

  public unselectAllGeometries(): void;
  public unselectAllGeometries(emitEvent: boolean): void;
  public unselectAllGeometries(emitEvent = false): void {
    this.selectedGeometries.forEach(geo => {
      this.unselectGeometry(geo, true, emitEvent);
    });
    this.selectedGeometries = [];
  }

  /**
   * This is called when a user presses delete while not in demoMode
   * Checks if any geometries are selected and emits a WorkspaceListEvent of type DELETE_REQUEST which triggers actions
   * in label-mode
   */
  public onDeleteRequest(): void {
    if (this.selectedGeometries.length === 0) {
      this.labNotificationService.error_i18('FEATURE.WORKSPACE.DELETE_REQUEST_ERROR');
      return;
    }
    const internalIdList: string[] = [];
    this.selectedGeometries.forEach(geo => {
      internalIdList.push(geo.geometryProperties.identifier);
    });
    this.internalWorkspaceEventBus.next(new WorkspaceListEvent(internalIdList, WorkspaceEventType.DELETE_REQUEST));
  }

  /**
   * Unselect a geometry, removes it from the main svg layer, removes it from object list
   *
   * @param geometry The specific geometry to remove
   */
  public deleteGeometry(geometry: BaseGeometry): void {
    const currentGeometryIdx = this.currentGeometries.indexOf(geometry);
    if (currentGeometryIdx !== -1) {
      // Unselect before deleting
      this.unselectGeometry(geometry);
      geometry.deleteFromSVGLayer();
      this.currentGeometries.splice(currentGeometryIdx, 1);
    }
  }

  /**
   * Deletes a geometry by its identifier
   * @param identifier The specific identifier id
   */
  public deleteGeometryByIdentifier(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach((geo: BaseGeometry) => this.deleteGeometry(geo));
  }

  /**
   * Gray out the given geometries and if necessary also all their child geometries.
   *
   * @param identifier
   */
  public grayOutGeometry(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    // Gray out all the given geometries
    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach(geo => geo.suppress());

    // Gray out all their child geometries
    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.lcEntryValueParentId))
      .forEach(geo => geo.suppress());
  }

  /**
   * Hides a geometry by its identifier
   * @param identifier The specific identifier id
   */
  public hideGeometryByIdentifier(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach((geo: BaseGeometry) => geo.hide());
  }

  /**
   * Hides a geometry by its identifier
   * @param identifier The specific identifier id
   */
  public showGeometryByIdentifier(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach((geo: BaseGeometry) => geo.show());
  }


  public selectGeometryByIdentifier(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach((geo: BaseGeometry) => this.selectGeometry(geo));
  }

  public unselectGeometryByIdentifier(identifier: string|string[]): void {
    const identifiers = Array.isArray(identifier) ? identifier : [identifier];

    this.currentGeometries
      .filter(geo => identifiers.includes(geo.geometryProperties.identifier))
      .forEach((geo: BaseGeometry) => this.unselectGeometry(geo));
  }

  /**
   * Returns the geometry data by its identifier
   * @param identifier
   */
  public getGeometryDataByIdentifier(identifier: string): BaseGeometryData | undefined {
    const geometry: BaseGeometry = this.currentGeometries.find(geo => geo.geometryProperties.identifier === identifier);
    if (geometry !== undefined) {
      return geometry.getGeometryData();
    }
    return undefined;
  }

  /**
   * Returns true if geometry is outside the svg workspace boundaries
   * @param identifier
   */
  public checkIfGeometryIsOutsideBoundariesByIdentifier(identifier: string): boolean | undefined {
    const geometry: BaseGeometry = this.currentGeometries.find(geo => geo.geometryProperties.identifier === identifier);
    if (geometry !== undefined) {
      return geometry.outOfBounds();
    }
    return undefined;
  }

  /**
   * Sets the geometry data by its identifier
   * @param identifier
   * @param data
   */
  public setGeometryDataByIdentifier(identifier: string, data: BaseGeometryData): void {
    const geometry: BaseGeometry = this.currentGeometries.find(geo => geo.geometryProperties.identifier === identifier);
    if (geometry !== undefined) {
      geometry.setGeometryData(data);
      geometry.addCommentDecorator(data.comment);
    }
  }

  /**
   * Handle event when user presses ctrl+c
   */
  public onCtrlCopyEvent(): void {
    // No support for copy&paste for video labelling right now
    if (this.mediaType === LabelModeType.VIDEO) {
      return;
    }
    const selectedGeometries = this.selectedGeometries;
    if (selectedGeometries.length === 0) {
      this.labNotificationService.error_i18('FEATURE.WORKSPACE.ERROR.COPY.NONE');
    } else if (selectedGeometries.length > 1) {
      this.labNotificationService.error_i18('FEATURE.WORKSPACE.ERROR.COPY.TOO_MANY');
    }
    this.id2copy = selectedGeometries[0].geometryProperties.identifier;
  }

  /**
   * Handle event when user presses ctrl+v
   */
  public onCtrlPasteEvent(): void {
    // No support for copy&paste for video labelling right now
    if (this.mediaType === LabelModeType.VIDEO || this.id2copy === undefined) {
      return;
    }

    const geometry2copy = this.currentGeometries.find(
      geo => geo.geometryProperties.identifier === this.id2copy
    );

    if (geometry2copy === undefined) {
      this.id2copy = undefined;
      // The geometry was deleted
      return;
    }

    const adjust = new WorkspacePoint(this.resizeOffsetWidth, this.resizeOffsetHeight);
    const mousePosition = this.mousePosition.scaleUp(adjust);
    this.contextMenu.contextMenuEventBus.next(new ContextMenuEvent(this.id2copy, ContextMenuEventType.DUPLICATE_GEOMETRY, mousePosition));
    this.mousePosition = new WorkspacePoint(0, 0);
  }

  /**
   * Handle event if user presses esc
   * if drawing: cancels drawing current geometry
   * else: unselects all
   */
  public onEscapePressedEvent(): void {
    if (this.isUserDrawing && this.drawingGeometry !== undefined) {
      this.cancelDrawingGeometry(null,true);
    } else {
      this.unselectAllGeometries(true);
    }
  }

  /**
   * Cancels drawing of the current geometry:
   * - unbinds eventListener on 'drawstop' event because draw('cancel') uses draw.stop in svg.draw.js
   * - deletes the geometry from selected- and currentGeometries if needed
   * - sets inDrawingMode to false for polygon (usually this happens after drawstop is fired)
   * - calls draw('cancel') which cancels drawing and deletes the geometry
   * - disables drawing mode to reset some properties
   * @param geometry: optionally give this method a geometry to cancel, if not set this uses the this.drawingGeometry
   * @param emitEvent: set this to false to not emit canceled event
   */
  public cancelDrawingGeometry(geometry?: BaseGeometry, emitEvent: boolean = true): void {
    let geometryToCancel: BaseGeometry;
    if (!!geometry) {
      geometryToCancel = geometry;
    } else {
      geometryToCancel = this.drawingGeometry;
    }
    geometryToCancel.svgObject.off('drawstop');

    // Must be executed before a draw('cancel')
    if (geometryToCancel.svgObject.type !== 'path') {
      // for path object there exists no svg.draw.js implementation.
      this.fixDrawJsPluginInternal(geometryToCancel);
      (geometryToCancel.svgObject as unknown as WithDraw).draw('cancel');
    }
    this.deleteGeometry(geometryToCancel);

    if (geometryToCancel.geometryProperties.geometryType === GeometryType.POLYGON) {
      (geometryToCancel as PolygonGeometry).inDrawingMode = false;
    }
    this.currentGeometries.forEach(geo => geo.stopSuppressing());
    if (emitEvent !== false) {
      this.internalWorkspaceEventBus.next(new WorkspaceEvent(geometryToCancel.geometryProperties.identifier, WorkspaceEventType.CANCELED));
    }
    this.disableDrawingMode();
  }

  /**
   * There is a bug in the svg.draw.js library.
   * If you want to cancel a geometry without initialization (first point), the library
   * wants to clear a internal-variable "set". If it is undefined the clearing process fails
   * and the svg.js library is broken. To fix this we create a new set before canceling a
   * geometry.
   *
   * @param geometryToCancel
   */
  public fixDrawJsPluginInternal(geometryToCancel: BaseGeometry) {
    const paintHandler = geometryToCancel.svgObject.remember('_paintHandler');
    if (paintHandler !== undefined) {
      const paintHandlerSet = paintHandler.set;
      if (paintHandlerSet === undefined) {
        paintHandler.set = new SVG.Set();
      }
    } else {
      if ((geometryToCancel.svgObject as any).init === "function") {
        // The set will be created in the init function
        (geometryToCancel.svgObject as any).init();
      }
    }
  }

  /**
   * This gets called if aiSeg is active and escape gets pressed
   * cancels the aiSegRectangle, restores the previous polygon, deletes media from aiSeg and notifies user
   */
  public cancelAISeg(): void {
    if (!this.aiSegActive) {
      return;
    }

    // Notify the bus that the aiseg drawing got canelled
    this.internalWorkspaceEventBus.next(new WorkspaceEvent(undefined, WorkspaceEventType.AISEG_CANCELED));

    // don't send canceled event when canceling aiSeg
    this.cancelDrawingGeometry(this.currentAISegGeometry, false);
    this.hiddenAISegPoly.show();
    this.hiddenAISegPoly = undefined;
    this.currentAISegGeometry = undefined;
    this.aiSegActive = false;
  }

  /**
   * Save the current zoom state and resize the svg-elements
   * @param currentZoom The current zoom state
   * @param zoomMax The maximum zoom state
   * @param isPanning Is the user is panning or zooming
   */
  public updateZoomState(currentZoom: number, zoomMax: number, isPanning: boolean) {
    this.currentZoom = currentZoom;
    this.maxZoom = zoomMax;

    // Update the screen matrix (for the svg plugins) for zooming while drawing/resizing
    this.currentGeometries.forEach(geo => geo.updateZoomState());

    // Rescaling is only necessary when zooming
    if (!isPanning) {
      this.rescaleSvgElements();
    }
  }

  /**
   * We are NOT SCALING the svg rather transforming the complete div with the angular library ng2-panzoom. Due this fact
   * we must manually scale down the specific svg-parameters (like stroke-width, circle-radius, etc...)
   */
  public rescaleSvgElements() {
    // Check if layer is initialized
    if (this.svgLayer !== undefined) {
      this.svgLayer.each((el, member) => {
        (member[el] as Element).attr('stroke-width', this.calculateStrokeWidth());
      });
      this.resizeSvgSelectStyle();
    }
  }

  /**
   * Through the fact that the styles for the svg-select plugin are static we need to override them at the specific times
   */
  public resizeSvgSelectStyle(): void {
    // Override bounding rectangle (helping lines by resizing)
    const rec = this.svgLayer.select('rect.svg_select_boundingRect');
    rec.each((el, member) => {
      (member[el] as Element).attr('stroke-width', this.calculateStrokeWidth());
    });

    // Override circle radius (helping points) AND poly-points (while drawing)
    const circ = this.svgLayer.select('circle');
    circ.each((el, member) => {
      const isADrawedGeometry = this.currentGeometries
        .filter(circle => !!circle.hasSvgObject)
        .findIndex(circle => circle.svgId === (member[el] as any).id());
      if (isADrawedGeometry === -1) {
        (member[el] as unknown as WithRadius).radius(this.calculateSelectCircleRadius());
      } else {
        // This element is a drawed point
        (member[el] as unknown as WithRadius).radius(this.calculateSelectCircleRadius(true));
      }
    });
  }

  public calculateStrokeWidth(): number {
    return (2 / this.maxZoom) * (this.maxZoom - this.currentZoom);
  }

  /**
   * Calculates the radius of the helper-circles (for resizing)
   */
  public calculateSelectCircleRadius(isPoint: boolean = false): number {
    if (isPoint) {
      return (4 / this.maxZoom) * (this.maxZoom - this.currentZoom);
    }
    return (3 / this.maxZoom) * (this.maxZoom - this.currentZoom);
  }

  /**
   * Callback on click within the svg layer, registered within
   * recreateSvg().
   *
   * This method is used to unselect all geometries if
   * - the user has finished drawing (e.g. is not drawing anymore)
   * - and the event target is the svg layer itself
   * - and at least one geometry is selected.
   *
   * @param event
   */
  private onClickIntoSVGLayer(event: any): void {
    if (this.isUserDrawing === true) {
      return;
    }
    if ((event.target.tagName || '') !== 'svg') {
      return;
    }
    if (this.selectedGeometries.length === 0) {
      return;
    }
    // Also inform the workspace to remove selection markers.
    this.unselectAllGeometries(true);
  }

  /**
   * Should be called if the media is successfully loaded
   * @param element The specific media element to scale
   * @param containerWidth The available width of the outer container
   * @param containerHeight The available height of the outer container
   */
  private scaleMedia(element: EventTarget, containerWidth: number, containerHeight: number) {

    const mediaWidth = this.mediaType === LabelModeType.IMAGE
      ? (element as HTMLImageElement).naturalWidth
      : (element as HTMLVideoElement).videoWidth;

    const mediaHeight = this.mediaType === LabelModeType.IMAGE
      ? (element as HTMLImageElement).naturalHeight
      : (element as HTMLVideoElement).videoHeight;

    const fitDimensions: ScaledImageDimensions =
      this.utility.getObjectFitSize(true, containerWidth, containerHeight, mediaWidth, mediaHeight);

    this.currentMediaWidth = fitDimensions.width;
    this.currentMediaHeight = fitDimensions.height;
    this.resizeOffsetWidth = mediaWidth / fitDimensions.width;
    this.resizeOffsetHeight = mediaHeight / fitDimensions.height;
  }

  /**
   * Create main svg object and inform service
   */
  private recreateSvg() {
    this.svgLayer = SVG('svg-layer');

    this.svgLayer.click(event => {
      this.onClickIntoSVGLayer(event);
    });

    this.afterSvgCreated();

    // Reset workspace params
    this.currentGeometries = [];
    this.selectedGeometries = [];
    this.copiedGeometry = undefined;
    this.isUserDrawing = false;

    this.internalWorkspaceEventBus.next(new WorkspaceEvent(undefined, WorkspaceEventType.WORKSPACE_READY));
  }

  /**
   * Gets triggered after the workspace-component has created the svg-container
   */
  private afterSvgCreated() {
    this.svgLayer.size(this.currentMediaWidth, this.currentMediaHeight);
    this.svgLayer.attr({
      'resizeOffsetWidth': this.resizeOffsetWidth,
      'resizeOffsetHeight': this.resizeOffsetHeight,
    });
    // Log all the mouse moves so we can access the current mouse position.
    this.svgLayer.on('mousemove', (e) => {
      this.mousePosition.updateCoordinates(e.offsetX, e.offsetY);
    });
  }

}
