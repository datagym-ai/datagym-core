import {Pipe, PipeTransform} from '@angular/core';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {GeometryItemHolder} from '../model/GeometryItemHolder';


@Pipe({
  name: 'groupSegmentations'
})
export class GroupSegmentationsPipe implements PipeTransform {

  transform(value: GeometryItemHolder[], ...args: unknown[]): GeometryItemHolder[] {

    if (!/*not*/!!value) {
      return [];
    }

    const seenSegmentationEntryIds: string[] = [];

    return value.filter((geometry: GeometryItemHolder) => {
      if (geometry.lcEntry.type !== LcEntryType.IMAGE_SEGMENTATION) {
        return true;
      }
      if (seenSegmentationEntryIds.includes(geometry.lcEntry.id)) {
        return false;
      }
      seenSegmentationEntryIds.push(geometry.lcEntry.id);
      return true;
    });
  }

}
