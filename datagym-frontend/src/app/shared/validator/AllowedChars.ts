import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';


export class AllowedChars {

  /**
   * Note: this validator expects some regex pattern of form ^(<allAllowedCharacterClasses>)$
   *
   * E.g. a match group between start and end tag.
   *
   * This validator cannot be used to validate the input length.
   *
   * @param allowed
   * @param flags
   */
  public static pattern(allowed: string, flags: string | string[] = ['ig']): ValidatorFn {
    flags = Array.isArray(flags) ? flags.join('') : flags;

    const trimLeft = (input: string, remove: string): string => {
      return input.startsWith(remove) ? input.slice(remove.length) : input;
    }

    const trimRight = (input: string, remove: string): string => {
      return input.endsWith(remove) ? input.slice(0, -remove.length) : input;
    }

    /*
     * Remove from allowed-string:
     * - '^' at the front means 'starts with'
     * - '$' from the end, means: 'ends with'
     */
    const rep = trimLeft(trimRight(allowed, '$'), '^');

    const replacer = new RegExp(rep, flags);
    const pattern = new RegExp(allowed, flags);

    return (control: AbstractControl): ValidationErrors | null => {

      const value = control.value as string;

      if (!/*not*/!!value) {
        return null;
      }

      const tmp = value.replace(pattern, '');
      if (!/*not*/!!tmp) {
        return null;
      }

      // Get all forbidden chars and make that unique.
      const forbidden = [...value.replace(replacer, '')];
      const forbiddenChars = [...new Set(forbidden)].join(', ');

      const length = forbiddenChars.length;

      return length === 1
        ? {forbiddenChar: {forbidden: forbiddenChars, actualValue: control.value}}
        : {forbiddenChars: {forbidden: forbiddenChars, actualValue: control.value}};
    };

  }
}
