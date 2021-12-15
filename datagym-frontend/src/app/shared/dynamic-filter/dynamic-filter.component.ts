import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {FilterBase} from './FilterBase';
import {FormGroup} from '@angular/forms';
import {Subject} from 'rxjs';
import {debounceTime, filter} from 'rxjs/operators';
import {FilterControlService} from './FilterControlService';

@Component({
  selector: 'app-dynamic-filter',
  templateUrl: './dynamic-filter.component.html'
})
export class DynamicFilterComponent implements OnInit, OnDestroy, OnChanges {

  @Input()
  disabledFilterComponent: boolean;

  @Input()
  filterConfiguration: FilterBase<any>[] = [];

  @Output()
  dynamicFilterChangeEvent: EventEmitter<FormGroup> = new EventEmitter<FormGroup>();

  public dynamicFilterChanged = false;

  public formGroup: FormGroup;

  public subDebouncer: Subject<FormGroup> = new Subject<FormGroup>();

  constructor(private filterControlService: FilterControlService) {}

  ngOnInit() {
    this.formGroup = this.filterControlService.toFormGroup(this.filterConfiguration);

    // Debounce here because filter may change fast and reset/disable may trigger other change listeners.
    const debounceMs = 750;
    this.subDebouncer.pipe(
      debounceTime(debounceMs),
      filter(_ => this.formGroup.valid)
    ).subscribe(value => {
      this.dynamicFilterChangeEvent.emit(value);
    });
  }

  ngOnDestroy() {
    this.subDebouncer.unsubscribe();
  }

  public resetForm() {
    this.filterControlService.resetFormGroup(this.formGroup, this.filterConfiguration);
    this.subDebouncer.next(this.formGroup);
    this.dynamicFilterChanged = false;
  }

  public emitFilterChangedEvent() {
    this.subDebouncer.next(this.formGroup);
    this.dynamicFilterChanged = true;
  }

  public ngOnChanges(changes: SimpleChanges): void {

    if (changes.disabledFilterComponent && this.formGroup && this.formGroup.controls) {
      if (changes.disabledFilterComponent.currentValue) {
        Object.keys(this.formGroup.controls).forEach(key => {
          this.formGroup.get(key).disable();
        });
      } else {
        Object.keys(this.formGroup.controls).forEach(key => {
          this.formGroup.get(key).enable();
        });
      }
    }
  }
}
