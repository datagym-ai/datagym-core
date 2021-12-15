import { Pipe, PipeTransform } from '@angular/core';
import { LcEntry } from '../model/LcEntry';
import { LcEntryType } from '../model/LcEntryType';
import { LcEntryGeometry } from '../model/geometry/LcEntryGeometry';

@Pipe({
  name: 'sortLabelConfig',
  pure: false
})
export class SortLabelConfigPipe implements PipeTransform {

  transform(value: LcEntry[], ...args: unknown[]): LcEntry[] {

    if (!/*not*/!!value || value.length < 1) {
      return value;
    }

    /*
     * Filter all geometries with shortcuts and sort them by shortcut
     */
    const entriesWithShortcut: LcEntry[] = value
      .filter(entry => LcEntryType.isGeometry(entry.type))
      .map(entry => entry as LcEntryGeometry)
      .filter(entry => !!entry.shortcut)
      .sort((e1, e2) => {
        // equal shortcuts are not possible! + converts the string to number
        return +e1.shortcut < +e2.shortcut ? -1 : 1;
      });

    /*
     * Filter all entries without shortcuts and sort them by entry key.
     */
    const remainingEntries: LcEntry[] = value
      .filter(entry => LcEntryType.isClassification(entry.type) || !/*not*/!!(entry as LcEntryGeometry).shortcut)
      .sort((e1, e2) => {
        if (e1.entryKey === null || e2.entryKey === null) {
          return 1;
        }
        // equal entryKeys are not possible!
        const sorted = [e1.entryKey.toLowerCase(), e2.entryKey.toLowerCase()].sort();
        return sorted[0] === e1.entryKey.toLowerCase() ? -1 : 1;
      });

    return [...entriesWithShortcut, ...remainingEntries];
  }
}
