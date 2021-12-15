import {DeleteHandler} from './DeleteHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {LcEntryType} from '../../../../../label-config/model/LcEntryType';
import {LcEntryChangeType} from '../../../../model/change/LcEntryChangeType';
import {ImageDeleteHandler as GeometryDeleteHandler} from '../delete-geometry/ImageDeleteHandler';
import {EntryValueService} from '../../../entry-value.service';
import {EntryValueApiService} from '../../entry-value-api.service';
import {WorkspaceControlService} from '../../../../../svg-workspace/service/workspace-control.service';
import {forkJoin} from 'rxjs';
import {EntryChangeService} from '../../../entry-change.service';
import {take, takeUntil} from 'rxjs/operators';
import {EventEmitter} from '@angular/core';
import {VideoValueService} from '../../../video-value.service';
import {StartDeleteHelper} from './helper/StartDeleteHelper';
import {DeleteHelper} from './helper/DeleteHelper';
import {ChangeDeleteHelper} from './helper/ChangeDeleteHelper';
import {EndDeleteHelper} from './helper/EndDeleteHelper';
import {StartEndDeleteHelper} from './helper/StartEndDeleteHelper';


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
  private readonly deleteGeometryHandler: GeometryDeleteHandler = undefined;

  private readonly onUpdateValue: EventEmitter<LcEntryGeometryValue> = undefined;
  public readonly onDeleteKeyframe: EventEmitter<void> = undefined;

  constructor(
    private readonly valueService: EntryValueService,
    private readonly entryValueApiService: EntryValueApiService,
    private readonly workspaceController: WorkspaceControlService,
    private readonly changeApi: EntryChangeService,
    videoValueService: VideoValueService
  ) {
    super();
    this.onUpdateValue = videoValueService.onUpdateValue;
    this.onDeleteKeyframe = videoValueService.onDeleteKeyframe;
    this.deleteGeometryHandler = new GeometryDeleteHandler(
      valueService,
      entryValueApiService,
      workspaceController,
      videoValueService.onDeleteValue
    );
  }

  public tearDown() {
    this.deleteGeometryHandler.tearDown();
    super.tearDown();
  }

  /**
   * Delete all change objects from the value with the frame number.
   *
   * @param value
   * @param frameNumber
   */
  public deleteChange(value: LcEntryGeometryValue, frameNumber: number): void;

  /**
   * Delete the given change object from the given value.
   *
   * @param value
   * @param change
   */
  public deleteChange(value: LcEntryGeometryValue, change: LcEntryChange): void;

  /**
   * Implementation of the above definitions.
   *
   * This method is called when a keyFrame is deleted from within the videoValueLine.
   *
   * When deleting a keyframe handle these scenarios:
   * - deleting a 'CHANGE' keyframe just removes that one.
   * - deleting a 'START_END' keyframe just removes that one.
   * - deleting a 'START' keyframe
   *  - turns the next 'CHANGE' into a 'START' frame
   *  - turns the next 'END' change into the 'START_END' frame.
   * - deleting a 'END' frame
   *  - turns the previous 'CHANGE' to an 'END'
   *  - turns the previous 'START' into an 'START_END'
   *  - is not supported if it's the only change object. (Should not be possible.)
   *
   * @param geometry: The geometry to delete.
   * @param arg: The change object or it's frameNumber to remove.
   */
  public deleteChange(geometry: LcEntryGeometryValue, arg: number | LcEntryChange): void {

    if (!/*not*/!!geometry || !/*not*/!!arg) {
      return;
    }

    const targetFrameNumber: number = typeof arg === 'number' ? arg : arg.frameNumber;
    const change2delete = geometry.change.find(c => c.frameNumber === targetFrameNumber);
    const containsOtherFrames = geometry.change.find(c => c.frameNumber !== targetFrameNumber) !== undefined;

    if (!containsOtherFrames) {
      // If the value contains only change objects for the given frame number, delete the whole value.
      this.deleteGeometryHandler.deleteGeometry(geometry, {});
      return;
    }

    if (!/*not*/!!change2delete) {
      // Should not be possible.
      return;
    }

    const two = 2;
    if (geometry.change.length === two) {
      const other = geometry.change.find(c => c.frameNumber !== change2delete.frameNumber);
      if (!/*not*/!!other || other.frameType !== LcEntryChangeType.START_END) {
        // if we delete the only start or the only delete element and there is only one change
        // element remaining (that must be also of type START or END) delete instead the full geometry.
        this.deleteGeometryHandler.deleteGeometry(geometry, {});
        return;
      }
    }

    const helper = this.getDeleteHelper(change2delete.frameType);
    if (helper === undefined) {
      return;
    }

    /**
     * Todo: delete the *last* END would draw the lines from the last frame to the right end of the controller.
     */

    const previous$ = helper.handlePreviousChange(geometry, targetFrameNumber);
    const current$ = helper.deleteCurrentChange(change2delete);
    const next$ = helper.handleNextChange(geometry, targetFrameNumber);
    forkJoin([
      previous$,
      current$,
      next$,
    ]).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(([prev, _, next]) => {
      geometry.change = geometry.change.filter(change => change.id !== change2delete.id);
      geometry.addChange([prev, next]);

      this.onUpdateValue.emit(geometry);
      this.onDeleteKeyframe.emit();
    });
  }

  private getDeleteHelper(type: LcEntryChangeType): DeleteHelper {
    switch (type) {
      case LcEntryChangeType.START:
        return new StartDeleteHelper(this.changeApi);
      case LcEntryChangeType.CHANGE:
        return new ChangeDeleteHelper(this.changeApi);
      case LcEntryChangeType.END:
        return new EndDeleteHelper(this.changeApi);
      case LcEntryChangeType.START_END:
        return new StartEndDeleteHelper(this.changeApi);
      case LcEntryChangeType.IMAGE:
      case LcEntryChangeType.INTERPOLATED:
      default:
        // not supported;
        return undefined;
    }
  }
}
