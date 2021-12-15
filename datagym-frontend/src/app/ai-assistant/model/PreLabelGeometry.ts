import {LcEntryType} from '../../label-config/model/LcEntryType';

export class PreLabelGeometry {
  id: string;
  entryKey: string;
  entryValue: string;
  type: LcEntryType;

  constructor(id:string | null, entryKey: string, entryValue: string, type: LcEntryType) {
    this.id = id;
    this.entryKey = entryKey;
    this.entryValue = entryValue;
    this.type = type;
  }
}
