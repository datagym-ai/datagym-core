import { LcEntryValue } from './LcEntryValue';

export class LabelIteration {
  public id: string;
  public projectId: string;
  public run: number;
  // contains the entry values as 'mixed object'
  public entryValues: LcEntryValue[] | any[];

  constructor(id: string, projectId: string, run: number, entryValues: []) {
    this.id = id;
    this.projectId = projectId;
    this.run = run;
    this.entryValues = entryValues;
  }
}
