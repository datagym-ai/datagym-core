import {EventEmitter, Injectable} from '@angular/core';
import {AisegApiService} from './aiseg-api.service';
import {Observable} from 'rxjs';
import {WorkspaceInternalService} from './workspace-internal.service';
import {AiSegCalculate} from '../model/AiSegCalculate';
import {WorkspaceUtilityService} from './workspace-utility.service';
import {GeometryType} from '../geometries/GeometryType';
import {BaseGeometry} from '../geometries/BaseGeometry';
import {AiSegResponse} from '../model/AiSegResponse';
import {PolygonGeometry} from '../geometries/geometry/PolygonGeometry';
import {WorkspaceEvent} from '../messaging/WorkspaceEvent';
import {WorkspaceEventType} from '../messaging/WorkspaceEventType';
import {AiSegType} from '../../label-mode/model/AiSegType';
import * as SVGType from '../model/utility/SVGTypes';
import {GeometryProperties} from '../geometries/GeometryProperties';
import {ValueConfiguration} from '../geometries/ValueConfiguration';


/**
 * Response used within the Observable of drawAiSegRectangle
 */
export type AiSegCalculateObject = {
  aiSegCalc: AiSegCalculate,
  aiSegPolygon: PolygonGeometry,
};

/**
 * Use this service to wrap the internal workspace service.
 * The 'internal' service should not be known outside the workspace module.
 *
 * Note: this service cannot be used within the workspace-internal service.
 * This would result in circular dependencies that cannot be resolved.
 */
@Injectable({
  providedIn: 'root'
})
export class WorkspaceExternalService {

  get hasCurrentMedia(): boolean {
    return this.workspaceInternal.currentMediaId !== undefined &&
      !!this.workspaceInternal.currentMediaId;
  }

  get id(): string {
    return this.workspaceInternal.currentMediaId;
  }

  get isDrawing(): boolean {
    return this.workspaceInternal.isUserDrawing;
  }

  get isDrawingPolygon(): boolean {
    return !!this.workspaceInternal.drawingGeometry
      ? this.workspaceInternal.drawingGeometry.geometryType === GeometryType.POLYGON
      : false;
  }

  get hasPolygonSelected(): boolean {
    return this.workspaceInternal.selectedGeometries.length === 1 &&
      this.workspaceInternal.selectedGeometries[0].geometryType === GeometryType.POLYGON;
  }

  get isAiCalculating(): boolean {
    return this.aiseg.isCalculating;
  }
  get isAiPrepared(): boolean {
    return this.aiseg.isPrepared;
  }

  get aiSegActive(): boolean {
    return this.aiseg.aiSegActive;
  }
  set aiSegActive(state: boolean) {
    if (!state) {
      // if aiseg is not active, make sure to cleanup the workspace internal service!
      this.workspaceInternal.hiddenAISegPoly = undefined;
      this.workspaceInternal.currentAISegGeometry = undefined;
    }
    this.aiseg.aiSegActive = state;
  }

  get aiSegActivationEvent(): EventEmitter<boolean> {
    return this.workspaceInternal.aiSegActivationEvent;
  }

  constructor(
    private aiseg: AisegApiService,
    private utilityService: WorkspaceUtilityService,
    private workspaceInternal: WorkspaceInternalService
  ) {
  }

  public emitUpdateAISegPolygonData(updatePolygon: PolygonGeometry, autoDrawNext: boolean, wasDrawing: boolean): void {
    if (wasDrawing) {
      this.workspaceInternal.internalWorkspaceEventBus.next(new WorkspaceEvent(updatePolygon.geometryProperties.identifier, WorkspaceEventType.DRAW_FINISHED, autoDrawNext));
    } else {
      this.workspaceInternal.internalWorkspaceEventBus.next(new WorkspaceEvent(updatePolygon.geometryProperties.identifier, WorkspaceEventType.DATA_UPDATED));
    }

    this.workspaceInternal.internalWorkspaceEventBus.next(new WorkspaceEvent(updatePolygon.geometryProperties.identifier, WorkspaceEventType.SELECTED));
  }

  // Inform the cluster that a new media should be fetched
  public prepareCurrentMedia(frameNumber: number = undefined, dataUri: string = null): Observable<void> {
    return this.aiseg.prepareMedia(this.id, frameNumber, dataUri);
  }

  public calculateImageByAiseg(calcObj: AiSegCalculate): Observable<AiSegResponse> {
    return this.aiseg.calculate(calcObj);
  }

  public finishCurrentImage(): Observable<void> {
    return this.aiseg.finishImage();
  }

  public finishFrameImage(mediaId: string, frameNumber: number): Observable<void> {
    return this.aiseg.finishFrameImage(mediaId, frameNumber);
  }

  public unselectAllGeometries(): void;
  public unselectAllGeometries(emitEvent: boolean = false): void {
    this.workspaceInternal.unselectAllGeometries(emitEvent);
  }

  public deleteGeometry(geometry: BaseGeometry): void {
    this.workspaceInternal.deleteGeometry(geometry);
  }

  public drawAiSegTarget(type: AiSegType, isRefinePrediction: boolean = false): Observable<AiSegCalculateObject> {

    let geometryType: GeometryType;
    switch (type) {
      case AiSegType.BRUSH:
        geometryType = GeometryType.POLYLINE;
        break;
      case AiSegType.POINT:
        geometryType = GeometryType.POINT;
        break;
      case AiSegType.POINTS:
        geometryType = GeometryType.POINTS;
        break;
      case AiSegType.EDGE_LINE:
        geometryType = GeometryType.APPROXIMATE_EDGES;
        break;
      case AiSegType.RECTANGLE:
        // fall through
      default:
        geometryType = GeometryType.RECTANGLE;
        break;
    }

    this.aiSegActive = true;

    const aiSegPolygon = this.prepareAiSegPolygon(isRefinePrediction);

    const configuration = ValueConfiguration.UNDEFINED();
    const geometryProperties = GeometryProperties.RANDOM(geometryType, configuration);
    const aiSegTarget = this.workspaceInternal.drawGeometry(geometryProperties, false);

    // Set reference in workspaceInternal for cancel-call
    this.workspaceInternal.currentAISegGeometry = aiSegTarget;
    aiSegTarget.sendMessageBusEvents = false;
    aiSegTarget.startDrawing();

    this.workspaceInternal.isUserDrawing = true;
    this.workspaceInternal.isUserDrawingSub.next(true);

    // Wait until the aiseg-rectangle geometry is drawed
    return new Observable<AiSegCalculateObject>((observer) => {
      aiSegTarget.svgObject.on('drawstop', () => {
        observer.next(this.prepareAiSegCalculationObject(aiSegTarget, aiSegPolygon, isRefinePrediction));
        observer.complete();
      });
    });
  }

  private prepareAiSegCalculationObject(geo: BaseGeometry, aiSegPolygon: PolygonGeometry, isRefinePrediction: boolean = false) : AiSegCalculateObject {
    geo.syncSvgToData();
    const aiSegCalc = geo.createAiSegCalculationObject(this.id);

    if (aiSegPolygon.aiSegCalculation !== undefined && isRefinePrediction) {
      // merge the previous calculation object with the new one
      aiSegCalc.positivePoints.push(...aiSegPolygon.aiSegCalculation.positivePoints);
      aiSegCalc.negativePoints.push(...aiSegPolygon.aiSegCalculation.negativePoints);
    }

    aiSegPolygon.aiSegCalculation = aiSegCalc;
    this.unselectAllGeometries();
    this.deleteGeometry(geo);

    return {
      aiSegCalc,
      aiSegPolygon,
    };
  }

  /**
   * Prepare the hidden aiseg polygon.
   */
  private prepareAiSegPolygon(isRefinePrediction: boolean = false): PolygonGeometry {

    const aiSegPolygon = this.hasPolygonSelected
      ? this.workspaceInternal.selectedGeometries[0] as PolygonGeometry
      : this.workspaceInternal.drawingGeometry as PolygonGeometry;

    if (this.workspaceInternal.drawingGeometry !== undefined) {
      /* Disable the message-bus events so that we can stop the
      * drawing process without emitting the draw_finished event to
      * the event bus*/
      aiSegPolygon.sendMessageBusEvents = false;
      this.workspaceInternal.fixDrawJsPluginInternal(aiSegPolygon);
      (aiSegPolygon.svgObject as unknown as SVGType.WithDraw).draw('stop');
      aiSegPolygon.sendMessageBusEvents = true;
    }

    // Set reference in workspaceInternal for cancel-call
    this.workspaceInternal.hiddenAISegPoly = aiSegPolygon;
    this.workspaceInternal.unselectGeometry(aiSegPolygon);
    if (!isRefinePrediction) {
      // Hide the current object to draw a aiseg-rectangle
      aiSegPolygon.hide();
    }


    return aiSegPolygon;
  }
}
