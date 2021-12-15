import {LcEntry, LcEntryClassification} from '../import';
import {LcEntryTextValue} from '../classification/LcEntryTextValue';
import {LcEntryTextChange} from '../change/LcEntryTextChange';


export class LcEntryTextFactory {
  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryTextValue {
    const text = new LcEntryTextValue(
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
      data.text || ''
    );
    if (!(lcEntry as LcEntryClassification).required) {
      text.valid = true;
    }

    return text;
  }

  public static change(data: {[key: string]: any}): LcEntryTextChange {
    return new LcEntryTextChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      // custom attributes
      (data as LcEntryTextChange).text
    );
  }
}
