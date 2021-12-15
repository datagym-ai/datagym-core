import {LabelIteration} from './LabelIteration';
import {UUID} from 'angular2-uuid';
import {LcEntry, LcEntryType} from './import';
import {LcEntryGeometryValue} from './geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from './classification/LcEntryClassificationValue';
import {LcEntryChange} from './change/LcEntryChange';


type ApplyChangeArgument =
  string | // The id of the change object.
  number | // The frameNumber of the change object.
  LcEntryChange | // A new change object
  ((change: LcEntryChange) => boolean); // or a filter method.


export abstract class LcEntryValue<CHANGE_CLASS extends LcEntryChange = LcEntryChange> {
  readonly abstract kind: LcEntryType;

  /**
   * Call the ctr to clone the value.
   */
  protected abstract createClone(): LcEntryValue<CHANGE_CLASS>;

  /**
   * Create a clone with the changed attributes.
   * @param change
   */
  public abstract withChange(change: CHANGE_CLASS): LcEntryValue;

  /**
   * Read the user defined properties from the source and set them within this value.
   */
  protected abstract eatValues(source: LcEntryValue<CHANGE_CLASS>): void;

  /**
   * Check if *this* entry is valid. Ignoring the children stack.
   */
  protected abstract isThisEntryValid(): boolean;

  public getNestedClassifications(): LcEntryClassificationValue[] {
    return (this.children || [])
      .filter(child => !LcEntryType.isGeometry(child.lcEntry))
      .sort((a, b) => {
        const key1 = a.lcEntry.entryKey.toLowerCase();
        const key2 = b.lcEntry.entryKey.toLowerCase();

        const sorted = [key1, key2].sort();
        return sorted[0] === key1 ? -1 : 1;
      }) as LcEntryClassificationValue[];
  }

  public getNestedGeometries(): LcEntryGeometryValue[] {
    return (this.children || []).filter(child => LcEntryType.isGeometry(child.lcEntry)) as LcEntryGeometryValue[];
  }

  public markAsDeleted(): void {
    this.preparedForDeletion = true;
    this.children.forEach(child => child.markAsDeleted());
  }

  public get isDeleted(): boolean {
    return this.preparedForDeletion;
  }

  /**
   * On multiple places we only care about the frame numbers itself.
   * The frameNumbers property is not
   * known within the BE.
   */
  public get frameNumbers(): number[] {
    return this.change.map(changes => changes.frameNumber);
  }

  public id: string;
  public lcEntry: LcEntry;
  /**
   * @deprecated Not necessary in the FE
   */
  public mediaId?: string;
  /**
   * @deprecated Not necessary in the FE
   */
  public timestamp?: number;
  /**
   * @deprecated Not necessary in the FE
   */
  public labelIteration?: LabelIteration | string | null;
  /**
   * @deprecated Not necessary in the FE
   */
  public labeler?: string;
  /**
   * @description: Use this property with caution.
   * A better way is to search the geometry list with lcEntryValueParentId.
   * This would include all the geometries created in the current session.
   * The children stack holds only the geometries created at label task load!
   */
  public children: LcEntryValue[] = [];
  public lcEntryValueParentId: string = null;
  public valid: boolean;
  public lcEntryId?: string = '';
  /**
   * There can only one LcEntryChange object per frame.
   * Images consist of only one frame. ;)
   */
  public change: CHANGE_CLASS[] = [];

  /**
   * This flag is a frontend only property.
   * Mark all entryValues prepared for deletion to avoid side effects
   * while the delete request is running.
   */
  private preparedForDeletion: boolean = false;

  /**
   * Todo: Use deconstruction instead of this huge parameter list.
   * @see https://stackoverflow.com/a/40524063
   * @protected
   */
  protected constructor(
    id: string | null,
    lcEntry: LcEntry | null,
    mediaId: string | null,
    labelIteration: LabelIteration | string | null,
    timestamp: number | null,
    labeler: string | null,
    children: LcEntryValue[] | null,
    lcEntryValueParentId: string | null,
    valid: boolean
  ) {
    this.id = id || `internal_${ UUID.UUID() }`;
    this.lcEntry = lcEntry;
    this.mediaId = mediaId;
    this.labelIteration = labelIteration;
    this.timestamp = timestamp;
    this.labeler = labeler;
    this.children = children || [];
    this.lcEntryValueParentId = lcEntryValueParentId;
    this.valid = valid === undefined ? true : valid;
    this.lcEntryId = !!lcEntry ? lcEntry.id : '';
  }

  /**
   * Update the validity flag *recursive* if necessary.
   */
  public updateValidity(): void {
    this.valid = this.isThisEntryValid();
    for (const child of this.children) {
      // Note: the child property is kinda broken.
      // The child property is only set at load time
      // but not updated with new geometries.
      if (LcEntryType.isGeometry(child.lcEntry)) {
        continue;
      }
      child.updateValidity();
      this.valid = this.valid && child.valid;
    }
  }

  /**
   * `eat` the values
   * @param possibleSources
   */
  public eat(possibleSources: LcEntryValue<CHANGE_CLASS>|LcEntryValue<CHANGE_CLASS>[]): void {

    possibleSources = Array.isArray(possibleSources) ? possibleSources : [possibleSources];

    const currentSource = possibleSources.find(s => s.lcEntry.id === this.lcEntry.id);

    this.eatValues(currentSource);

    this.children.forEach(c => c.eat(currentSource.children));

    if (this.lcEntryValueParentId === null) {
      // goes recursively through all children so fire just from the root entries.
      this.updateValidity();
    }
  }

  /**
   * Create a fully copy of the current LcEntryValue.
   */
  public clone(): LcEntryValue<CHANGE_CLASS> {
    const clone = this.createClone();
    clone.change = [...this.change];
    return clone;
  }

  /**
   * This method removes all changes with the same frameNumber
   * as the new changes. Then adds the new ones, sorts the new
   * stack asc by the frameNumber and replaces the old changes.
   *
   * @param change
   */
  public addChange(change: CHANGE_CLASS|CHANGE_CLASS[]): void {
    change = Array.isArray(change) ? change : [change];
    change = change.filter(c => !!c);
    const newFrameNumbers = change.map(c => c.frameNumber);

    if (newFrameNumbers.length === 0) {
      return;
    }

    const changes = this.change.filter(c => !newFrameNumbers.includes(c.frameNumber));
    changes.push(...change);
    changes.sort((a, b): -1|0|1 =>
      a.frameNumber < b.frameNumber ? -1 : a.frameNumber === b.frameNumber ? 0 : 1
    );

    this.change = changes;
  }

  /**
   * Search the current change stack and and call `LcEntryValue.withChange()`
   * with the one as argument that is identified by the given id.
   *
   * @param id
   */
  public applyChange(id: string): LcEntryValue;

  /**
   * Search the current change stack and and call `LcEntryValue.withChange()`
   * with the one as argument that is identified by the given frameNumber.
   *
   * Note: the stack is searched by `Array.find()`. If more than one change
   * object are matching the criteria only the first match is respected. All
   * other matches are ignored. There should be only one change object per
   * frameNumber but in some error cases more than one could be saved. There
   * is no check about that uniqueness.
   *
   * @param frameNumber
   */
  public applyChange(frameNumber: number): LcEntryValue;

  /**
   * Add the new change object with `LcEntryValue.addChange()` and then
   * call `LcEntryValue.withChange()` with that one.
   *
   * @param change
   */
  public applyChange(change: LcEntryChange): LcEntryValue;

  /**
   * If none of the above overloads match the criteria, pass a predicate
   * for `Array.find()` to find and identify the change object that should
   * be used with `LcEntryValue.withChange()`;
   *
   * @param predicate callback function for `Array.find()`.
   */
  public applyChange(predicate: (change: LcEntryChange) => boolean): LcEntryValue;

  /**
   * ApplyChangeArgument combines the possible parameter of the previous arguments.
   *
   * This 'overload' is required because the recursion within the implementation
   * would raise an error like 'No overload matches this call. The last overload gave the following error.'
   *
   * @param arg
   */
  public applyChange(arg: ApplyChangeArgument): LcEntryValue;

  /**
   * Implementation of the definitions above.
   *
   * @param arg
   */
  public applyChange(arg: ApplyChangeArgument): LcEntryValue {

    let predicate = (_) => false;

    switch (typeof arg) {
      case 'number':
        predicate = (c: LcEntryChange) => c.frameNumber === arg;
        break;
      case 'string':
        predicate = (c: LcEntryChange) => c.id === arg;
        break;
      case 'object':
        if (arg instanceof LcEntryChange) {
          this.addChange(arg as CHANGE_CLASS);
          predicate = (c: LcEntryChange) => c === arg;
        }
        break;
      case 'function':
        // A wired way to 'count' the arguments that require to be set.
        if (arg.length === 1) {
          predicate = arg;
        }
        break;
      default:
        // Oooops. This should not be possible. Throw an error?
    }

    const change = this.change.find(predicate);

    const applied = this.withChange(change);
    applied.children = applied.children.map(c => c.applyChange(arg));
    return applied;
  }
}
