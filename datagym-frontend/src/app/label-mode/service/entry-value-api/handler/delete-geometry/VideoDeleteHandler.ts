import {EntryValueService} from '../../../entry-value.service';
import {EntryValueApiService} from '../../entry-value-api.service';
import {WorkspaceControlService} from '../../../../../svg-workspace/service/workspace-control.service';
import {EntryChangeService} from '../../../entry-change.service';
import {VideoControlService} from '../../../video-control.service';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChangeFactory} from '../../../../model/change/LcEntryChangeFactory';
import {LcEntryChangeType} from '../../../../model/change/LcEntryChangeType';
import {DeleteHandler} from './DeleteHandler';
import {ImageDeleteHandler} from './ImageDeleteHandler';
import {forkJoin, Observable, of} from 'rxjs';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {LcEntryValue} from '../../../../model/LcEntryValue';
import {map, take, takeUntil} from 'rxjs/operators';
import {EventEmitter} from '@angular/core';
import {LcEntryType} from '../../../../../label-config/model/LcEntryType';
import {DeleteGeometryConfig} from './DeleteGeometryConfig';
import {VideoValueService} from '../../../video-value.service';


export class VideoDeleteHandler extends DeleteHandler {

  /**
   * IMAGE_SEGMENTATION is not supported in video labeling mode.
   *
   * IMAGE_SEGMENTATION_ERASER is not supported within the BE.
   * Override this property in child classes to set different types.
   */
  public readonly unsupportedKinds: LcEntryType[] = [
    LcEntryType.IMAGE_SEGMENTATION,
    LcEntryType.IMAGE_SEGMENTATION_ERASER
  ];

  /**
   * To prevent memory leaks create the instance here.
   * The teardown method must be called once.
   * @private
   */
  private deleteGeometryHandler: ImageDeleteHandler;

  private readonly onUpdateValue: EventEmitter<LcEntryGeometryValue> = undefined;

  public constructor(
    valueService: EntryValueService,
    entryValueApiService: EntryValueApiService,
    workspaceController: WorkspaceControlService,
    private readonly changeApi: EntryChangeService,
    private readonly videoControl: VideoControlService,
    videoValueService: VideoValueService
  ) {
    super(valueService, entryValueApiService, workspaceController);
    this.onUpdateValue = videoValueService.onUpdateValue;
    this.deleteGeometryHandler = new ImageDeleteHandler(
      this.valueService,
      this.entryValueApiService,
      this.workspaceController,
      videoValueService.onDeleteValue
    );
  }

  public tearDown() {
    this.deleteGeometryHandler.tearDown();
    super.tearDown();
  }

  /**
   * This method is called when the geometry is deleted from within the workspace
   * or when the full geometry is deleted via context menu within the video value line.
   *
   * @param value
   * @param config
   */
  public deleteGeometry(value: LcEntryGeometryValue, config: DeleteGeometryConfig): void {
    if (!!config.deleteGeometry) {
      this.deleteGeometryHandler.deleteGeometry(value, config);
    } else if (value.change.length <= 1) {
      this.deleteGeometryHandler.deleteGeometry(value, config);
    } else {
      this.handleChangeObjects(value);
    }
  }

  private handleChangeObjects(value: LcEntryGeometryValue): void {
    /**
     * Don't delete the value, just create a new END-CHANGE object.
     */
    const currentFrameNumber = this.videoControl.currentFrameNumber;
    const previousFrameNumber = currentFrameNumber - 1;

    const currentChange = value.change.find(c => c.frameNumber === currentFrameNumber);
    const nextChange = value.change.find(c => c.frameNumber > currentFrameNumber);
    const previousKeyFrame = [...value.change].reverse().find(c => c.frameNumber < currentFrameNumber);

    const previousObservable$ = this.handlePreviousFrame(previousFrameNumber, previousKeyFrame, value);
    const nextObservable$ = this.handleNextChange(nextChange);
    const currentObservable$ = this.handleCurrentChange(currentChange);

    forkJoin([
      previousObservable$,
      currentObservable$,
      nextObservable$,
    ]).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(([prev, _, next]) => {

      /**
       * Could be updated, created or null for not available.
       */
      if (!/*not*/!!prev) {
        value.change = value.change.filter(c => c.frameNumber !== previousFrameNumber);
      } else {
        value.addChange(prev);
      }

      /**
       * The current frame is removed anyway.
       */
      value.change = value.change.filter(c => c.frameNumber !== currentFrameNumber);

      /**
       * Could be updated, delete or null for not available.
       */
      if (!/*not*/!!next && !!nextChange) {
        value.change = value.change.filter(c => c.frameNumber !== nextChange.frameNumber);
      } else {
        value.addChange(prev);
      }

      this.valueService.removeGeometryFromStack(value);
      this.workspaceController.deleteGeometry(value.id);
      this.onUpdateValue.emit(value);
    });
  }

  /**
   * Delete the current frames. Don't care about their types.
   *
   * @param current
   * @private
   */
  private handleCurrentChange(current: LcEntryChange): Observable<LcEntryChange> {

    if (!/*not*/!!current) {
      /**
       * The current frame is not a keyframe. Do nothing.
       */
      return of(null);
    }

    return this.changeApi.delete(current)
      // Pipe is just for type system / to not use void as return type.
      .pipe(map(() => null));
  }

  /**
   * Change or create the previous frame (currentFrameNumber - 1) to END.
   * Expect it's not a possible frameNumber (< 0).
   *
   * @param previousFrameNumber: number
   * @param previousKeyFrame: LcEntryChange
   * @param value: LcEntryValue
   * @private
   */
  private handlePreviousFrame(previousFrameNumber: number, previousKeyFrame: LcEntryChange, value: LcEntryValue): Observable<LcEntryChange> {

    if (previousFrameNumber <= 0) {
      /**
       * If the previous frame number is not possible, do nothing.
       */
      return of(null);
    }

    /*
     * If the previous frame is a keyframe, update it to an END or START_END keyframe.
     */
    const previous = value.change.find(c => c.frameNumber === previousFrameNumber);
    if (!!previous) {
      if (LcEntryChangeType.isEnd(previous)) {
        return of(previous);
      }

      previous.frameType = previous.frameType === LcEntryChangeType.START
        ? LcEntryChangeType.START_END
        : LcEntryChangeType.END;
      return this.changeApi.update(previous);
    }

    if (!/*not*/!!previousKeyFrame || LcEntryChangeType.isEnd(previousKeyFrame)) {
      /**
       * If the previous keyFrame is a END frame, don't create a new KeyFrame.
       * That means, the current one must be of type start.
       */
      return of(null);
    }

    /*
     * The previous frame is not a keyframe, create it as END keyFrame.
     */
    const endChange = (new LcEntryChangeFactory({
      type: LcEntryChangeType.END,
      frameNumber: previousFrameNumber
    })).fromValue(value);

    const rootParentId = this.valueService.findRootValue(value).id;
    return this.changeApi.create(value.id, rootParentId, endChange);
  }

  private handleNextChange(next: LcEntryChange): Observable<LcEntryChange> {
    if (!/*not*/!!next) {
      /*
       * There is no following keyFrame. Do nothing.
       */
      return of(null);
    }

    switch (next.frameType) {
      case LcEntryChangeType.START:
      case LcEntryChangeType.START_END:
        /*
         * Start should stay.
         */
        return of(next);
      case LcEntryChangeType.CHANGE:
        /*
         * Update to type END.
         */
        next.frameType = LcEntryChangeType.START;
        return this.changeApi.update(next);
      case LcEntryChangeType.END:
        /*
         * If the following change is of type END, delete it.
         */
        return this.changeApi.delete(next)
          // Pipe is just for type system / to not use void as return type.
          .pipe(map(() => null));
      case LcEntryChangeType.IMAGE:
        // Should not be possible
        // Fall through
      case LcEntryChangeType.INTERPOLATED:
        // Should not be possible
        // Fall through
    }
    return of(null);
  }
}
