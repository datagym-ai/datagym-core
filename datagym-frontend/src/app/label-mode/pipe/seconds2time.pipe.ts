import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'seconds2time'
})
export class Seconds2timePipe implements PipeTransform {

  transform(seconds: any, ...args: unknown[]): string {
    if (seconds === null || seconds === undefined || typeof seconds !== 'number') {
      return null;
    }

    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds - (m * 60));

    const minutePrefix = m < 10 ? '0' : '';
    const secondPrefix = s < 10 ? '0' : '';

    return `${ minutePrefix }${ m }:${ secondPrefix }${ s }`;
  }

}
