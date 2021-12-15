import {LcEntry} from '../import';

export class LcEntryContainer {
  constructor(private readonly lcEntries: LcEntry[] = []) {
  }

  /**
   * Find an LcEntry within the stacks this.rootGeometries of this.globalClassifications by it's id.
   *
   * Optional any LcEntryType can be passed as second argument. If one is passed that type is used to
   * decide if only within this.rootGeometries or this.globalClassifications should be searched.
   * On default both stacks would be searched at.
   *
   * Todo: refactoring: contains duplicate code from `EntryConfigService`.
   *
   * @param id
   */
  public findRecursiveById(id: string): LcEntry | undefined {
    return this.findRecursive(entry => entry.id === id);
  }

  public findRecursive(predicate: (value: LcEntry, index: number, obj: LcEntry[]) => boolean) : LcEntry | undefined {
    /**
     * Find within the given stack.
     *
     * @param stack
     */
    const finder = (stack: LcEntry[]) : LcEntry | undefined => {
      let found: LcEntry = stack.find(predicate);
      if (!!found) {
        return found;
      }

      for (const entry of stack) {
        const children = entry.children || [];
        found = finder(children);
        if (!!found) {
          return found;
        }
      }

      return undefined;
    };

    return finder(this.lcEntries);
  }

}

