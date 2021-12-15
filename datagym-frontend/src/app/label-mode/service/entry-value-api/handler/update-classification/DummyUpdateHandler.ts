import {UpdateHandler} from './UpdateHandler';
import {LcEntryClassificationValue} from '../../../../model/classification/LcEntryClassificationValue';


export class DummyUpdateHandler extends UpdateHandler {

  public constructor() {
    super(null);
  }

  public updateClassification(value: LcEntryClassificationValue): void {
    // Have some break. Here is nothing to do.
  }
}
