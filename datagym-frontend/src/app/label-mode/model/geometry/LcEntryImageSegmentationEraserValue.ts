import {LcEntryImageSegmentationValue} from './LcEntryImageSegmentationValue';
import {LcEntryGeometry} from '../../../label-config/model/geometry/LcEntryGeometry';
import {LcEntryType} from '../import';

export class LcEntryImageSegmentationEraserValue extends LcEntryImageSegmentationValue {
  readonly kind = LcEntryType.IMAGE_SEGMENTATION_ERASER;

  constructor(rootGeo: LcEntryGeometry, mediaId: string, labelIterationId) {
    super(rootGeo.id, rootGeo, mediaId, labelIterationId, 0, null, [], null, true, [], '');
  }

}
