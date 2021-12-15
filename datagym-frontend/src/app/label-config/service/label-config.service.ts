import {EventEmitter, Injectable} from '@angular/core';
import {LabelConfiguration} from '../model/LabelConfiguration';
import {LcEntry} from '../model/LcEntry';
import {LcEntryType} from '../model/LcEntryType';
import {LcEntryGeometry} from '../model/geometry/LcEntryGeometry';
import {LcEntryFactory} from '../model/LcEntryFactory';
import {LcEntryClassification} from '../model/classification/LcEntryClassification';
import {LabelConfigApiService} from './label-config-api.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LabelConfigService {
  public doneEditing: EventEmitter<void> = new EventEmitter<void>();
  public dirty: boolean = false;
  public hasNewLabels = false;
  public changedRequiredToTrue = false;
  private tempBoolLabelAdded = false;
  private tempBoolRequiredFromFalseToTrue = false;
  private config: LabelConfiguration;
  public readonly geometryColors: string[] = [
    '#3333FF', '#6300ED',
    '#9A00CD', '#CD0066',
    '#FB110D', '#F141FA',
    '#FAF45C', '#009A00',
    '#66FF33', '#41FAD8'];

  constructor(private api: LabelConfigApiService) {}

  /**
   * Do NOT use this for performing actions on the entries inside this service! Only for displaying in Components
   */
  get entries(): LcEntry[] {
    // slice to trigger change detection on nested object change
    return this.config.entries.slice().sort(this.orderByRootNameAlphabetically);
  }

  get id(): string {
    return this.config.id;
  }

  get numberOfCompletedTasks(): number {
    return this.config.numberOfCompletedTasks;
  }

  get numberOfReviewedTasks(): number {
    return this.config.numberOfReviewedTasks;
  }

  public getExportConfigUrl(configId: string): string {
    return this.api.getExportUrl(configId);
  }

  public updateLabelConfig(id: string, data: LcEntry[], changeCompletedTasksStatus: boolean = false): Observable<LabelConfiguration> {
    return this.api.updateLabelConfig(id, data, changeCompletedTasksStatus);
  }

  public deleteLabelConfig(id: string): Observable<LabelConfiguration> {
    return this.api.deleteLabelConfig(id);
  }

  /**
   * Sets the hasNewLabels flag which is used to determine if save dialogue should be displayed
   *
   * @param entries: LcEntry[] after the editing process
   */
  public wereNewLabelsAdded(entries: LcEntry[]){
    this.calculateIfNewLabelWasAdded(entries);
    this.hasNewLabels = this.tempBoolLabelAdded;
    this.tempBoolLabelAdded = false;
  }

  /**
   * Sets the changedRequiredToTrue flag which is used to determine if save dialogue should be displayed
   *
   * @param startEntries: LcEntry[] before the last editing process
   * @param entries: LcEntry[] after the editing process
   */
  public wasRequiredChangedFromFalseToTrue(startEntries: LcEntry[], entries: LcEntry[]){
    this.calculateIfRequiredChangedFromFalseToTrue(startEntries, entries);
    this.changedRequiredToTrue = this.tempBoolRequiredFromFalseToTrue;
    this.tempBoolRequiredFromFalseToTrue = false;
  }

  /**
   * Calculates if a new entry was added.
   * Recursively looks for internal id in every current LcEntry[].
   *
   * @param entries: LcEntry[] after the editing process
   */

  private calculateIfNewLabelWasAdded( entries: LcEntry[]){
    entries.forEach(entry => {
      if(entry.id.startsWith('internal')){
        this.tempBoolLabelAdded = true;
      }
      this.calculateIfNewLabelWasAdded(entry.children);
    });
  }

  /**
   * Calculates if a required field was changed from false to true.
   * Recursively compares current LcEntry[] with previous LcEntry[], after every editing process.
   *
   * @param startEntries: LcEntry[] before the last editing process
   * @param entries: LcEntry[] after the editing process
   */
  public calculateIfRequiredChangedFromFalseToTrue(startEntries: LcEntry[], entries: LcEntry[]){
    entries.forEach(entry => {
      if (!!(entry as LcEntryClassification) && (entry as LcEntryClassification).required === true) {
        const foundEntry = (this.findLcEntryByIdFromEntryList(entry.id, startEntries) as LcEntryClassification);
        if (!!foundEntry && foundEntry.required===false){
          this.tempBoolRequiredFromFalseToTrue = true;
        }
      }
      this.calculateIfRequiredChangedFromFalseToTrue(startEntries,entry.children);
    });
  }

  public createEntry(newEntry: LcEntry) {
    this.config.entries.push(newEntry);
    this.doneEditing.emit();
  }

  /**
   * Note this init method overrides all unsaved settings!
   * @param: the new configuration labelConfiguration
   */
  public init(newLabelConfiguration: LabelConfiguration): void {
    this.config = newLabelConfiguration;
    this.dirty = false;
    this.config.entries = LcEntryFactory.castEntriesListProperly(this.config.entries);
    this.doneEditing.emit();
  }

  public deleteEntryById(idToDelete: string): void {
    let index = this.getIndexById(idToDelete);
    if (index === -1) {
      const parentId = this.findLcEntryById(idToDelete).lcEntryParentId;
      const parent = this.findLcEntryById(parentId);
      if (parent !== null) {
        index = parent.children.findIndex((child: LcEntry) => child.id === idToDelete);
        parent.children.splice(index, 1);
        this.doneEditing.emit();
      }
    } else if (index >= 0) {
      this.config.entries.splice(index, 1);
      this.doneEditing.emit();
    }
    this.dirty = true;
  }

  /**
   * Attempt to find an entry or ChildEntry by its id
   * @param entryId
   */
  public findLcEntryById(entryId: string): LcEntry | null {
    const entryById = this.getEntryById(entryId);
    if (entryById === null) {
      return this.deepChildrenSearch(this.config.entries, entryId);
    }
    return entryById;
  }

  /**
   * Attempt to find an entry or ChildEntry by its id, on a given LcEntry[] array
   * @param entryId
   * @param entries: LcEntry[], the entries were the LcEntry should be looked for
   */
  public findLcEntryByIdFromEntryList(entryId: string, entries: LcEntry[]): LcEntry | null {
    const entryById = this.getEntryById(entryId);
    if (entryById === null) {
      return this.deepChildrenSearch(entries, entryId);
    }
    return entryById;
  }

  /**
   * Return a list of all used exportKeys within the scope of
   * the entry with the given id or the global scope if that id
   * is not set.
   */
  public getUsedExportKeys(id: string | undefined | null): string[] {
    // On geometries or media classifications, the export keys must be unique.
    if (id === undefined || id === null) {
      const entries = !!this.config.entries ? this.config.entries : [];
      return entries.map(entry => entry.entryKey);
    }

    let parent = this.findLcEntryById(id);
    if (parent === undefined) {
      // error case, should not be possible
      return [];
    }

    // loop through the stack until the 'root' parent element is found
    while (!!parent.lcEntryParentId) {
      parent = this.findLcEntryById(parent.lcEntryParentId);
    }

    // and collect *all* export keys within that parent node.
    const deepSearch = function (classification: LcEntry, usedKeys: string[]): string[] {
      usedKeys.push(classification.entryKey);
      classification.children.forEach((child) => {
        deepSearch(child, usedKeys);
      });
      return usedKeys;
    };

    return deepSearch(parent, []);
  }

  /**
   * Return a list of all used colors.
   * The array entries are not unique, a color can be used
   * multiple times.
   */
  public getUsedColors(): string[] {
    return this.config.entries
      .filter((e) => LcEntryType.isGeometry(e))
      .map((e: LcEntryGeometry) => e.color);
  }

  /**
   * Return a random color for a LcEntry
   */
  public getNextColor(): string {
    const countObj: {[key: string]: number} = {};
    const usedColors = this.getUsedColors();
    // fill the countObject with color -> counter values
    this.geometryColors.forEach((color: string) => {
      countObj[color] = usedColors.filter(c => c === color).length;
    });

    const min: number = Object.values(countObj).reduce((a: number, b: number) => Math.min(a, b));

    // return the first color with the minimal usage.
    return this.geometryColors.find(c => countObj[c] === min);
  }

  public getUsedShortCuts(): string[] {
    const entries = !!this.config.entries ? this.config.entries : [];
    // shortcuts are only available for root geometries so no recursive lookup is needed.
    return entries
      .filter(entry => LcEntryType.isGeometry(entry))
      .map(entry => (entry as LcEntryGeometry).shortcut || '')
      .filter(shortcut => !!shortcut)
      // the new added shortcut appears as number not a string so force casting:
      .map(s => `${s}`);
  }

  /**
   * Rename LcEntry to name + ' COPY' if it has a duplicate name.
   * @param entry: LcEntry that needs a shortcut
   */
  public renameEntryToCopy(entry: LcEntry): LcEntry {
    entry.entryValue += ' COPY';
    entry.entryKey += '_copy';

    const entryValues: string[] = this.entries.map((e:LcEntry) => e.entryValue);

    if (entryValues.includes(entry.entryValue)) {
      const copyNumber = this.getNextCopyNumber(entry.entryValue, entryValues, 1);
      entry.entryKey = `${ entry.entryKey }_${ copyNumber }`;
      entry.entryValue = `${ entry.entryValue }_${ copyNumber }`;
    }
    return entry;
  }

  /**
   * Helper function. Return the next possible shortcut that is not already in the shortcut list.
   * If there are not shortcuts left (0-9) return null
   */
  public getNextShortcut(): string | null {
    const ten = 10;
    const shortcutList = this.getUsedShortcuts();
    let i = 1;

    while (i <= ten) {
      const shortcut = (i % ten).toString();
      if (!shortcutList.includes(shortcut)) {
        return shortcut;
      }
      i++;
    }
    return null;
  }

  /**
   * Callback function for array.sort to order LcEntries alphabetically by their entryValue
   * This is called in entries getter
   * @param entryA
   * @param entryB
   */
  private orderByRootNameAlphabetically = (entryA: LcEntry, entryB: LcEntry): number => {
    if (entryA.entryValue === null || entryB.entryValue === null) {
      return 0;
    } else {
      return entryA.entryValue.localeCompare(entryB.entryValue);
    }
  };

  /**
   * Attempt to find an entry by its id
   * @param id
   */
  private getEntryById(id: string): LcEntry | null {
    const index = this.getIndexById(id);
    if (index >= 0) {
      return this.config.entries[index];
    }
    return null;
  }

  /**
   * Recursive search through all children
   * @param entries The specific children to look for
   * @param searchEntryId The specific uuid/id to search for
   */
  private deepChildrenSearch(entries: LcEntry[], searchEntryId: string): LcEntry | undefined {
    if (!/* not */!!entries) {
      return undefined;
    }
    for (const entry of entries) {
      if (entry.id === searchEntryId) {
        return entry;
      }
      const found = this.deepChildrenSearch(entry.children, searchEntryId);
      if (found) {
        return found;
      }
    }
    return undefined;
  }

  /**
   * Attempt to find the index of and entry by its id
   * @param id
   */
  private getIndexById(id: string): number {
    return this.config.entries.findIndex((entry: LcEntry) => entry.id === id);
  }

  /**
   * Recursive Helper function. Adds a number to a name and checks if name is in a list of names.
   * Increments the number until it finds a name that is not in the name list.
   * @param name
   * @param nameList
   * @param i
   */
  private getNextCopyNumber(name: string, nameList: string[], i: number) {
    const copyName = `${ name }_${ i }`;
    if (nameList.includes(copyName)) {
      i++;
      return this.getNextCopyNumber(name, nameList, i);
    } else {
      return i;
    }
  }

  /**
   * Return a list of all used shortcuts.
   * The array entries are unique, a shortcut can't be used
   * multiple times.
   */
  public getUsedShortcuts(): string[] {
    return this.config.entries
      .filter((e) => LcEntryType.isGeometry(e))
      .map((e: LcEntryGeometry) => e.shortcut);
  }
}
