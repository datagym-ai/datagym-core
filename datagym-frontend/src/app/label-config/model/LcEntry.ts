import { LcEntryType } from './LcEntryType';
import { UUID } from 'angular2-uuid';

export abstract class LcEntry {
  public id: string;
  public type: LcEntryType;
  public lcEntryParentId: string;
  public children: LcEntry[];
  public entryKey: string;
  public entryValue: string;

  public isNewEntry(): boolean {
    return this.id.startsWith('internal_');
  }

  protected constructor(type: LcEntryType, lcEntryParentId: string | null, key?: string, value?: string) {
    this.id = `internal_${ UUID.UUID() }`;
    this.type = type;
    this.lcEntryParentId = lcEntryParentId;
    this.children = [];
    this.entryKey = key || null;
    this.entryValue = value || null;
  }

  public static hasNestedGeometries(entry: LcEntry|LcEntry[]): boolean {
    const children = Array.isArray(entry) ? entry : entry.children || [];
    return children.filter(child => LcEntryType.isGeometry(child)).length > 0;
  }
}
