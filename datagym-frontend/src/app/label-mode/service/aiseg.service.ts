import {EventEmitter, Injectable} from '@angular/core';
import {AiSegCalculateObject, WorkspaceExternalService} from '../../svg-workspace/service/workspace-external.service';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {PolygonGeometry} from '../../svg-workspace/geometries/geometry/PolygonGeometry';
import {AiSegResponse} from '../../svg-workspace/model/AiSegResponse';
import {LabelModeUtilityService} from './label-mode-utility.service';
import {EntryValueService} from './entry-value.service';
import {AiSegCalculate} from '../../svg-workspace/model/AiSegCalculate';
import {AiSegType} from '../model/AiSegType';
import {LabelModeType} from '../model/import';
import {VideoControlService} from './video-control.service';
import {takeUntil} from "rxjs/operators";
import {UUID} from "angular2-uuid";
import {WorkspaceControlService} from "../../svg-workspace/service/workspace-control.service";
import {WorkspaceListenerFilter as Filter} from "./workspace-listener-filter";
import {WorkspaceEventType} from "../../svg-workspace/messaging/WorkspaceEventType";
import {AisegApiService} from "../../svg-workspace/service/aiseg-api.service";
import {UserService} from "../../client/service/user.service";


export enum AisegNotes {
  NEW_SUBJECT,
  LOADING,
  ERROR_CALCULATING,
  COMPLETED
}

@Injectable({
  providedIn: 'root'
})
export class AisegService {

  public notifications$: BehaviorSubject<AisegNotes> = new BehaviorSubject<AisegNotes>(AisegNotes.NEW_SUBJECT);

  /**
   * Store here the selected aiseg geometry type so it can also be used
   * outside of the toolbar component.
   */
  public selectedType: AiSegType = AiSegType.POINTS;

  get aiSegActivationEvent(): EventEmitter<boolean> {
    return this.workspace.aiSegActivationEvent;
  }

  get aiSegActive(): boolean {
    return this.workspace.aiSegActive;
  }

  private unsubscribe: Subject<void> = new Subject<void>();

  get isAllowed(): boolean {
    return this.isAvailable &&
      (this.workspace.hasPolygonSelected ||
        (this.workspace.isDrawing &&
          this.workspace.isDrawingPolygon))
      ;
  }

  /**
   * Return the translation identifier as title string.
   */
  get title(): string {

    // create a object of title translations and a boolean flag.
    // the first key with a value of true is returned. So the order
    // of the titles matters.
    const titles: {[key: string]: boolean} = {
      // this should not be possible.
      'OPEN_CORE.FEATURE_NOT_AVAILABLE': this.userService.userDetails.isOpenCoreEnvironment,
      'AISEG.PREVIEW': this.demoProject,
      'FEATURE.PROJECT.DUMMY': this.dummyProject,
      'AISEG.LIMIT_REACHED': this.aiSegLimitReached,
      'AISEG.FAILED_TO_PREPARE_IMAGE': !this.workspace.isAiPrepared,
      'AISEG.ERROR_LOAD_WORKSPACE_IMAGE': !this.workspace.hasCurrentMedia,
      'AISEG.LOADING_HEADLINE': this.workspace.isAiCalculating,
      'AISEG.IS_ACTIVE': this.workspace.aiSegActive,
      'AISEG.ERROR_WRONG_TYPE': this.workspace.isDrawing && !this.workspace.isDrawingPolygon,
      'AISEG.ERROR_USAGE': !this.workspace.isDrawing,
      // fallback if all above result in false values.
      'AISEG.LABEL': true,
    };
    // extract the keys and filter the truthfully ones.
    const title = Object.keys(titles)
      .filter(t => !!titles[t]);

    return title[0];
  }

  public get dummyProject(): boolean {
    return this._dummyProject;
  }

  public get demoProject(): boolean {
    return this._demoProject;
  }

  public get aiSegLimitReached(): boolean {
    return this._aiSegLimitReached;
  }

  private _dummyProject: boolean = false;
  private _demoProject: boolean = false;
  private _aiSegLimitReached: boolean = false;
  // Remember the last video screenshot
  private _lastVideoScreenshotId: string = undefined;
  private _lastVideoScreenshotFrame: number = undefined;
  public readonly userSessionUUID = UUID.UUID();

  constructor(
    private valueService: EntryValueService,
    private videoControl: VideoControlService,
    private workspace: WorkspaceExternalService,
    private workspaceControl: WorkspaceControlService,
    private labelModeUtilityService: LabelModeUtilityService,
    private aisegApiService: AisegApiService,
    private userService: UserService
  ) {
    // Handle canceled aiseg requests
    const filter = Filter.TYPE(WorkspaceEventType.AISEG_CANCELED);
    this.workspaceControl.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        if (this.selectedType === AiSegType.POINTS) {
          this.aisegApiService.finishUserSession(this.userSessionUUID).subscribe(() => {
          });
        }
      });
  }

  get isAvailable(): boolean {
    const isImageType = this.valueService.labelModeType === LabelModeType.IMAGE;
    // AiSeg prepare is only important for image mode, in video mode the prepare gets done when activating aiseg
    const aiSegPrepared: boolean = isImageType ? this.workspace.isAiPrepared : true;

    return this.workspace.hasCurrentMedia &&
      !this.demoProject &&
      !this.dummyProject &&
      !this.aiSegLimitReached &&
      !this.userService.userDetails.isOpenCoreEnvironment &&
      aiSegPrepared &&
      !this.workspace.aiSegActive;
  }

  public init(dummyProject: boolean, demoProject: boolean, aiSegLimitReached: boolean): void {
    this._dummyProject = dummyProject;
    this._demoProject = demoProject;
    this._aiSegLimitReached = aiSegLimitReached;

    // Release the video frame screenshot after a frame change to release storage
    this.videoControl.onFrameChanged.pipe(takeUntil(this.unsubscribe)).subscribe((frameNumber) => {
      if (this._lastVideoScreenshotId !== undefined && this._lastVideoScreenshotFrame !== undefined) {
        if (this._lastVideoScreenshotFrame !== frameNumber) {
          this.finishVideoFrameScreenshot(this._lastVideoScreenshotId, this._lastVideoScreenshotFrame).subscribe(() => {
          });
          this._lastVideoScreenshotId = undefined;
          this._lastVideoScreenshotFrame = undefined;
        }
      }
    });
  }

  public reset(): void {
    this.notifications$.complete();
    this.notifications$ = new BehaviorSubject<AisegNotes>(AisegNotes.NEW_SUBJECT);

    if (this.valueService.labelModeType === LabelModeType.IMAGE) {
      this.finishCurrentImage().subscribe(() => {
      });
    } else {
      if (this._lastVideoScreenshotId !== undefined && this._lastVideoScreenshotFrame !== undefined) {
        this.finishVideoFrameScreenshot(this._lastVideoScreenshotId, this._lastVideoScreenshotFrame)
          .subscribe(() => {
          });
        this._lastVideoScreenshotId = undefined;
        this._lastVideoScreenshotFrame = undefined;
      }
    }

    // ... unsubscribe internal ...
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  // Inform the cluster that a new image should be fetched
  public prepareCurrentMedia(frameNumber: number = undefined, dataUri: string = null): Observable<void> {
    return this.workspace.prepareCurrentMedia(frameNumber, dataUri);
  }

  public finishCurrentImage(): Observable<void> {
    return this.workspace.finishCurrentImage();
  }

  public finishVideoFrameScreenshot(mediaId: string, frameNumber: number): Observable<void> {
    return this.workspace.finishFrameImage(mediaId, frameNumber);
  }

  /**
   * @param type optional define here the aiseg type to use.
   * @param isRefinePrediction a geometry created in *this session* can be refined via context menu.
   */
  public initAiseg(type: AiSegType = undefined, isRefinePrediction: boolean = false): void {
    if (!this.isAllowed) {
      return;
    }

    if (type === undefined) {
      if (this.selectedType === undefined) {
        this.selectedType = AiSegType.POINTS;
      }
      type = this.selectedType;
    }

    // Make a screenshot of the current frame and sent it to the aiseg cluster
    if (this.valueService.labelModeType === LabelModeType.VIDEO) {
      if (this._lastVideoScreenshotId !== this.workspace.id &&
        this._lastVideoScreenshotFrame !== this.videoControl.currentFrameNumber) {

        // Release old image
        if (this._lastVideoScreenshotId !== undefined && this._lastVideoScreenshotFrame !== undefined) {
          this.finishVideoFrameScreenshot(this._lastVideoScreenshotId, this._lastVideoScreenshotFrame).subscribe(() => {
          });
          this._lastVideoScreenshotId = undefined;
          this._lastVideoScreenshotFrame = undefined;

        }
        this.prepareCurrentMedia(this.videoControl.currentFrameNumber, this.makeVideoScreenshot()).subscribe(() => {
        });
        this._lastVideoScreenshotId = this.workspace.id;
        this._lastVideoScreenshotFrame = this.videoControl.currentFrameNumber;
      }

    }

    const isDrawing = this.workspace.isDrawing;
    this.workspace.drawAiSegTarget(type, isRefinePrediction).subscribe((response: AiSegCalculateObject) => {
      this.aiSegObjectDrawed(type, response.aiSegCalc, response.aiSegPolygon, isDrawing);
    });
  }

  /**
   * "Handler"-Method after the aiseg-rectangle is drawed
   * @param aisegCalc The specific "aiseg-"rectangle calculation
   * @param updatePolygon The polygon where the point-values from the aiseg-calculation should be applied
   * @param wasDrawing decides if the polygon should be restored or deleted in error case.
   */
  private aiSegObjectDrawed(type: AiSegType = undefined, aisegCalc: AiSegCalculate, updatePolygon: PolygonGeometry, wasDrawing: boolean): void {
    this.notifications$.next(AisegNotes.LOADING);

    if (this.valueService.labelModeType === LabelModeType.VIDEO) {
      aisegCalc.frameNumber = this.videoControl.currentFrameNumber;
    }

    if (this.selectedType === AiSegType.POINTS) {
      aisegCalc.userSessionUUID = this.userSessionUUID;
    }

    this.workspace.calculateImageByAiseg(aisegCalc).subscribe((response: AiSegResponse) => {
      const isContinuousLabeling: boolean = type === AiSegType.POINTS;
      const autoDrawNext: boolean = !isContinuousLabeling;

      // aiseg succeed
      this.handleAiSegResponse(response, updatePolygon, autoDrawNext, wasDrawing);

      // Enter refinement-mode / continous-aiseg for points
      if (isContinuousLabeling) {
        this.workspaceControl.selectSingleGeometry(updatePolygon.geometryProperties.identifier);
        this.initAiseg(AiSegType.POINTS, true);
      }

    }, () => {
      // aiseg failed
      this.handleAiSegFailure(updatePolygon, wasDrawing);
    });
  }

  /**
   * On successfully receive some aiseg points, plot this polygon
   * @param response
   * @param updatePolygon
   */
  private handleAiSegResponse(response: AiSegResponse, updatePolygon: PolygonGeometry, autoDrawNext: boolean, wasDrawing: boolean): void {

    // Update polygon of AiSeg-Response
    updatePolygon.hide();
    updatePolygon.syncPoints(response.result);
    updatePolygon.inDrawingMode = false;

    /*
     * Do not update the polygon here. Use emitUpdateAISegPolygonData instead. This will
     * emit the WorkspaceEventType.DATA_UPDATED event and calls the backend.
     * ~this.valueService.updateGeometryById(updatePolygon.geometryProperties.identifier);~
     */

    updatePolygon.show();

    this.workspace.aiSegActive = false;
    this.labelModeUtilityService.userIsDrawing = false;
    this.workspace.unselectAllGeometries();
    this.workspace.emitUpdateAISegPolygonData(updatePolygon, autoDrawNext, wasDrawing);

    this.notifications$.next(AisegNotes.COMPLETED);
  }

  /**
   * On failure of aiseg, handle that also.
   *
   * @param updatePolygon
   * @param wasDrawing
   */
  private handleAiSegFailure(updatePolygon: PolygonGeometry, wasDrawing: boolean): void {
    if (wasDrawing) {
      // delete the polygon
      this.workspace.deleteGeometry(updatePolygon);
    } else {
      // restore the polygon.
      updatePolygon.show();
    }
    this.workspace.aiSegActive = false;
    this.labelModeUtilityService.userIsDrawing = false;
    this.notifications$.next(AisegNotes.ERROR_CALCULATING);

    // Release the current image
    if (this.valueService.labelModeType === LabelModeType.VIDEO) {
      if (this._lastVideoScreenshotId !== undefined && this._lastVideoScreenshotFrame !== undefined) {
        this.finishVideoFrameScreenshot(this._lastVideoScreenshotId, this._lastVideoScreenshotFrame).subscribe(() => {
        });
        this._lastVideoScreenshotId = undefined;
        this._lastVideoScreenshotFrame = undefined;
      }
    }
  }

  public unselectAllGeometries(): void {
    this.workspace.unselectAllGeometries();
  }

  /**
   * Captures the current frame of the video and returns a dataUri
   * @private
   */
  private makeVideoScreenshot(): string | undefined {
    if (this.valueService.labelModeType === LabelModeType.IMAGE) {
      return undefined;
    }

    const videoPlayer = document.querySelector('video');
    const canvas = document.createElement('canvas');
    canvas.height = videoPlayer.videoHeight;
    canvas.width = videoPlayer.videoWidth;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(videoPlayer, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL('image/jpeg');
  }
}
