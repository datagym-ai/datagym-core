import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

export interface SelectOption {
  readonly id: string;
  readonly label: string;
}

@Component({
  selector: 'app-dg-select',
  templateUrl: './dg-select.component.html',
  styleUrls: ['./dg-select.component.css']
})
export class DgSelectComponent implements OnInit {

  @Input()
  private preselectIndex: number = 0;

  @Input()
  public options: SelectOption[] = [];

  @Output()
  public onChange: EventEmitter<string> = new EventEmitter<string>();

  public formGroup = new FormGroup({
    'options': new FormControl()
  });

  constructor() { }

  ngOnInit(): void {
    // Preselect the element at the given index.
    const index = this.preselectIndex;
    if (this.options.length > index) { // length = max.index + 1
      this.formGroup.controls['options'].reset(this.options[index].id);
      this.onChange.emit(this.options[index].id);
    }
  }

  public changed($event: unknown): void {
    this.onChange.emit($event as string);
  }
}
