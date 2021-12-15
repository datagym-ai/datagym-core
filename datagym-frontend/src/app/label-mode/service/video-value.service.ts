import {EventEmitter, Injectable} from '@angular/core';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LcEntryClassificationValue} from '../model/classification/LcEntryClassificationValue';

@Injectable({
  providedIn: 'root'
})
export class VideoValueService {

  /**
   * This stack contains *all* geometries that are visible within
   * the video value lines. E.g. visible in range `currentFrame +- numberOfFrameBars`.
   *
   * They differ from the values loaded in the EntryValueService. There are only
   * geometries visible in the `currentFrame`.
   */
  public currentValueLines: LcEntryGeometryValue[] = [];

  /**
   * Fires every time, a new geometry value was created.
   */
  public readonly newValue: EventEmitter<LcEntryGeometryValue> = new EventEmitter<LcEntryGeometryValue>();
  /**
   * Fires every time, a geometry was updated.
   *
   * When fired within the VideoUpdateHandler, redraw the value line!
   */
  public readonly onUpdateValue: EventEmitter<LcEntryGeometryValue> = new EventEmitter<LcEntryGeometryValue>();
  /**
   * Fires every time, a nested classification was updated.
   *
   * When fired within the VideoUpdateHandler, redraw the value line!
   */
  public readonly onUpdateClassification: EventEmitter<LcEntryClassificationValue> = new EventEmitter<LcEntryClassificationValue>();

  /**
   * Fires when a full, geometry was deleted.
   *
   * Remove the value line!
   */
  public readonly onDeleteValue: EventEmitter<string> = new EventEmitter<string>();

  /**
   * Fires when a keyframe was deleted.
   *
   * It is used to reinitialize the current frame geometries
   */
  public readonly onDeleteKeyframe: EventEmitter<void> = new EventEmitter<void>();

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  constructor() {
  }

  public init(): void {

    this.onUpdateValue.pipe(takeUntil(this.unsubscribe)).subscribe((value: LcEntryGeometryValue) => {
      // When the value should be redrawn a change object was added or removed.
      // So update the stack.
      this.currentValueLines
        .filter(v => v.id === value.id)
        .forEach(v => v.change = value.change);
    });
  }

  public teardown(): void {
    this.currentValueLines = [];
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
