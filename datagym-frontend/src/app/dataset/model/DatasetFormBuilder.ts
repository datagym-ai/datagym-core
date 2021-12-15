import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DatasetDetail } from './DatasetDetail';
import { MediaType } from '../../project/model/MediaType.enum';
import {AllowedChars} from '../../shared/validator/AllowedChars';
import {RestrictionUtility} from '../../shared/validator/RestrictionUtility';


/**
 * Create the form group to create / update a dataset here
 * to avoid duplicate code and make sure the validator rules
 * take affect in all used places.
 */
export class DatasetFormBuilder {

  public static create(dataset?: DatasetDetail) {

    const isDummy = DatasetDetail.isDummy(dataset);
    const name = !!dataset && !!dataset.name ? dataset.name : null;
    const owner = !!dataset && !!dataset.owner ? dataset.owner : null;
    const shortDescription = !!dataset && !!dataset.shortDescription ? dataset.shortDescription : null;
    const mediaType = !!dataset && !!dataset.mediaType ? dataset.mediaType : MediaType.IMAGE;

    return new FormGroup({
      'datasetOwner': new FormControl({value: owner, disabled: isDummy}, Validators.required),
      'datasetName': new FormControl({value: name, disabled: isDummy}, [
        Validators.required,
        AllowedChars.pattern(RestrictionUtility.dataset.name.pattern),
        Validators.maxLength(RestrictionUtility.dataset.name.maxLength)
      ]),
      'datasetShortDescription': new FormControl({value: shortDescription, disabled: isDummy}, [
        AllowedChars.pattern(RestrictionUtility.dataset.description.pattern),
        Validators.maxLength(RestrictionUtility.dataset.description.maxLength)
      ]),
      'mediaType': new FormControl({value: mediaType, disabled: !!dataset}, Validators.required)
    });
  }

}
