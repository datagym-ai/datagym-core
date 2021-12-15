import {ExpandHandler} from './ExpandHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {ExpandVideoHelper} from './helper/ExpandVideoHelper';
import {ExpandLeftHelper} from './helper/ExpandLeftHelper';
import {ExpandRightHelper} from './helper/ExpandRightHelper';
import {EntryChangeService} from '../../../entry-change.service';
import {EventEmitter} from '@angular/core';
import {VideoControlService} from '../../../video-control.service';
import {forkJoin} from 'rxjs';
import {take} from 'rxjs/operators';
import {LcEntryChangeFactory} from '../../../../model/change/LcEntryChangeFactory';
import {takeUntil} from 'rxjs/operators';


/**
 * In video labeling mode the video value lines can be expanded via context menu.
 * Expand in the selected direction to the current frame or as default 10 frames.
 * If there's another chunk in between or in range + 1 combine them. Otherwise
 * copy the 'last known' coordinates.
 */
export class VideoExpandHandler extends ExpandHandler {

  public constructor(
    private changeApi: EntryChangeService,
    private videoControl: VideoControlService,
    private onUpdateValue: EventEmitter<LcEntryGeometryValue>
  ) {
    super();
  }

  public expandVideoValueLine(value: LcEntryGeometryValue, change: LcEntryChange, left: boolean): void {

    const startFrame = this.videoControl.startFrame;
    const totalFrames = this.videoControl.totalFrames;
    const currentFrameNumber = this.videoControl.currentFrameNumber;

    const helper: ExpandVideoHelper = !left
      ? new ExpandRightHelper(startFrame, totalFrames, currentFrameNumber)
      : new ExpandLeftHelper(startFrame, totalFrames, currentFrameNumber);

    const targetFrameNumber = helper.getTargetFrameNumber(change);
    const change2merge = helper.getChangeToCombine(value, change, targetFrameNumber);

    if (!!change2merge) {
      // Update to change

      change.frameType = helper.getChangeType(change);
      change2merge.frameType = helper.getMergeChangeType(change2merge);

      this.updateChanges(value, change, change2merge);
      return;
    }

    change.frameType = helper.getChangeType(change);

    const newChangeObject = (new LcEntryChangeFactory({
      frameNumber: targetFrameNumber,
      type: helper.newChangeType,
      handCrafted: false
    })).fromChangeObject(change);

    this.handleNewChange(value, change, newChangeObject);
  }

  protected updateChanges(geo: LcEntryGeometryValue, a: LcEntryChange, b: LcEntryChange): void {

    forkJoin([
      this.changeApi.update(a),
      this.changeApi.update(b)
    ]).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(([c, d]) => {
      geo.addChange([c, d]);
      this.onUpdateValue.emit(geo);
    });
  }

  protected handleNewChange(geo: LcEntryGeometryValue, existing: LcEntryChange, newChange: LcEntryChange): void {

    forkJoin([
      this.changeApi.update(existing),
      this.changeApi.create(geo, newChange)
    ]).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(([c, d]) => {
      geo.addChange([c, d]);
      this.onUpdateValue.emit(geo);
    });
  }
}
