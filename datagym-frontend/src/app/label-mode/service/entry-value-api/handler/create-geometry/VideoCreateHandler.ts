import {CreateHandler} from './CreateHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {EventEmitter} from '@angular/core';
import {LcEntryType} from '../../../../../label-config/model/LcEntryType';
import {EntryValueService} from '../../../entry-value.service';
import {LcEntryChangeFactory} from '../../../../model/change/LcEntryChangeFactory';
import {LcEntryChangeType} from '../../../../model/change/LcEntryChangeType';
import {VideoControlService} from '../../../video-control.service';
import {forkJoin, Observable, of} from 'rxjs';
import {take, takeUntil} from 'rxjs/operators';
import {EntryChangeService} from '../../../entry-change.service';
import {LcEntryValue} from '../../../../model/LcEntryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';


export class VideoCreateHandler extends CreateHandler {

  constructor(
    labelTaskId: string,
    private readonly changeApi: EntryChangeService,
    private readonly valueService: EntryValueService,
    private readonly videoControl: VideoControlService,
    private readonly newValue: EventEmitter<LcEntryValue>
  ) {
    super(labelTaskId);
  }

  /**
   * For new created geometries without change objects:
   * - Create a START object with the current geometries.
   * - Create a END object with the current geometries.
   */
  public createGeometry(value: LcEntryGeometryValue): void {

    if ([
      LcEntryType.IMAGE_SEGMENTATION, // This type is not supported within the video labeling
      LcEntryType.IMAGE_SEGMENTATION_ERASER, // This type is not supported in the BE in any way.
    ].includes(value.kind)) {
      return;
    }

    const newCreated = value.change.length === 0;
    if (!newCreated) {
      // This should not be possible!
      return;
    }

    const rootParentId = this.valueService.findRootValue(value).id;

    const inLastFrame = this.videoControl.currentFrameNumber === this.videoControl.totalFrames;

    /**
     * Add the 'START' change object for the current frame and
     * a 'END' change object for the last possible frame.
     */
    const startChangeType = inLastFrame ? LcEntryChangeType.START_END : LcEntryChangeType.START;
    const startChangeFrameNumber = this.videoControl.currentFrameNumber;

    const startChange$ = this.create(value, rootParentId, startChangeType, startChangeFrameNumber);

    const endChange$ = !inLastFrame
      ? this.create(value, rootParentId, LcEntryChangeType.END, this.videoControl.totalFrames)
      : of(null);

    forkJoin([
      startChange$,
      endChange$
    ]).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(([startResponse, endResponse]) => {

      value.addChange(startResponse);

      if (!!endResponse) {
        endResponse.handCrafted = false;
        value.addChange(endResponse);
      }

      /**
       * Inform the VideoMediaController about the new value to reinitialise the value service
       * and the video value lines.
       */
      this.newValue.emit(value);
    });
  }

  private create(value: LcEntryValue, rootParentId: string, type: LcEntryChangeType, frameNumber: number): Observable<LcEntryChange> {

    const change = (new LcEntryChangeFactory({type, frameNumber})).fromValue(value);

    return this.changeApi.create(value.id, rootParentId, change);
  }
}
