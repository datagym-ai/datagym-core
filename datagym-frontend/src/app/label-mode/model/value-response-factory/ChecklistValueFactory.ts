import {LcEntry} from '../import';
import {LcEntryClassification} from '../../../label-config/model/classification/LcEntryClassification';
import {LcEntryChecklistValue} from '../classification/LcEntryChecklistValue';
import {LcEntryChecklistChange} from '../change/LcEntryChecklistChange';


export class ChecklistValueFactory {

  public static create(data: {[key: string]: any} , lcEntry: LcEntry): LcEntryChecklistValue {
    const checklistValue = new LcEntryChecklistValue(
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
      data.checkedValues || []
    );
    if (!(lcEntry as LcEntryClassification).required) {
      checklistValue.valid = true;
    }

    return checklistValue;
  }

  public static change(data: {[key: string]: any}): LcEntryChecklistChange {
    return new LcEntryChecklistChange(
      // shared attributes
      data.id,
      data.frameNumber,
      data.frameType || data.type,
      (data as LcEntryChecklistChange).checkedValues
    );
  }
}
