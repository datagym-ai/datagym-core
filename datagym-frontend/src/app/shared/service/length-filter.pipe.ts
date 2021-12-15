import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'lengthFilter'
})
export class LengthFilterPipe implements PipeTransform {

  transform(stringToFilter: string, length?: number): string {
    if (stringToFilter === undefined || stringToFilter === null) {
      return '';
    }
    if (length === null || length < 1 || stringToFilter.length <= length) {
      return stringToFilter;
    }
     return stringToFilter.substr(0, length) + '...';
  }
}
