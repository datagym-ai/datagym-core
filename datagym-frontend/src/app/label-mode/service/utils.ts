import {Observable} from 'rxjs';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {LcEntryValue} from '../model/LcEntryValue';


type WithFlat<T> = { flat: (depth: number) => T[] };

export const DEFAULT_TIMEOUT = 300; // ms


export function singleClick(timeout: number = DEFAULT_TIMEOUT) {
  return function<T>(source: Observable<T>): Observable<T> {
    let watchDoubleClick: number = 0;
    return new Observable<T>(subscriber => {
      return source.subscribe({
        next(value) {
          watchDoubleClick++;
          if (watchDoubleClick === 1) {
            setTimeout(() => {
              if (watchDoubleClick === 1) {
                subscriber.next(value);
              }
              watchDoubleClick = 0;
            }, timeout);
          }
        },
        error(error) {subscriber.error(error);},
        complete() {subscriber.complete();}
      });
    });
  };
}

export function doubleClick(timeout: number = DEFAULT_TIMEOUT) {
  return function<T>(source: Observable<T>): Observable<T> {
    let watchDoubleClick: number = 0;
    return new Observable(subscriber => {
      return source.subscribe({
        next(value) {
          watchDoubleClick++;
          setTimeout(() => {
            watchDoubleClick = 0;
          }, timeout);

          if (watchDoubleClick > 1) {
            watchDoubleClick = 0;
            subscriber.next(value);
          }
        },
        error(error) {subscriber.error(error);},
        complete() {subscriber.complete();}
      });
    });
  };
}

/**
 * May some nested geometries appear as child of the root geometry.
 *
 * Append them to the geometry array.
 *
 * @param values
 * @private
 */
export function flatNestedGeometries(values: LcEntryValue[]): LcEntryGeometryValue[] {
  const depth = 2;

  const geometries = values.filter(value => LcEntryType.isGeometry(value.lcEntry.type)) as LcEntryGeometryValue[];

  const nestedGeometryValues = geometries.map(geo => geo.children.filter(child => LcEntryType.isGeometry(child.lcEntry)));

  return [...geometries, ...(nestedGeometryValues as unknown as WithFlat<LcEntryGeometryValue>).flat(depth)];
}
