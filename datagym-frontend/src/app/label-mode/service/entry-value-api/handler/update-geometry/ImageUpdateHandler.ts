import {UpdateHandler} from './UpdateHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryValueUpdateFactory} from '../../../../model/LcEntryValueUpdateFactory';
import {take, takeUntil} from 'rxjs/operators';
import {EntryValueApiService} from '../../entry-value-api.service';
import {EventEmitter} from '@angular/core';

/**
 * Technically this is equal to the ImageCreateHandler.
 */
export class ImageUpdateHandler extends UpdateHandler {

  public constructor(
    labelTaskId: string,
    entryValueApiService: EntryValueApiService,
    protected onChange: EventEmitter<void>
  ) {
    super(labelTaskId, entryValueApiService);
  }

  /**
   * On images we don't care about change objects on geometry values.
   *
   * @param value
   */
  public updateGeometry(value: LcEntryGeometryValue): void {
    const data = LcEntryValueUpdateFactory.prepare(this.labelTaskId).geometry(value);
    this.entryValueApiService.updateSingleValue(value.id, data).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.onChange.emit();
    });
  }
}
