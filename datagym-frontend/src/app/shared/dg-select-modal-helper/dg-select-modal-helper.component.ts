import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SelectOption} from '../dg-select/dg-select.component';

@Component({
  selector: 'app-dg-select-modal-helper',
  templateUrl: './dg-select-modal-helper.component.html',
  styleUrls: ['./dg-select-modal-helper.component.css']
})
export class DgSelectModalHelperComponent implements OnInit {

  @Input()
  public title: string = '';
  @Input()
  public description: string = '';
  @Input()
  public submitLabel: string = '';
  @Input()
  public options: SelectOption[] = [];

  @Output()
  public onChange: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  public onCancel: EventEmitter<void> = new EventEmitter<void>();

  @Output()
  public onSubmit: EventEmitter<void> = new EventEmitter<void>();

  constructor() { }

  ngOnInit(): void {
  }
}
