import {LcEntryGeometryValue} from '../../../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../../../model/classification/LcEntryClassificationValue';
import {LcEntryValue} from '../../../model/LcEntryValue';


type WithFlat<T> = { flat: (depth: number) => T[] };

/**
 * This class is intended as 'use once' immutable object.
 *
 * At the moment only the lookup for parent values is implemented.
 */
export class ValueStack {

  public constructor(
    private geometries: LcEntryGeometryValue[] = [],
    private mediaClassifications: LcEntryClassificationValue[] = []
  ) {}

  public getParentId(entry: string|LcEntryValue): string {
    return typeof entry !== 'string'
      ? !!entry.lcEntryValueParentId
        ? entry.lcEntryValueParentId
        : entry.id
      : entry;
  }

  /**
   * Find the root value by the given value or it's id / it's parent id.
   *
   * Searches recursively through all geometries and classifications until no more
   * lcEntryValueParentId is set.
   *
   * @param entry
   */
  public findRootValue(entry: string|LcEntryValue): LcEntryValue {
    const parentId = this.getParentId(entry);

    const all = this.flat();

    const lookup = (searchId: string): LcEntryValue => {
      const parent = all.find(value => value.id === searchId);
      if (!/*not*/!!parent) {
        // Invalid id given, should not be possible.
        return undefined;
      }

      if (!/*not*/!!parent.lcEntryValueParentId) {
        // The root found.
        return parent;
      }

      // Loop recursively.
      return lookup(parent.lcEntryValueParentId);
    };

    return lookup(parentId);
  }

  /**
   * Flat the full stack.
   *
   * @private
   */
  public flat(withGrandChildren: boolean = true): LcEntryValue[] {
    const depth = 2;
    const roots = [...this.geometries, ...this.mediaClassifications];
    const children = (roots.map(value => value.children)  as unknown as WithFlat<LcEntryValue>).flat(depth);
    const cc = !!withGrandChildren
      ? (children.map(c => c.children) as unknown as WithFlat<LcEntryValue>)
      : [];

    return ([roots, children, cc] as unknown as WithFlat<LcEntryValue>).flat(depth);
  }
}
