import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {LcEntry} from '../../model/LcEntry';
import {LabelConfigService} from '../../service/label-config.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs';
import {LcEntryType} from '../../model/LcEntryType';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {LcEntryGeometry} from '../../model/geometry/LcEntryGeometry';
import {LcEntryText} from '../../model/classification/LcEntryText';
import {HasOptionsMap} from '../../model/classification/HasOptionsMap';
import {LcEntryClassification} from '../../model/classification/LcEntryClassification';
import {DialogueService} from '../../../shared/service/dialogue.service';
import {AppButtonInput} from '../../../shared/button/button.component';
import {EditModeGuardService} from '../../service/edit-mode-guard.service';
import {take} from 'rxjs/operators';
import {ErrorHandlers, ErrorMessage} from '../../../shared/dg-input-errors/dg-input-errors.component';
import {DialogueModel} from '../../../shared/dialogue-modal/DialogueModel';
import {LabelConfigApiService} from '../../service/label-config-api.service';
import {KeywordsValidator} from '../../custom-validation/KeywordsValidator';
import {EditEntryDuplicateValidator} from '../../custom-validation/EditEntryDuplicateValidator';
import {ConnectControls} from '../../service/ConnectControls';
import {TextfieldComponent} from '../../../shared/textfield/textfield.component';
import {UUID} from 'angular2-uuid';
import {Project} from '../../../project/model/Project';
import {MediaType} from '../../../project/model/MediaType.enum';
import {LabelConfigForm} from '../../model/LabelConfigForm';
import {AllowedChars} from '../../../shared/validator/AllowedChars';


@Component({
  selector: 'app-edit-entry',
  templateUrl: './edit-entry.component.html',
  styleUrls: ['./edit-entry.component.css']
})
export class EditEntryComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('editBox') public editBox: ElementRef;
  public entry: LcEntry;
  public idToEdit: string;
  public editForm: FormGroup;
  public isGeo: boolean = false;
  public isText: boolean = false;
  public shortcutOnInit: string | null = null;
  public entryProperties: FormArray;
  public answers: FormArray;
  public LcEntryType = LcEntryType;


  // [1 .. 9, 0]
  public readonly possibleShortcuts: string[] = LabelConfigForm.getPossibleShortcuts();

  /**
   * @deprecated Remove when the video labeling mode supports required classifications.
   * There is a (performance) problem to decide 'when is a geometry valid' when there
   * are required nested classifications. Global classifications are not supported anyway.
   */
  public isVideo: boolean = false;
  public readonly geometryColors: string[] = this.lcService.geometryColors;
  @ViewChild('entryValueField') private entryValueField: TextfieldComponent;

  /**
   * register this error handlers to 'app-text-component'
   * to display useful error messages.
   */
  public errorHandlers: ErrorHandlers = {
    // duplicate is set by EditEntryDuplicateValidator set to answers / options
    duplicate: (error => new ErrorMessage(error as string, {})),
    // set by KeywordsValidator
    duplicateExportKey: (() => new ErrorMessage('GLOBAL.ERROR_FLAGS.DUPLICATE_KEYS'))
  };

  private routeSub: Subscription;
  private diaSub: Subscription;
  private changeSub: Subscription;
  private forbiddenKeywords: string[] = [];

  constructor(private router: Router,
              private fb: FormBuilder,
              private route: ActivatedRoute,
              private labelConfigApiService: LabelConfigApiService,
              private diaService: DialogueService,
              public lcService: LabelConfigService,
              public editModeGuard: EditModeGuardService) {
  }

  /**
   * Depending on the entry type return all possible geometry or classification
   * values that can be selected as new type.
   */
  public get entryTypes(): LcEntryType[] {

    if (LcEntryType.isClassification(this.entry)) {
      return LcEntryType.values().filter(value => LcEntryType.isClassification(value));
    }

    return LcEntryType.values()
      .filter(value => LcEntryType.isGeometry(value))
      // IMAGE_SEGMENTATION_ERASER is only available within the label mode.
      .filter(type => type !== LcEntryType.IMAGE_SEGMENTATION_ERASER)
      // Geometries cannot be changed to IMAGE_SEGMENTATION or vice versa.
      .filter(type => type !== LcEntryType.IMAGE_SEGMENTATION);
  }

  /**
   * Shortcuts are only supported on geometries of the root nesting level.
   */
  public get shortcutSupported(): boolean {
    return LcEntryType.isGeometry(this.entry) && this.entry.lcEntryParentId === null;
  }

  /**
   * Initializes FormGroup for Geometries with the entries values or defaults
   */
  private get geoProperties(): FormGroup {
    return this.fb.group({
      color: [(this.entry as LcEntryGeometry).color || this.getParentColor() || this.lcService.getNextColor()],
      shortcut: [(this.entry as LcEntryGeometry).shortcut || null]
    });
  }

  /**
   * Initializes FormGroup for Classifications with Answers with the entries values or defaults
   * double casting to HasOptionsMap while researching a better way
   */
  get classificationPropertiesWithAnswers(): FormGroup {
    const options = (this.entry as LcEntryClassification & HasOptionsMap).options;
    if (Object.keys(options).length > 0) {
      const optionsObj = (this.entry as LcEntryClassification & HasOptionsMap).options;
      const keys = Object.keys(optionsObj);
      const answerArr = this.fb.array([]);
      keys.forEach((mapKey: string) => {
        const mapValue = optionsObj[mapKey];
        const group = this.fb.group({
          key: [mapKey, [
            Validators.required,
            Validators.maxLength(LabelConfigForm.exportKey.maxLength),
            AllowedChars.pattern(LabelConfigForm.exportKey.pattern),
          ]],
          value: [mapValue, [
            Validators.required,
            Validators.maxLength(LabelConfigForm.exportName.maxLength),
            AllowedChars.pattern(LabelConfigForm.exportName.pattern)
          ]]
        });

        if (!mapKey) {
          const leader = group.get('value');
          const follower = group.get('key');
          ConnectControls.LABEL_CONFIG(leader, follower);
        }

        answerArr.push(group);
      });
      return this.fb.group({
        required: [(this.entry as LcEntryClassification).required || false],
        answers: answerArr
      });
    } else if (this.entry.type !== LcEntryType.CHECKLIST) {
      return this.fb.group({
        required: [(this.entry as LcEntryClassification).required || false],
        answers: this.fb.array([this.createNewOptionFG(), this.createNewOptionFG()])
      });
    } else {
      return this.fb.group({
        required: [(this.entry as LcEntryClassification).required || false],
        answers: this.fb.array([this.createNewOptionFG()])
      });
    }
  }

  /**
   * Initializes FormGroup for FreeText with the entries values or defaults
   */
  get freeTextProperties(): FormGroup {
    return this.fb.group({
      required: [(this.entry as LcEntryText).required || false],
      maxLength: [(this.entry as LcEntryText).maxLength || LabelConfigForm.textFieldMaxLength, [
        Validators.required,
        Validators.min(1),
        Validators.max(LabelConfigForm.textFieldMaxLength)
      ]]
    });
  }

  get color(): string {

    const properties = this.editForm.get('entryProperties');
    if (properties === undefined) {
      return '';
    }

    const colorForm = properties.get('color');
    if (colorForm === undefined || colorForm === null) {
      return '';
    }

    const color = colorForm.value as string;
    return color || '';
  }

  /**
   * Takes Id from routeParams; gets the entry from the service and sets boolean values depending on the entryType; initializes Form
   */
  ngOnInit() {
    const project = this.route.parent.parent.parent.snapshot.data.project as Project;
    this.isVideo = project.mediaType === MediaType.VIDEO;

    this.forbiddenKeywords = this.route.parent.snapshot.data.forbidden as string[];
    this.routeSub = this.route.paramMap.subscribe(value => {
      this.idToEdit = value.get('id');
      this.reset();
      this.entry = this.lcService.findLcEntryById(this.idToEdit);
      if (this.entry) {
        this.isGeo = LcEntryType.isGeometry(this.entry.type);
        this.shortcutOnInit = this.isGeo ? (this.entry as LcEntryGeometry).shortcut || null : null;
        this.isText = this.entry.type === LcEntryType.FREE_TEXT;
        this.initForm();
      }
    });
  }

  ngOnDestroy(): void {
    this.reset();
    this.routeSub.unsubscribe();
    if (this.changeSub) {
      this.changeSub.unsubscribe();
    }
    if (this.diaSub) {
      this.diaSub.unsubscribe();
    }
  }

  ngAfterViewInit(): void {
    if (this.entryValueField !== undefined) {
      this.entryValueField.inputElementRef.nativeElement.focus();
    }
  }

  public hasParent(): boolean {
    return !!this.entry && !!this.entry.lcEntryParentId;
  }

  public isShortcutAvailable(shortcut: string) : boolean {
    if (!!this.shortcutOnInit && shortcut === this.shortcutOnInit) {
      return true;
    }
    return !this.lcService.getUsedShortCuts().includes(shortcut);
  }

  /**
   * Prevent the user from typing lowercase characters
   * by instant converting them to lowercase.
   *
   * Usage in template:
   * <app-textfield (input)="toLowercase($event, 'name', 'entryKey')" ...>
   */
  public toLowercase($event, groupOrIndex: string | number, name: string) {

    const value = $event.target.value;
    const lower = value.toLowerCase();

    if (typeof groupOrIndex === 'number') {
      this.getAnswerControlByIndexAndName(+groupOrIndex, name).setValue(lower);
    } else {
      this.getControlsByGroupAndName(groupOrIndex, name).setValue(lower);
    }
  }

  /**
   * Builds the LCEntry depending on the type.
   */
  public onSubmit(): void {
    this.entry.entryKey = this.getControlsByGroupAndName('name', 'entryKey').value;
    this.entry.entryValue = this.getControlsByGroupAndName('name', 'entryValue').value;

    if (LcEntryType.isGeometry(this.entry.type)) {
      (this.entry as LcEntryGeometry).color = this.getControlsByGroupAndName('entryProperties', 'color').value;
      if (!this.hasParent()) {
        (this.entry as LcEntryGeometry).shortcut = this.getControlsByGroupAndName('entryProperties', 'shortcut').value;
      }
    } else {
      this.getOptions();
      if (this.entry.type === LcEntryType.FREE_TEXT) {
        (this.entry as LcEntryText).required = this.getControlsByGroupAndName('entryProperties', 'required').value;
        (this.entry as LcEntryText).maxLength = this.getControlsByGroupAndName('entryProperties', 'maxLength').value;
      }
      if (this.isVideo) {
        // Just to make sure that the required flag is false
        // until it is supported within the video labeling mode.
        (this.entry as LcEntryClassification).required = false;
      }
    }

    const entryTypeControl = this.getControlsByGroupAndName('name', 'entryType');
    /*
     * Allow for new created geometries to switch the geometry type.
     * Expect the IMAGE_SEGMENTATION geometry type. This type cannot be nested
     * and cannot have a nested geometry. To simplify this component remove that possibility.
     */
    if (this.isGeo && entryTypeControl.dirty && this.entry.type !== LcEntryType.IMAGE_SEGMENTATION && this.entry.id.startsWith('internal_')) {
      // const fakeEntry = {...this.entry, type: entryTypeControl.value};
      // const newGeometry = LcEntryFactory.copyEntry(fakeEntry as LcEntry);
      this.entry.id = `internal_${ UUID.UUID() }`;
      this.entry.children.forEach(c => {
        c.id = `internal_${ UUID.UUID() }`;
        c.children.forEach(cc => {
          cc.id = `internal_${ UUID.UUID() }`;
          cc.children.forEach(ccc => {
            // Max three nesting levels are supported.
            ccc.id = `internal_${ UUID.UUID() }`;
          });
        });
      });
      this.entry.type = entryTypeControl.value;
    }

    this.lcService.doneEditing.emit();
    // don't listen on 'lcService.doneEditing',
    // this event is emitted on several other places.
    this.editModeGuard.editMode = false;
    this.router.navigate(['../../'], { relativeTo: this.route }).then();
  }

  /**
   * Deletes the current element after accept was clicked in the dialogue and navigates to label-editor component
   */
  public onDelete(): void {
    const title = 'GLOBAL.DIALOGUE.TITLE.CONFIRM_DELETE';
    const content = 'FEATURE.LABEL_CONFIG.DIALOGUE.DELETE_ENTRY';
    const confirm = 'GLOBAL.CONFIRM';
    const cancel = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = { label: confirm, styling: 'warn' };
    const dialogueContent: DialogueModel = { title, content, buttonLeft, buttonRight: cancel };
    this.diaService.openDialogue(dialogueContent);
    this.diaSub = this.diaService.closeAction.subscribe((choice: boolean) => {
      if (choice) {
        this.lcService.deleteEntryById(this.entry.id);
        this.editModeGuard.editMode = false;
        this.router.navigate(['../../'], { relativeTo: this.route }).then();
      } else {
        this.diaSub.unsubscribe();
      }
    });
  }

  /**
   * Remove the shortcut from the entry and reset corresponding formControl
   */
  public onRemoveShortcut(): void {
    if (LcEntryType.isGeometry(this.entry.type)) {
      (this.editForm.get('entryProperties').get('shortcut') as FormControl).reset(null);
    }
  }

  /**
   * Sets the values from the Form into the Map of the Entry
   * Double casting into HasOptionsMap while researching a better way
   */
  public getOptions(): void {
    (this.entry as LcEntryClassification & HasOptionsMap).options = new Map<string, string>();
    if (this.answers) {
      for (let i = 0; i < this.answers.controls.length; i++) {
        const key: string = this.getAnswerControlByIndexAndName(i, 'key').value;
        (this.entry as LcEntryClassification & HasOptionsMap).options[key] = this.getAnswerControlByIndexAndName(i, 'value').value;
      }
    }
    (this.entry as LcEntryClassification).required = this.getControlsByGroupAndName('entryProperties', 'required').value;
  }

  /**
   * Gets FormControl from group with name
   * @param group i.e. 'name'
   * @param name i.e. 'entryKey'
   */
  public getControlsByGroupAndName(group: string, name: string): FormControl {
    return this.editForm.get(group).get(name) as FormControl;
  }

  /**
   * Just a little helper to 'cast' the given input for the template.
   * @param formGroup
   */
  public asFormGroup(formGroup): FormGroup {
    return formGroup as FormGroup;
  }

  public onShortcutChanged(shortcut: string): void {
    (this.editForm.get('entryProperties').get('shortcut') as FormControl).patchValue(shortcut);
  }

  /**
   * Gets FormControl for answers
   * @param index of the answer
   * @param name 'key' or 'value'
   */
  public getAnswerControlByIndexAndName(index: number, name: string): FormControl {
    return (this.editForm.get('entryProperties').get('answers') as FormArray).controls[index].get(name) as FormControl;
  }

  public addAnswer() {
    const newOption = this.createNewOptionFG();
    ((this.editForm.get('entryProperties') as FormGroup).get('answers') as FormArray).push(newOption);
    this.answers = this.editForm.get('entryProperties').get('answers') as FormArray;
    this.editModeGuard.editMode = true;
    const waitingTime = 300; // milliseconds
    setTimeout(() => {
      EditEntryComponent.scrollToBottom(this.editBox);
    }, waitingTime);
  }

  public removeAnswer(answer: number) {
    this.answers.removeAt(answer);
    this.editModeGuard.editMode = true;
  }

  public onCancel(): void {
    if (!this.entry.entryKey || !this.entry.entryValue) {
      // Allow cancel only when the entry has key & value
      // otherwise an 'empty' config could be passed to the BE.
      return;
    }
    this.editModeGuard.editMode = false;
    this.router.navigate(['../../'], {relativeTo: this.route}).then();
  }

  /**
   * Initializes options FormGroup for new Classifications with Options
   */
  private createNewOptionFG(): FormGroup {
    const optionFG = this.fb.group({
      key: [null, [
        Validators.required,
        Validators.maxLength(LabelConfigForm.exportKey.maxLength),
        AllowedChars.pattern(LabelConfigForm.exportKey.pattern),
      ]],
      value: [null, [
        Validators.required,
        Validators.maxLength(LabelConfigForm.exportName.maxLength),
        AllowedChars.pattern(LabelConfigForm.exportName.pattern)
      ]]
    });

    ConnectControls.LABEL_CONFIG(
      optionFG.get('value'),
      optionFG.get('key')
    );

    return optionFG;
  }

  /**
   * helper function to reset form state
   */
  private reset(): void {
    if (this.changeSub) {
      this.changeSub.unsubscribe();
    }
    if (this.editForm) {
      this.editForm.reset();
    }
    this.isGeo = false;
    this.isText = false;
    this.answers = null;
  }

  /**
   * Creates the form depending on entryType:
   * - creates 'name' group for with entryKey and entryValue
   * - adds 'entryProperties' group with controls depending on the entryType
   * - if its a classification that isn't a text, the answers FormArray is bound to this.answers
   * - sets up a listener on formChanges to enable editMode in routeGuard
   */
  private initForm(): void {

    const usedExportKeys: string[] = this.lcService.getUsedExportKeys(this.entry.lcEntryParentId)
      .filter(v => v !== this.entry.entryKey);

    this.editForm = this.fb.group({
      name: this.fb.group({
        entryKey: [this.entry.entryKey || null, [
          Validators.required,
          Validators.maxLength(LabelConfigForm.exportKey.maxLength),
          AllowedChars.pattern(LabelConfigForm.exportKey.pattern),
          KeywordsValidator.forbidden(this.forbiddenKeywords),
          KeywordsValidator.duplicateExportKey(usedExportKeys)
        ]],
        entryValue: [this.entry.entryValue || null, [
          Validators.required,
          Validators.maxLength(LabelConfigForm.exportName.maxLength),
          AllowedChars.pattern(LabelConfigForm.exportName.pattern),
        ]],
        entryType: [this.entry.type, [
          Validators.required
        ]]
      })
    });

    /**
     * Allow for *new* created geometries expect IMAGE_SEGMENTATION to switch the type.
     */
    if (!this.isGeo || !this.entry.isNewEntry() || this.entry.type === LcEntryType.IMAGE_SEGMENTATION) {
      this.editForm.get('name.entryType').disable();
    }

    if (!this.entry.entryKey) {
      const name = this.editForm.get('name.entryValue');
      const key = this.editForm.get('name.entryKey');
      ConnectControls.LABEL_CONFIG(name, key);
    }

    if (this.isGeo) {
      this.editForm.addControl('entryProperties', this.geoProperties);
    } else if (!this.isText) {
      this.editForm.addControl('entryProperties', this.classificationPropertiesWithAnswers);
      this.answers = this.editForm.get('entryProperties').get('answers') as FormArray;
      this.editForm.setValidators([
        EditEntryDuplicateValidator.validate('key', 'GLOBAL.ERROR_FLAGS.DUPLICATE_KEYS'),
        EditEntryDuplicateValidator.validate('value', 'GLOBAL.ERROR_FLAGS.DUPLICATE_VALUES')
      ]);
    } else {
      this.editForm.addControl('entryProperties', this.freeTextProperties);
    }
    this.changeSub = this.editForm.valueChanges.pipe(take(1)).subscribe(() => {
      this.editModeGuard.editMode = true;
    });
  }

  private static scrollToBottom(element: ElementRef): void {
    element.nativeElement.scrollTop = element.nativeElement.scrollHeight;
  }

  /**
   * If a nested geometry is created, use the parent's color as default color.
   *
   * @private
   */
  private getParentColor(): string|null {
    if (this.hasParent()) {
      const parent = this.lcService.findLcEntryById(this.entry.lcEntryParentId);
      if (!!parent && LcEntryType.isGeometry(parent)) {
        return (parent as LcEntryGeometry).color;
      }
    }
    return null;
  }
}
