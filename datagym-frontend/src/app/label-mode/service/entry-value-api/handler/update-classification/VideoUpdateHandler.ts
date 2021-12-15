import {UpdateHandler} from './UpdateHandler';
import {LcEntryClassificationValue} from '../../../../model/classification/LcEntryClassificationValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {EntryValueApiService} from '../../entry-value-api.service';
import {EventEmitter} from '@angular/core';
import {EntryChangeService} from '../../../entry-change.service';
import {VideoControlService} from '../../../video-control.service';
import {Observable} from 'rxjs';
import {LcEntryChangeFactory} from '../../../../model/change/LcEntryChangeFactory';
import {LcEntryChangeType} from '../../../../model/change/LcEntryChangeType';
import {take, takeUntil} from 'rxjs/operators';


export class VideoUpdateHandler extends UpdateHandler {

  public constructor(
    labelTaskId: string,
    private entryValueApiService: EntryValueApiService,
    private videoControl: VideoControlService,
    private changeApi: EntryChangeService,
    private onUpdateClassification: EventEmitter<LcEntryClassificationValue>,
    private onChange: EventEmitter<void>
  ) {
    super(labelTaskId);
  }

  public updateClassification(value: LcEntryClassificationValue): void {

    const currentFrameNumber = this.videoControl.currentFrameNumber;

    if (value.change.length > 0) {
      /*
       * If the value wasn't changed, compared with the previous keyframe (ordered
       * by the frameNumbers) don't create or update the change list.
       */
      const tmpChange: LcEntryChange = (new LcEntryChangeFactory()).fromValue(value);

      const previousFrame = value.change.find(
        c => c.frameNumber === Math.max(...value.frameNumbers.filter(n => n <= currentFrameNumber))
      );

      if (!!previousFrame && !!tmpChange && previousFrame.equalValues(tmpChange)) {
        return;
      }
    }

    const change = value.change.find(c => c.frameNumber === currentFrameNumber);
    const handleChangeObject$: Observable<LcEntryChange> = change === undefined
      ? this.createChange(value, currentFrameNumber)
      : this.updateChange(value, change);

    handleChangeObject$.pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((newChange: LcEntryChange) => {

      value.addChange(newChange);

      this.onChange.emit();
      this.onUpdateClassification.emit(value);
    });
  }

  private createChange(value: LcEntryClassificationValue, currentFrameNumber: number): Observable<LcEntryChange> {

    const newChange = (new LcEntryChangeFactory({
      type: LcEntryChangeType.CHANGE,
      frameNumber: currentFrameNumber
    })).fromValue(value);

    return this.changeApi.create(value, newChange);
  }

  private updateChange(value: LcEntryClassificationValue, change: LcEntryChange): Observable<LcEntryChange> {

    const newChange = (new LcEntryChangeFactory({
      id: change.id,
      type: change.frameType,
      frameNumber: change.frameNumber
    })).fromValue(value);

    return this.changeApi.update(newChange);
  }
}
