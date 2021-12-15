import {AbstractControl, FormArray, ValidationErrors, ValidatorFn} from '@angular/forms';

/**
 * This validator is used to validate all answers / options
 * of lcEntries in edit mode.
 */
export class EditEntryDuplicateValidator {

  /**
   * This validator is not a usual form validator. It relays on the design of the
   * EditEntryComponents form group. It loops over all 'answers' within the
   * 'entryProperties' form array and compares all {{name}} entries within that
   * array. It flags *all* duplicate entries within that form array as invalid.
   * It also removes the error flag from *all* inputs even if only one of the
   * values was changed.
   *
   * @param name the name can be 'key' or 'value'.
   * @param errorMessage the error message to translate and display to the user.
   */
  public static validate(name: string, errorMessage: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

      const answerFormArray = control.get('entryProperties').get('answers') as FormArray;
      const namedAnswers = answerFormArray.controls.map(c => c.get(name));

      const values = namedAnswers
        .filter(c => !!c.value)
        .map(c => c.value);

      const duplicates = EditEntryDuplicateValidator.extractDuplicates(values);

      // reset duplicate errors.
      namedAnswers
        .filter(c => c.hasError('duplicate'))
        .forEach(c => {
          const e = c.errors;
          delete e['duplicate'];
          c.setErrors(!!e && Object.keys(e).length > 0 ? e : null);
          c.markAsDirty();
        });

      // set duplicate errors.
      namedAnswers
        .filter(c => !!c.value && duplicates.includes(c.value))
        .forEach(c => {
          c.setErrors({...c.errors, duplicate: errorMessage});
          c.markAsTouched({onlySelf: true});
          c.markAsDirty({onlySelf: true});
      });

      return duplicates.length > 0 ? {duplicate: errorMessage} : null;
    };
  }


  /**
   * Store all duplicates from entryKeys in this.duplicates
   */
  private static extractDuplicates(values: string[]): string[] {
    const tmp: string[] = [];
    const duplicates: string[] = [];
    values.forEach(k => {
      if (!!k && tmp.indexOf(k) !== -1) {
        duplicates.push(k);
      }
      tmp.push(k);
    });

    return duplicates;
  }
}
