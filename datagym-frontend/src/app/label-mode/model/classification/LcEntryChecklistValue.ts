import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryClassificationValue} from './LcEntryClassificationValue';
import {LcEntryChecklistChange} from '../change/LcEntryChecklistChange';
import {LcEntryType} from '../import';

export class LcEntryChecklistValue extends LcEntryClassificationValue<LcEntryChecklistChange> {
  readonly kind = LcEntryType.CHECKLIST;

  public checkedValues: string[];

  constructor(
    id: string | null,
    lcEntry: LcEntry | null,
    media: string | null,
    labelIteration: LabelIteration | string | null,
    timestamp: number | null,
    labeler: string | null,
    children: LcEntryClassificationValue[] | null,
    parent: string | null,
    valid: boolean,
    checkedValues: string[] | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid);
    this.checkedValues = checkedValues;
  }

  /**
   * Create a fully copy of the current LcEntryChecklistValue.
   */
  public createClone(): LcEntryChecklistValue {
    return new LcEntryChecklistValue(
      this.id,
      this.lcEntry,
      this.mediaId,
      this.labelIteration,
      this.timestamp,
      this.labeler,
      this.children.map(c => c.clone()) as LcEntryClassificationValue[],
      this.lcEntryValueParentId,
      this.valid,
      [...this.checkedValues]
    );
  }

  public withChange(change: LcEntryChecklistChange): LcEntryChecklistValue {
    const clone = this.clone() as LcEntryChecklistValue;
    if (!!change) {
      clone.checkedValues = [...change.checkedValues];
    }
    return clone;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntryChecklistValue): void {
    this.checkedValues = source.checkedValues;
  }

  protected isThisEntryValid(): boolean {

    if (!this.isRequired) {
      return true;
    }

    return this.checkedValues !== undefined && this.checkedValues.length > 0;
  }

  public setCheckedValues(value) : void {
    this.checkedValues = value;
  }


}
