import { Injectable } from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {FilterBase} from './FilterBase';
import {HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FilterControlService {

  private labelerText: string = '';

  constructor() { }

  toFormGroup(filterElements: FilterBase<any>[]): FormGroup{
    const formGroup: {[key: string]: FormControl} = {};
    filterElements.forEach(filterElement => {
      formGroup[filterElement.key] = new FormControl(filterElement.value || '', filterElement.validators);
    });
    return new FormGroup(formGroup);
  }

  public resetFormGroup(formGroup: FormGroup, filterElements: FilterBase<any>[]): void {
    formGroup.reset();
    // Reinitialize values (especially for select-box)
    filterElements.forEach(filterElement => {
      formGroup.get(filterElement.key).setValue(filterElement.value || '');
    });
  }

  public requestLabelerFilter (formGroup: FormGroup): string {
    Object.keys(formGroup.controls).forEach( control => {
      if ( control === 'search_labeler') {
        this.labelerText = formGroup.get(control).value.toString();
      }
    });
    return this.labelerText;
  }

  public buildHttpParams(formGroup: FormGroup): HttpParams {
    let urlParams: HttpParams = new HttpParams();

    Object.keys(formGroup.controls).forEach(control => {
      const formValue = formGroup.get(control).value;
      if (!!formValue) {
        urlParams = urlParams.append(control, formValue);
      }
    });

    return urlParams;
  }
}
