import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryClassificationValue} from './LcEntryClassificationValue';
import {LcEntrySelectChange} from '../change/LcEntrySelectChange';
import {LcEntryType} from '../import';

export class LcEntrySelectValue extends LcEntryClassificationValue<LcEntrySelectChange> {
  readonly kind = LcEntryType.SELECT;

  public selectKey: string;

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
    selectKey: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid);
    this.selectKey = selectKey;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  public createClone(): LcEntrySelectValue {
    return new LcEntrySelectValue(
      this.id,
      this.lcEntry,
      this.mediaId,
      this.labelIteration,
      this.timestamp,
      this.labeler,
      this.children.map(c => c.clone()) as LcEntryClassificationValue[],
      this.lcEntryValueParentId,
      this.valid,
      this.selectKey
    );
  }

  public withChange(change: LcEntrySelectChange): LcEntrySelectValue {
    const clone = this.clone() as LcEntrySelectValue;
    if (!!change) {
      clone.selectKey = change.selectKey;
    }
    return clone;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntrySelectValue): void {
    this.selectKey = source.selectKey;
  }

  protected isThisEntryValid(): boolean {

    if (!this.isRequired) {
      return true;
    }

    return this.selectKey !== undefined && this.selectKey !== '';
  }
}

