import {LcEntry} from '../../../label-config/model/LcEntry';
import {LabelIteration} from '../LabelIteration';
import {LcEntryClassificationValue} from './LcEntryClassificationValue';
import {LcEntryTextChange} from '../change/LcEntryTextChange';
import {LcEntryType} from '../import';

type WithMaxLength = { readonly maxLength: number };

export class LcEntryTextValue extends LcEntryClassificationValue<LcEntryTextChange> {
  readonly kind = LcEntryType.FREE_TEXT;

  public text: string;

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
    text: string | null
  ) {
    super(id, lcEntry, media, labelIteration, timestamp, labeler, children, parent, valid);
    this.text = text || '';
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  protected eatValues(source: LcEntryTextValue): void {
    this.text = source.text;
  }

  /**
   * Create a fully copy of the current LcEntryRadioValue.
   */
  public createClone(): LcEntryTextValue {
    return new LcEntryTextValue(
      this.id,
      this.lcEntry,
      this.mediaId,
      this.labelIteration,
      this.timestamp,
      this.labeler,
      this.children.map(c => c.clone()) as LcEntryClassificationValue[],
      this.lcEntryValueParentId,
      this.valid,
      this.text
    );
  }

  public withChange(change: LcEntryTextChange): LcEntryTextValue {
    const clone = this.clone() as LcEntryTextValue;
    if (!!change) {
      clone.text = change.text;
    }
    return clone;
  }

  protected isThisEntryValid(): boolean {

    if (!this.isRequired) {
      return true;
    }

    // this.lcEntry is of type LcEntryText but to reduce module
    // dependencies use type WithMaxLength
    return this.text !== undefined
      && this.text.length > 0
      && this.text.length <
      (this.lcEntry as unknown as WithMaxLength).maxLength + 1;
  }
}

