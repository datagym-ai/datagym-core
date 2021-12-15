import {LcEntryValueUpdateBindingModel} from './LcEntryValueUpdateBindingModel';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {LcEntryChecklistValue} from './classification/LcEntryChecklistValue';
import {LcEntryTextValue} from './classification/LcEntryTextValue';
import {LcEntrySelectValue} from './classification/LcEntrySelectValue';
import {LcEntryPolyValue} from './geometry/LcEntryPolyValue';
import {LcEntryPointValue} from './geometry/LcEntryPointValue';
import {LcEntryRectangleValue} from './geometry/LcEntryRectangleValue';
import {LcEntryImageSegmentationValue} from './geometry/LcEntryImageSegmentationValue';
import {LcEntryGeometryValue} from './geometry/LcEntryGeometryValue';
import {LcEntryLineValue} from './geometry/LcEntryLineValue';
import {LcEntryClassificationValue} from './classification/LcEntryClassificationValue';


export class LcEntryValueUpdateFactory {

  private constructor(private readonly labelTaskId: string) {}

  /**
   * @constructor
   * @param labelTaskId
   */
  public static prepare(labelTaskId: string): LcEntryValueUpdateFactory {
    return new LcEntryValueUpdateFactory(labelTaskId);
  }

  public classification(value: LcEntryClassificationValue): LcEntryValueUpdateBindingModel {
    // May the delete request is running.
    if (value.isDeleted) {
      return undefined;
    }

    const tmp: LcEntryValueUpdateBindingModel =
      new LcEntryValueUpdateBindingModel(value.id, value.lcEntryId, value.lcEntryValueParentId, value.valid, this.labelTaskId);
    switch (value.lcEntry.type) {
      case LcEntryType.CHECKLIST:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryChecklistValue).checkedValues =
          (value as LcEntryChecklistValue).checkedValues;
        break;
      case LcEntryType.FREE_TEXT:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryTextValue).text = (value as LcEntryTextValue).text;
        break;
      case LcEntryType.SELECT:
        (tmp as LcEntryValueUpdateBindingModel & LcEntrySelectValue).selectKey =
          (value as LcEntrySelectValue).selectKey;
        break;
      default:
        // Precaution, should not be possible
        break;
    }
    return tmp;
  }

  public geometry(value: LcEntryGeometryValue): LcEntryValueUpdateBindingModel {
    // May the delete request is running.
    if (value.isDeleted) {
      return undefined;
    }

    const tmp: LcEntryValueUpdateBindingModel = new LcEntryValueUpdateBindingModel(
      value.id, value.lcEntryId, value.lcEntryValueParentId, value.valid, this.labelTaskId, value.comment
    );

    switch (value.kind) {
      case LcEntryType.POLYGON:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryPolyValue).points = (value as LcEntryPolyValue).points;
        break;
      case LcEntryType.POINT:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryPointValue).x = (value as LcEntryPointValue).x;
        (tmp as LcEntryValueUpdateBindingModel & LcEntryPointValue).y = (value as LcEntryPointValue).y;
        break;
      case LcEntryType.LINE:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryLineValue).points = (value as LcEntryLineValue).points;
        break;
      case LcEntryType.RECTANGLE:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryRectangleValue).height = (value as LcEntryRectangleValue).height;
        (tmp as LcEntryValueUpdateBindingModel & LcEntryRectangleValue).width = (value as LcEntryRectangleValue).width;
        (tmp as LcEntryValueUpdateBindingModel & LcEntryRectangleValue).y = (value as LcEntryRectangleValue).y;
        (tmp as LcEntryValueUpdateBindingModel & LcEntryRectangleValue).x = (value as LcEntryRectangleValue).x;
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        (tmp as LcEntryValueUpdateBindingModel & LcEntryImageSegmentationValue).pointsCollection =
          (value as LcEntryImageSegmentationValue).pointsCollection;
        break;
      default:
        // Precaution, should not be possible
        break;
    }

    return tmp;
  }

}
