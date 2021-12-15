import {UpdateHandler} from './UpdateHandler';
import {LcEntryClassificationValue} from '../../../../model/classification/LcEntryClassificationValue';
import {LcEntryValueUpdateFactory} from '../../../../model/LcEntryValueUpdateFactory';
import {EventEmitter} from '@angular/core';
import {EntryValueApiService} from '../../entry-value-api.service';


export class ImageUpdateHandler extends UpdateHandler {

  public constructor(
    labelTaskId: string,
    private entryValueApiService: EntryValueApiService,
    private onChange: EventEmitter<void>
  ) {
    super(labelTaskId);
  }

  public updateClassification(value: LcEntryClassificationValue): void {

    const data = LcEntryValueUpdateFactory.prepare(this.labelTaskId).classification(value);
    this.entryValueApiService.updateSingleValue(value.id, data).subscribe(() => {
      this.onChange.emit();
    });
  }
}
