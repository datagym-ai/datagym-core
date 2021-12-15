import {CreateHandler} from './CreateHandler';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryValueUpdateFactory} from '../../../../model/LcEntryValueUpdateFactory';
import {take, takeUntil} from 'rxjs/operators';
import {EntryValueApiService} from '../../entry-value-api.service';
import {EventEmitter} from '@angular/core';

/**
 * Technically this is equal to the ImageUpdateHandler.
 */
export class ImageCreateHandler extends CreateHandler {

  constructor(
    labelTaskId: string,
    private onChange: EventEmitter<void>,
    protected valueApiService: EntryValueApiService,
  ) {
    super(labelTaskId);
  }

  public createGeometry(value: LcEntryGeometryValue): void {
    const data = LcEntryValueUpdateFactory.prepare(this.labelTaskId).geometry(value);
    this.valueApiService.updateSingleValue(value.id, data).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.onChange.emit();
    });
  }
}
