import {LcEntryGeometryValue} from './geometry/LcEntryGeometryValue';
import {LcEntry} from './import';

/**
 * This is just a helper object to bundle the root geometry
 * together with all it's child values.
 */
export class GeometryItemHolder {

  public get lcEntry(): LcEntry {
    return this.item.lcEntry;
  }

  constructor(
    public item: LcEntryGeometryValue,
    public children: LcEntryGeometryValue[]
  ) { }

}
