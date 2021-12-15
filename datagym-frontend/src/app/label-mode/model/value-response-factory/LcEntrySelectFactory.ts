import {LcEntry, LcEntryClassification} from '../import';
import {LcEntrySelectValue} from '../classification/LcEntrySelectValue';
import {LcEntrySelectChange} from '../change/LcEntrySelectChange';


export class LcEntrySelectFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntrySelectValue {
    const selectValue = new LcEntrySelectValue(
      // shared attributes
      data.id,
      lcEntry,
      data.mediaId,
      data.labelIterationId,
      data.timestamp,
      data.labeler,
      [],
      data.parent,
      data.valid,
      // custom attributes
      data.selectKey || ''
    );
    if (!(lcEntry as LcEntryClassification).required) {
      selectValue.valid = true;
    }

    return selectValue;
  }

  public static change(data: {[key: string]: any}): LcEntrySelectChange {
    return new LcEntrySelectChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      (data as LcEntrySelectChange).selectKey
    );
  }
}
