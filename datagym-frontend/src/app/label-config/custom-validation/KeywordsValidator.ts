import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';


export class KeywordsValidator {

  public static forbidden(keywords: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!KeywordsValidator.isForbidden(control, keywords)) {
        return null;
      }
      const payload = {forbidden: keywords, actualValue: control.value};
      return {forbidden: payload};
    };
  }

  public static duplicateExportKey(usedExportKeys: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!KeywordsValidator.isForbidden(control, usedExportKeys)) {
        return null;
      }
      const payload = {forbidden: usedExportKeys, actualValue: control.value};
      return {duplicateExportKey: payload};
    };
  }

  private static isForbidden(control: AbstractControl, values: string[]): boolean {

    if (!control.touched && !control.value) {
      return false;
    }
    if (values.length === 0 ) {
      return false;
    }
    const value = control.value as string;
    return values.includes(value);
  }

}
