import {LcEntry, LcEntryType} from '../import';
import {LcEntryValue} from '../LcEntryValue';
import {LcEntryContainer} from './LcEntryContainer';

import {ChecklistValueFactory} from './ChecklistValueFactory';
import {LcEntrySelectFactory} from './LcEntrySelectFactory';
import {LcEntryLineFactory} from './LcEntryLineFactory';
import {LcEntryTextFactory} from './LcEntryTextFactory';
import {LcEntryPointFactory} from './LcEntryPointFactory';
import {LcEntryPolyFactory} from './LcEntryPolyFactory';
import {LcEntryRectangleFactory} from './LcEntryRectangleFactory';
import {LcEntryImageSegmentationFactory} from './LcEntryImageSegmentationFactory';
import {LcEntryChange} from '../change/LcEntryChange';


export class LcEntryValueFactory {

  public static castFromObject(data: any, lcEntry: LcEntry): LcEntryValue {
    if (lcEntry === undefined) {
      return undefined;
    }
    data.parent = data.parent || data.lcEntryValueParentId;
    const obj = LcEntryValueFactory.create(data, lcEntry);

    obj.change = [];
    if (!!data.change && Array.isArray(data.change) && data.change.length > 0) {
      obj.change = data.change
        .map(change => LcEntryValueFactory.change(obj.kind, change))
        // Image segmentations support no change objects.
        .filter(c => !!c);

      obj.change.sort((a, b): -1|0|1 =>
        a.frameNumber < b.frameNumber ? -1 : a.frameNumber === b.frameNumber ? 0 : 1
      );
    }

    // handle children recursive.
    if (!!data.children && data.children.length > 0) {
      const container = new LcEntryContainer(lcEntry.children);
      obj.children = data.children.map(child => {
        child.parent = obj.id;
        return LcEntryValueFactory.castFromObject(child, container.findRecursiveById(child.lcEntryId));
      });
    }
    return obj;
  }

  /**
   * @deprecated Use 'LcEntryChangeFactory' instead.
   *
   * @param type
   * @param data
   */
  private static change(type: LcEntryType, data: LcEntryChange): LcEntryChange {
    let ret: LcEntryChange;

    switch (type) {
      case LcEntryType.CHECKLIST:
        ret = ChecklistValueFactory.change(data);
        break;
      case LcEntryType.SELECT:
        ret = LcEntrySelectFactory.change(data);
        break;
      case LcEntryType.FREE_TEXT:
        ret = LcEntryTextFactory.change(data);
        break;

      case LcEntryType.LINE:
        ret = LcEntryLineFactory.change(data);
        break;
      case LcEntryType.POINT:
        ret = LcEntryPointFactory.change(data);
        break;
      case LcEntryType.POLYGON:
        ret = LcEntryPolyFactory.change(data);
        break;
      case LcEntryType.RECTANGLE:
        ret = LcEntryRectangleFactory.change(data);
        break;
    }

    return ret;
  }

  private static create(data: { [key: string]: any }, lcEntry: LcEntry): LcEntryValue {
    let obj: LcEntryValue;
    switch (lcEntry.type) {
      case LcEntryType.CHECKLIST:
        obj = ChecklistValueFactory.create(data, lcEntry);
        break;
      case LcEntryType.SELECT:
        obj = LcEntrySelectFactory.create(data, lcEntry);
        break;
      case LcEntryType.FREE_TEXT:
        obj = LcEntryTextFactory.create(data, lcEntry);
        break;
      case LcEntryType.LINE:
        obj = LcEntryLineFactory.create(data, lcEntry);
        break;
      case LcEntryType.POINT:
        obj = LcEntryPointFactory.create(data, lcEntry);
        break;
      case LcEntryType.POLYGON:
        obj = LcEntryPolyFactory.create(data, lcEntry);
        break;
      case LcEntryType.RECTANGLE:
        obj = LcEntryRectangleFactory.create(data, lcEntry);
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        obj = LcEntryImageSegmentationFactory.create(data, lcEntry);
        break;
    }
    return obj;
  }
}
