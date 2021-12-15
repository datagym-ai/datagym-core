import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-dg-slider',
  templateUrl: './dg-slider.component.html',
  styleUrls: ['./dg-slider.component.css']
})
export class DgSliderComponent implements OnInit {

  /**
   * Set the start value.
   */
  @Input('value')
  public value: number = 0;

  @Output()
  valueChange: EventEmitter<number> = new EventEmitter<number>();

  /**
   * ngOnInit sets this property to the value of 'value' if not set.
   * This is used with the 'reset' button to restore the default.
   */
  @Input('default')
  public defaultValue: number;

  @Input('min')
  public min: number = 0;

  @Input('max')
  public max: number;

  @Input('step')
  public step: string | number = 'any';

  @Input('hasResetButton')
  public hasResetButton: boolean = true;

  @Input()
  public title: string;

  @Input()
  public label: string = '';

  // return value with 2 decimal place
  get rounded(): number {
    const number = this.value;
    const roundFactor = 100;
    return Math.round( number * roundFactor );
  }

  /**
   * Cast the value from string to number & emit the onChange event.
   * @param val
   */
  set setValue(val: string|number) {
    this.value = +val || 0;
    this.valueChange.emit(this.value);
  }

  constructor() { }

  ngOnInit() {
    // Make sure the given value is a number.
    this.value = typeof this.value === 'number'
      ? this.value
      : 0;

    // Make sure the default value is set. Use the value as default.
    this.defaultValue = this.defaultValue !== undefined && typeof this.defaultValue === 'number'
      ? this.defaultValue
      : this.value;
  }
}
