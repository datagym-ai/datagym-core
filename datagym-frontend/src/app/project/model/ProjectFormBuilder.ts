import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Project } from './Project';
import { MediaType } from './MediaType.enum';
import {AllowedChars} from '../../shared/validator/AllowedChars';
import {RestrictionUtility} from '../../shared/validator/RestrictionUtility';

/**
 * Create the form group to create / update a project here
 * to avoid duplicate code and make sure the validator rules
 * take affect in all used places.
 */
export class ProjectFormBuilder {

  public static create(project?: Project): FormGroup {

    const isDummy = Project.isDummy(project);
    const owner = !!project && !!project.owner ? project.owner : null;
    const name = !!project && !!project.name ? project.name : null;
    const description = !!project && !!project.description ? project.description : null;
    const shortDescription = !!project && !!project.shortDescription ? project.shortDescription : null;
    const mediaType = !!project && !!project.mediaType ? project.mediaType : MediaType.IMAGE;

    return new FormGroup({
      'projectOwner': new FormControl({value: owner, disabled: isDummy}, [
        Validators.required
      ]),
      'projectName': new FormControl({value: name, disabled: isDummy}, [
        Validators.required,
        AllowedChars.pattern(RestrictionUtility.project.name.pattern),
        Validators.maxLength(RestrictionUtility.project.name.maxLength)
      ]),
      'projectShortDescription': new FormControl({value: shortDescription, disabled: isDummy}, [
        Validators.required,
        AllowedChars.pattern(RestrictionUtility.project.description.pattern),
        Validators.maxLength(RestrictionUtility.project.description.maxLength)
      ]),
      'projectDescription': new FormControl({value: description, disabled: isDummy}, [
        AllowedChars.pattern(RestrictionUtility.project.fullDescription.pattern),
        Validators.maxLength(RestrictionUtility.project.fullDescription.maxLength)
      ]),
      'mediaType': new FormControl({value: mediaType, disabled: !!project}, Validators.required)
    });
  }
}
