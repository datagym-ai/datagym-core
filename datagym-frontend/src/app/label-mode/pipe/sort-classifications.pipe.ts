import { Pipe, PipeTransform } from '@angular/core';
import {LcEntryClassificationValue} from '../model/classification/LcEntryClassificationValue';

@Pipe({
  name: 'sortClassifications'
})
export class SortClassificationsPipe implements PipeTransform {

  transform(value: LcEntryClassificationValue[], ...args: unknown[]): LcEntryClassificationValue[] {

    if (!/*not*/!!value || value.length < 1) {
      return value;
    }

    return value.sort((e1, e2) => {
      const key1 = e1.lcEntry.entryKey.toLowerCase();
      const key2 = e2.lcEntry.entryKey.toLowerCase();

      const sorted = [key1, key2].sort();
      return sorted[0] === key1 ? -1 : 1;
    });
  }

}
