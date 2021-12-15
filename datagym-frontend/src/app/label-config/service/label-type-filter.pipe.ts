import { Pipe, PipeTransform } from '@angular/core';
import { LcEntry } from '../model/LcEntry';
import {LcEntryType} from '../model/LcEntryType';

export enum LabelType {
  GEO = 'GEO',
  CLASSIFICATION = 'CLASSIFICATION'
}

@Pipe({
  name: 'labelTypeFilter'
})
export class LabelTypeFilterPipe implements PipeTransform {

  transform(labelEntries: LcEntry[], filter: LabelType): LcEntry[] {
    let filtered = labelEntries;
    if (!labelEntries || !filter) {
      // nothing to do
    } else if (filter === LabelType.GEO) {
      filtered = labelEntries.filter((entry: LcEntry) =>
        LcEntryType.isGeometry(entry.type)
      );
    } else if (filter === LabelType.CLASSIFICATION) {
      filtered = labelEntries.filter((entry: LcEntry) =>
        LcEntryType.isClassification(entry.type)
      );
    }
    return filtered;
  }
}
