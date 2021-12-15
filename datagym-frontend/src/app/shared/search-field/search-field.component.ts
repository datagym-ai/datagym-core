import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SearchFieldComponent),
      multi: true
    }
  ]
})
export class SearchFieldComponent implements OnInit, ControlValueAccessor {

  @Input()
  public readonly = false;

  public val: string = '';

  @Input()
  public placeholder: string = '';
  @Input()
  public label: string = '';

  /**
   * ControlValueAccessor callbacks.
   * Note that this doesn't equal to registered (change)-handlers, etc.
   * @param _
   */
  private onChange = (_: any) => {
  };
  private onTouched = () => {
  };

  public set value(val){
    if( val !== undefined && this.val !== val){
      this.val = val;
      this.onChange(val);
      this.onTouched();
    }
  }

  ngOnInit(): void {
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    isDisabled ? this.readonly = true : this.readonly = false;
  }

  writeValue(obj: any): void {
    this.value = obj;
  }

}
