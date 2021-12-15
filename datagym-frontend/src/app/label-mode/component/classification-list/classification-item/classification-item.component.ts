import {Component, Input, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {LcEntryClassification} from '../../../../label-config/model/classification/LcEntryClassification';
import {FormArray, FormGroup} from '@angular/forms';
import {LcEntryType} from '../../../../label-config/model/LcEntryType';
import {HasOptionsMap} from '../../../../label-config/model/classification/HasOptionsMap';
import {Subject} from 'rxjs';
import {LcEntryTextValue} from '../../../model/classification/LcEntryTextValue';
import {LcEntrySelectValue} from '../../../model/classification/LcEntrySelectValue';
import {LcEntryChecklistValue} from '../../../model/classification/LcEntryChecklistValue';
import {EntryValueService} from '../../../service/entry-value.service';
import {LcEntryText} from '../../../../label-config/model/classification/LcEntryText';
import {LabelModeUtilityService} from '../../../service/label-mode-utility.service';
import {LcEntryClassificationValue} from '../../../model/classification/LcEntryClassificationValue';
import {takeUntil} from 'rxjs/operators';


@Component({
  selector: 'app-classification-item',
  templateUrl: './classification-item.component.html',
  styleUrls: ['./classification-item.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ClassificationItemComponent implements OnInit, OnDestroy {
  @Input('classification')
  public classification: LcEntryClassification;
  @Input('value')
  public value: LcEntryClassificationValue;
  @Input('nestingLevel')
  public nestingLevel: number;
  @Input('group')
  public formGroup: FormGroup;
  public isCollapsed: boolean = false;
  public LcEntryType = LcEntryType;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  /**
   * Store here the latest updated text area value to avoid
   * sending two times the same content to the backend.
   *
   * @private
   */
  private lastUpdatedTextValue: string = undefined;

  constructor(private eVService: EntryValueService, public labelModeUtilityService: LabelModeUtilityService) {
  }

  public get children(): LcEntryClassificationValue[] {
    return this.value.getNestedClassifications();
  }

  public get classificationOptions(): LcEntryClassification & HasOptionsMap {
    return (this.classification as LcEntryClassification & HasOptionsMap);
  }

  /**
   * Object.keys() is not supported within the template file and
   * the size property does not work so count here the available options.
   */
  public get classificationOptionsSize(): number {
    return Object.keys(this.classificationOptions.options).length;
  }

  public get maxLength(): number | null {
    // this is only available on this.classification.type === LcEntryType.FREE_TEXT
    return (this.classification as LcEntryText).maxLength || null;
  }

  public textAreaRows: number = 3;

  public get answers(): FormGroup {
    return this.formGroup.get('answers') as FormGroup;
  }

  private get childrenFormArray(): FormArray {
    return this.formGroup.get('children') as FormArray;
  }

  private get rootValueId(): string | null {
    const selectedEntryValueIds = this.labelModeUtilityService.selectedEntryValueIds;
    if (selectedEntryValueIds.length !== 1) {
      return null;
    }
    return selectedEntryValueIds[0];
  }

  ngOnInit() {
    if (this.classification.type !== LcEntryType.FREE_TEXT) {
      // Listen to changes to submit the value to the server without a submit button
      this.answers.valueChanges.pipe(takeUntil(this.unsubscribe)).subscribe(selectedValue => {
        if (this.value.lcEntry.type !== LcEntryType.CHECKLIST) {
          this.submitValue(selectedValue);
        } else {
          this.submitMultipleValues(selectedValue);
        }
      });
    }

    /**
     * On a text value set the 'lastUpdatedTextValue' so the
     * `updateTextValue` method also respects the value at start
     * and does not
     */
    if (this.classification.type === LcEntryType.FREE_TEXT) {
      this.textAreaRows = this.calculateTextAreaRows();
      this.lastUpdatedTextValue = (this.value as LcEntryTextValue).text;
    }
  }

  ngOnDestroy(): void {

    if (this.classification.type === LcEntryType.FREE_TEXT) {
      this.updateTextValue();
    }

    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public updateTextValue() {
    // LcEntryType.FREE_TEXT values will by updated via (change) event instead of
    // valueChanges to avoid timing issues.
    if (this.value.isDeleted) {
      // While the delete request is running
      // we cannot update the text value.
      return;
    }

    const key = this.classification.entryKey;
    const text = this.formGroup.get(key).value as string || '';
    if (this.lastUpdatedTextValue === text) {
      // nothing had changed, nothing to do.
      return;
    }
    this.lastUpdatedTextValue = text;
    (this.value as LcEntryTextValue).text = text;
    if (this.classification.required) {
      this.value.valid = this.formGroup.get(key).valid;
    }
    this.eVService.updateClassificationValue(this.value, this.rootValueId);
  }

  public getValueLength(key: string): number {
    return !!this.formGroup.get(key) && !!this.formGroup.get(key).value
      ? this.formGroup.get(key).value.length
      : 0;
  }

  public reachedMaxLength(key: string): boolean {
    // maxLength is only set if the classification is for LcEntryType.FREE_TEXT
    return !!this.maxLength && this.maxLength === this.getValueLength(key);
  }

  public getClassificationByValue(value: LcEntryClassificationValue): LcEntryClassification {
    return value.lcEntry as LcEntryClassification;
  }

  public toggleFold(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  public getChildrenFormGroupByIndexAndName(index: number, name: string): FormGroup {
    return this.childrenFormArray.get([index, name]) as FormGroup;
  }

  /**
   * Calculate the text area rows depending on the maxLength field.
   * @private
   */
  private calculateTextAreaRows(): number {
    const textFieldMaxLength = 255;

    const setting = [
      {max: 15, rows: 1},
      {max: 30, rows: 2},
      {max: textFieldMaxLength, rows: 3},
    ];

    // this is only available on this.classification.type === LcEntryType.FREE_TEXT
    const maxLength = (this.classification as LcEntryText).maxLength || textFieldMaxLength;

    const filtered = setting.filter(s => maxLength <= s.max);
    const rows = filtered.map(f => f.rows);
    return Math.min(...rows);
  }

  /**
   * Takes a selectedValue and sets it on the LcEntryValue Object. Validity is checked on the control to avoid timing
   * issues with pending validity
   * @param selectedValue
   */
  private submitValue(selectedValue: string): void {
    switch (this.classification.type) {
      case LcEntryType.SELECT:
        (this.value as LcEntrySelectValue).selectKey = selectedValue;
        if (this.classification.required) {
          this.value.valid = this.answers.valid;
        }
        this.eVService.updateClassificationValue(this.value, this.rootValueId);
        break;
      default:
        break;
    }
  }

  /**
   * Takes an array of selectedValues for checkboxes with key and value and sets the value and validity if at
   * least one was checked
   * @param selectedValues
   */
  private submitMultipleValues(selectedValues: { [key: string]: boolean }[]): void {
    let trueCounter: number = 0;
    Object.keys(selectedValues).forEach((key: string) => {
      const index = (this.value as LcEntryChecklistValue).checkedValues.indexOf(key);
      if (selectedValues[key] === true) {
        trueCounter++;
        if (index === -1) {
          (this.value as LcEntryChecklistValue).checkedValues.push(key);
        }
      } else {
        if (index >= 0) {
          (this.value as LcEntryChecklistValue).checkedValues.splice(index, 1);
        }
      }
    });
    if (this.classification.required) {
      this.value.valid = trueCounter > 0;
    }
    this.eVService.updateClassificationValue(this.value, this.rootValueId);
  }
}
