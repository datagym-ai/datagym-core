import {Component, OnDestroy, OnInit} from '@angular/core';
import {LcEntryClassification} from '../../../label-config/model/classification/LcEntryClassification';
import {FormArray, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {LcEntryType} from '../../../label-config/model/LcEntryType';
import {LcEntryText} from '../../../label-config/model/classification/LcEntryText';
import {HasOptionsMap} from '../../../label-config/model/classification/HasOptionsMap';
import {LcEntryChecklist} from '../../../label-config/model/classification/LcEntryChecklist';
import {LcEntryTextValue} from '../../model/classification/LcEntryTextValue';
import {LcEntryChecklistValue} from '../../model/classification/LcEntryChecklistValue';
import {LcEntrySelectValue} from '../../model/classification/LcEntrySelectValue';
import {LabelModeUtilityService} from '../../service/label-mode-utility.service';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LcEntryClassificationValue} from '../../model/classification/LcEntryClassificationValue';


@Component({
  selector: 'app-classification-list',
  templateUrl: './classification-list.component.html',
  styleUrls: ['./classification-list.component.css']
})
export class ClassificationListComponent implements OnInit, OnDestroy {
  public valuesToShow: LcEntryClassificationValue[];
  public entryValueForm: FormGroup;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private fb: FormBuilder, private labelModeUtilityService: LabelModeUtilityService) {
  }

  ngOnInit(): void {
    this.labelModeUtilityService.valueTree$.pipe(takeUntil(this.unsubscribe))
      .subscribe((selectedValueTree: LcEntryClassificationValue[]) => {
        this.valuesToShow = selectedValueTree;
        this.initForm();
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public getClassificationByValue(value: LcEntryClassificationValue): LcEntryClassification {
    return value.lcEntry as LcEntryClassification;
  }

  /**
   * Get the formGroup by entry name
   * @param name
   */
  public getFormGroupByName(name: string): FormGroup {
    return this.entryValueForm.get(name) as FormGroup;
  }

  /**
   * Builds a formGroup with a named control per classificationToShow with the entryKey as name and nested children
   */
  private initForm(): void {
    this.entryValueForm = this.fb.group({});
    this.valuesToShow.forEach((entryValue: LcEntryClassificationValue) => {
      const classification: LcEntryClassification = entryValue.lcEntry as LcEntryClassification;
      this.entryValueForm.addControl(classification.entryKey, this.buildGroupFromEntry(entryValue, classification));
    });
  }

  /**
   * Takes an LcEntryClassification and builds a formGroup with Validation, an answers array or control depending on entry type
   * and a children array which is built recursively
   * @param entry
   * @param value
   */
  private buildGroupFromEntry(value: LcEntryClassificationValue, entry: LcEntryClassification): FormGroup {
    const validators: ValidatorFn[] = [];
    const group: FormGroup = this.fb.group({});
    if (entry.required === true) {
      validators.push(Validators.required);
    }
    if (entry.type === LcEntryType.FREE_TEXT) {
      if (value.children.length === 0) {
        validators.push(Validators.maxLength((entry as LcEntryText).maxLength));
        group.addControl(entry.entryKey, this.fb.control((value as LcEntryTextValue).text || null, validators));
        return group;
      } else {
        const childrenArray: FormArray = this.buildChildrenArray(value.getNestedClassifications());
        group.addControl(entry.entryKey, this.fb.control((value as LcEntryTextValue).text || null, validators));
        group.addControl('children', childrenArray);
        return group;
      }
    } else if (entry.type === LcEntryType.CHECKLIST) {
      if (value.children.length === 0) {
        group.addControl('answers', this.buildAnswersGroup((value as LcEntryChecklistValue), (entry as LcEntryClassification & HasOptionsMap), validators));
        return group;
      } else {
        const childrenArray: FormArray = this.buildChildrenArray(value.getNestedClassifications());
        group.addControl('answers', this.buildAnswersGroup((value as LcEntryChecklistValue), (entry as LcEntryClassification & HasOptionsMap), validators));
        group.addControl('children', childrenArray);
        return group;
      }
    } else if (entry.type === LcEntryType.SELECT) {
      if (value.children.length === 0) {
        group.addControl('answers', this.fb.control((value as LcEntrySelectValue).selectKey || null, validators));
        return group;
      } else {
        const childrenArray: FormArray = this.buildChildrenArray(value.getNestedClassifications());
        group.addControl('answers', this.fb.control((value as LcEntrySelectValue).selectKey || null, validators));
        group.addControl('children', childrenArray);
        return group;
      }
    }
  }

  /**
   * Builds a FormArray with the given validators for the answers from the options of the given entry
   * using the key from the options and initializing with the values entry
   * @param value
   * @param entry
   * @param validators
   */
  private buildAnswersGroup(value: LcEntryChecklistValue, entry: LcEntryChecklist, validators: ValidatorFn[]): FormGroup {
    const answerGroup: FormGroup = this.fb.group({}, validators);
    Object.keys(entry.options).forEach((key: string) => {
      answerGroup.addControl(key, this.fb.control(value.checkedValues.includes(key) || false));
    });
    return answerGroup;
  }

  /**
   * Builds a FormArray from the given LcEntryClassification array using the buildGroupFromEntry function
   * @param children
   */
  private buildChildrenArray(children: LcEntryClassificationValue[]): FormArray {
    const childrenArray: FormArray = this.fb.array([]);
    children.sort((a, b) => {
      const key1 = a.lcEntry.entryKey.toLowerCase();
      const key2 = b.lcEntry.entryKey.toLowerCase();

      const sorted = [key1, key2].sort();
      return sorted[0] === key1 ? -1 : 1;
    }).forEach((childEntry: LcEntryClassificationValue) => {
      const classification: LcEntryClassification = childEntry.lcEntry as LcEntryClassification;
      const childGroup: FormGroup = this.fb.group({});
      childGroup.addControl(classification.entryKey, this.buildGroupFromEntry(childEntry, classification));
      childrenArray.push(childGroup);
    });
    return childrenArray;
  }
}
