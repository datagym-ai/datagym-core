import {AfterViewInit, Component, ElementRef, Input, Optional, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, FormControl, NgControl, ValidationErrors} from '@angular/forms';
import {ErrorHandlers} from '../dg-input-errors/dg-input-errors.component';


@Component({
  selector: 'app-textfield',
  templateUrl: './textfield.component.html',
  styleUrls: ['./textfield.component.css']
})
export class TextfieldComponent implements ControlValueAccessor, AfterViewInit {
  @Input('label')
  public labelText: string;
  @Input('title')
  public titleText: string = '';
  @Input('placeholder')
  public placeholderText: string = '';
  @Input('hint')
  public hintText: string;
  @Input('required')
  public requiredField: boolean = false;
  @Input('editable')
  public editable: boolean = false;
  @Input('controlName')
  public formControlName: FormControl;
  @Input('autofocus')
  public autofocus: boolean = false;
  @Input('autocomplete')
  public autocomplete: boolean = false;
  @Input('type')
  public type: string = 'text';
  @Input('min')
  public min: number;
  @Input('max')
  public max: number;
  @Input() // set the label's font size small
  public small: boolean = false;
  public internalModel: string = '';
  @ViewChild('input', { static: true, read: NgControl })
  private inputNgControl: NgControl;
  @ViewChild('input', { static: true, read: ElementRef })
  public inputElementRef: ElementRef;
  @Input()
  public errorHandlers: ErrorHandlers = {};

  // not from parent or ancestors.
  constructor(@Self() @Optional() public ngControl: NgControl) {
    if (ngControl !== undefined) {
      this.ngControl.valueAccessor = this;
    }
  }
  get hasErrors(): boolean {
    return !!this.errors && Object.keys(this.errors).length > 0;
  }

  get errors(): ValidationErrors | null {
    return this.ngControl.errors;
  }

  /**
   * ControlValueAccessor callbacks.
   * Note that this doesn't equal to registered (change)-handlers, etc.
   * @param _
   */
  public onChange = (_: any) => {
  };

  // Retrieve the dependency only from the local injector,

  public onTouched = () => {
  };

  ngAfterViewInit(): void {
    // Set the input validator to the same as the control validator
    this.inputNgControl.control.setValidators(this.ngControl.control.validator);
  }


  writeValue(value: string): void {
    this.internalModel = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
