import {LcEntryValue} from '../LcEntryValue';
import {LcEntryChange} from '../change/LcEntryChange';


type WithRequired = {readonly required: boolean};

export abstract class LcEntryClassificationValue<C extends LcEntryChange = LcEntryChange> extends LcEntryValue<C> {

  public get isRequired(): boolean {
    // this.lcEntry is of type LcEntryClassification with required flag.
    // Just to reduce module dependencies the WithRequired type is used.
    return !!(this.lcEntry as unknown as WithRequired).required;
  }
}
