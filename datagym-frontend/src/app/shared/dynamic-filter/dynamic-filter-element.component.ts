import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {FilterBase} from './FilterBase';
import {FilterDropdown} from './FilterDropdown';
import {FilterTextbox} from './FilterTextbox';


@Component({
  selector: 'app-dynamic-filter-element',
  templateUrl: './dynamic-filter-element.component.html'
})
export class DynamicFilterElement implements OnInit {
  @Input()
  filterElement: FilterBase<any>;
  @Input()
  formGroup: FormGroup;
  @Input()
  disabled: boolean;

  get filterElementWithAutocomplete(): FilterTextbox {
    return this.filterElement as FilterTextbox;
  }

  get filterElementWithOptions(): FilterDropdown {
    return this.filterElement as FilterDropdown;
  }

  @Output()
  filterElementChanged = new EventEmitter<any>();

  ngOnInit(): void {}

  public elementChanged(event: any) {
    this.filterElementChanged.emit(event);
  }
}
