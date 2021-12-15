import {AbstractControlOptions, ValidatorFn} from '@angular/forms';

type validatorTypes = ValidatorFn | ValidatorFn[] | AbstractControlOptions | null;

export class FilterBase<T> {
  colSize: number;
  value: T;
  placeholder: string;
  key: string;
  controlType: string;
  validators: validatorTypes = null;

  constructor(options: {
    colSize?: number,
    value?: T,
    placeholder?: string,
    key?: string,
    controlType?: string,
    validators?: validatorTypes
  } = {}) {
    this.colSize = options.colSize;
    this.value = options.value;
    this.placeholder = options.placeholder;
    this.key = options.key || '';
    this.controlType = options.controlType || '';
    this.validators = options.validators || null;
  }
}
