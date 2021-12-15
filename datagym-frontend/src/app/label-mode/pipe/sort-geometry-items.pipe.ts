import {Pipe, PipeTransform} from '@angular/core';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {LcEntryType} from '../../label-config/model/LcEntryType';


@Pipe({
  name: 'sortGeometryItems'
})
export class SortGeometryItemsPipe implements PipeTransform {

  transform(value: LcEntryGeometry[], ...args: unknown[]): LcEntryGeometry[] {

    if (!/*not*/!!value || value.length < 1) {
      return value;
    }

    const erasers: LcEntryGeometry[] = value
      .filter(entry => entry.type === LcEntryType.IMAGE_SEGMENTATION_ERASER);

    /*
     * Filter all geometries with shortcuts and sort them by shortcut
     */
    const entriesWithShortcut: LcEntryGeometry[] = value
      .filter(entry => !!entry.shortcut)
      .filter(entry => entry.type !== LcEntryType.IMAGE_SEGMENTATION_ERASER)
      .sort((e1, e2) => {
        // equal shortcuts are not possible! + converts the string to number
        return +e1.shortcut < +e2.shortcut ? -1 : 1;
      });

    /*
     * Filter all entries without shortcuts and sort them by entry key.
     */
    const remainingEntries: LcEntryGeometry[] = value
      .filter(entry => !/*not*/!!entry.shortcut)
      .filter(entry => entry.type !== LcEntryType.IMAGE_SEGMENTATION_ERASER)
      .sort((e1, e2) => {
        // equal entryKeys are not possible!
        const sorted = [e1.entryKey.toLowerCase(), e2.entryKey.toLowerCase()].sort();
        return sorted[0] === e1.entryKey.toLowerCase() ? -1 : 1;
      });

    return [...entriesWithShortcut, ...remainingEntries, ...erasers];
  }

}
