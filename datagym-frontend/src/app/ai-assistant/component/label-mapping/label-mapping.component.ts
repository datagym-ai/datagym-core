import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Project} from '../../../project/model/Project';
import {ActivatedRoute} from '@angular/router';
import {AbstractControl, FormArray, FormControl, FormGroup, Validators} from '@angular/forms';
import {PreLabelClass} from '../../model/PreLabelClass';
import {PreLabelApiService} from '../../service/pre-label-api.service';
import {PreLabelInfoViewModel} from '../../model/PreLabelInfoViewModel';
import {PreLabelConfigUpdateBindingModel} from '../../model/PreLabelConfigUpdateBindingModel';
import {PreLabelMapping} from '../../model/PreLabelMapping';
import {PreLabelGeometry} from '../../model/PreLabelGeometry';
import {LcEntryType} from '../../../label-config/model/LcEntryType';
import {LabelConfiguration} from '../../../label-config/model/LabelConfiguration';
import {LcEntry} from '../../../label-config/model/LcEntry';
import {LcEntryFactory} from '../../../label-config/model/LcEntryFactory';
import {map, switchMap} from 'rxjs/operators';
import {AppButtonInput} from '../../../shared/button/button.component';
import {DialogueModel} from '../../../shared/dialogue-modal/DialogueModel';
import {DialogueService} from '../../../shared/service/dialogue.service';
import {LabelConfigService} from '../../../label-config/service/label-config.service';


@Component({
  selector: 'app-label-mapping',
  templateUrl: './label-mapping.component.html',
  styleUrls: ['./label-mapping.component.css']
})
export class LabelMappingComponent implements OnInit {
  configEditForm: FormGroup;
  availableNetworkClasses: PreLabelClass[];
  remainingNetworkClasses: PreLabelClass[] = [];
  availableGeometries: PreLabelGeometry[] = [];
  availableEntryTypes: LcEntryType[] = [LcEntryType.RECTANGLE, LcEntryType.POLYGON];
  LcEntryType = LcEntryType;

  @Input()
  preLabelConfig: PreLabelInfoViewModel;

  @Output()
  updateMapping: EventEmitter<PreLabelInfoViewModel> = new EventEmitter<PreLabelInfoViewModel>();

  project: Project;
  inputError: boolean = false;
  labelConfig: LabelConfiguration;

  constructor(private route: ActivatedRoute,
              private labelConfigService: LabelConfigService,
              private preLabelApiService:PreLabelApiService,
              private diaService: DialogueService,) {
  }

  ngOnInit(): void {
    this.project = this.route.parent.parent.snapshot.data.project as Project;

    this.labelConfig = this.route.snapshot.data.labelConfig as LabelConfiguration;
    if (!!this.preLabelConfig) {
      this.availableNetworkClasses = [];
      for (const classId in this.preLabelConfig.availableNetworkClasses) {
        this.availableNetworkClasses.push(new PreLabelClass(classId, this.preLabelConfig.availableNetworkClasses[classId]));
      }

      this.availableNetworkClasses.sort((a, b) => a.preLabelModel < b.preLabelModel ? -1 : 1);
      this.remainingNetworkClasses = Object.assign(this.remainingNetworkClasses, this.availableNetworkClasses);
      this.availableGeometries = Object.assign(this.availableGeometries, this.preLabelConfig.availableGeometries);
      this.initMappingControls();
    }
  }

  getControls() {
    return (this.configEditForm.get('mappingControls') as FormArray).controls;
  }

  /**
   * Creates the reactive form and its entries for each label mapping within preLabelMappings
   */
  initMappingControls() {
    this.configEditForm = new FormGroup({
      'mappingControls': new FormArray([])
    });

    this.preLabelConfig.preLabelMappings.forEach((mapping:PreLabelMapping) => {
      const preLabelGeometry = this.getPreLabelGeometryById(mapping.lcEntryId);
      const preLabelGeometryType = !!preLabelGeometry? preLabelGeometry.type : null;
      const preLabelClass = new PreLabelClass(mapping.preLabelClassKey, mapping.preLabelModel);

      const controlGroup = new FormGroup({
        'labelClassControl': new FormControl(preLabelClass, Validators.required),
        'lcEntryControl': new FormControl(preLabelGeometry, Validators.required),
        'lcEntryTypeControl': new FormControl(preLabelGeometryType)
      });
      (this.configEditForm.get('mappingControls') as FormArray).push(controlGroup);
    });
  }

  private getPreLabelGeometryById(lcEntryId: string): PreLabelGeometry {
    for (const preLabelGeometry of this.preLabelConfig.availableGeometries) {
      if (preLabelGeometry.id === lcEntryId) {
        return preLabelGeometry;
      }
    }
    return null;
  }

  private getPreLabelGeometryByEntryKey(lcEntryKey: string): PreLabelGeometry {
    for (const preLabelGeometry of this.availableGeometries) {
      if (preLabelGeometry.entryKey === lcEntryKey) {
        return preLabelGeometry;
      }
    }
    return null;
  }

  private getLcEntryByEntryKey(lcEntryKey: string): LcEntry {
    for (const lcEntry of this.labelConfig.entries) {
      if (lcEntry.entryKey === lcEntryKey) {
        return lcEntry;
      }
    }
    return null;
  }

  addMapping() {
    const controlGroup = new FormGroup({
      'labelClassControl': new FormControl(null, Validators.required),
      'lcEntryControl': new FormControl(null, Validators.required),
      'lcEntryTypeControl': new FormControl(null)
    });

    (this.configEditForm.get('mappingControls') as FormArray).push(controlGroup);
  }

  /**
   * Creates new LcEntries for every selected Geometry that has no LcEntry in the exisisting
   * LabelConfiguration.
   */
  createNewLcEntries(): LcEntry[] {
    const newLcEntries: LcEntry[] = [];

    this.getControls().forEach((control: AbstractControl) => {
      const preLabelGeometry = control.value['lcEntryControl'];
      if (!!preLabelGeometry && !!!preLabelGeometry.id) {
        newLcEntries.push(LcEntryFactory.createEmptyLcEntryFromPreLabelGeometry(preLabelGeometry));
      }
    });

    return  newLcEntries;
  }

  /**
   * Parses selected preLabelClass and geometries to mapping format required by the backend
   * { 'LcEntryId': [ { 'preLabelClasskey': '0', 'preLabelModel': 'car'}, ... ] }
   */
  parseMappingsFromForm(): Record<any,PreLabelClass[]> {
    const mappingObject: Record<any,PreLabelClass[]> = {};

    this.getControls().forEach((control: AbstractControl) => {

      const preLabelGeometry = control.value['lcEntryControl'];
      let lcEntryId: string = preLabelGeometry.id;
      const preLabelClass = control.value['labelClassControl'];

      if (!!!lcEntryId) {
        lcEntryId =  this.getLcEntryByEntryKey(preLabelGeometry.entryKey).id;
        preLabelGeometry.id = lcEntryId;
      }

      if (!(lcEntryId in mappingObject) ) {
        mappingObject[lcEntryId] = [];
      }

      mappingObject[lcEntryId].push(preLabelClass);

    });

    return mappingObject;
  }

  onSubmit() {
    const newLcEntries: LcEntry[] = this.createNewLcEntries();

    // Mark controls as touched to show possible invalid inputs
    this.getControls().forEach((control: AbstractControl) => {

      if (!/*not*/!!control.value['labelClassControl']) {
        control.get('labelClassControl').markAsTouched();
      }
      if (!/*not*/!!control.value['lcEntryControl']) {
        control.get('lcEntryControl').markAsTouched();
      }

      control.markAsTouched();
    });

    if (this.configEditForm.valid) {
      this.inputError = false;

      if (newLcEntries.length > 0) {
        // LabelConfiguration must be updated if new LcEntries (geometries) are defined
        const payload: LcEntry[] = [...newLcEntries, ...LcEntryFactory.castEntriesListProperly(this.labelConfig.entries)];

        this.labelConfigService.updateLabelConfig(this.project.labelConfigurationId, payload, false)
          .pipe(switchMap(labelConfig => {

            this.labelConfig = labelConfig;
            const mappingObject: Record<any, PreLabelClass[]> = this.parseMappingsFromForm();
            const config = new PreLabelConfigUpdateBindingModel(false, mappingObject);

            return this.preLabelApiService.updatePreLabelConfigByProject(this.project.id, config)
              .pipe(map(preLabelConfig => ({labelConfig, preLabelConfig})));
          })).subscribe(({labelConfig, preLabelConfig}) => {
            this.updateMapping.emit(preLabelConfig);
            this.configEditForm.markAsPristine();
        });
      } else {
        const mappingObject: Record<any,PreLabelClass[]> = this.parseMappingsFromForm();
        const config = new PreLabelConfigUpdateBindingModel(false, mappingObject);
        this.preLabelApiService.updatePreLabelConfigByProject(this.project.id, config).subscribe((updatedConfig: PreLabelInfoViewModel) => {
          this.updateMapping.emit(updatedConfig);
          this.configEditForm.markAsPristine();
        });
      }
    } else {
      this.inputError = true;
    }
  }

  deleteMapping(i: number) {
    const title = 'GLOBAL.DIALOGUE.TITLE.CONFIRM_DELETE';
    const content = 'FEATURE.AI_ASSISTANT.DELETE_DIALOG_MESSAGE';
    const confirm = 'GLOBAL.CONFIRM';
    const cancel = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = { label: confirm, styling: 'warn' };
    const dialogueContent: DialogueModel = { title, content, buttonLeft, buttonRight: cancel };
    this.diaService.openDialogue(dialogueContent);
    const diaSub = this.diaService.closeAction.subscribe((choice: boolean) => {
      if (choice) {
        this.getControls().splice(i, 1);
        this.configEditForm.get('mappingControls').updateValueAndValidity();
        this.configEditForm.markAsDirty();
      } else {
        diaSub.unsubscribe();
      }
    });
  }

  /**
   * Updates the ng-select control with a new LcEntryType after a new LcEntry was selected
   * @param i - index of the mapping table row
   */
  updateLcEntrySelection(i: number) {
    const lcEntry = this.getControls()[i].get('lcEntryControl').value;
    if (!!lcEntry) {
      const lcEntryKey =  this.getPreLabelGeometryByEntryKey(lcEntry.entryKey);
      if (!!lcEntryKey) {
        this.getControls()[i].get('lcEntryTypeControl').setValue(lcEntryKey.type);
      } else {
        this.getControls()[i].get('lcEntryTypeControl').setValue(null);
      }
    } else {
      this.getControls()[i].get('lcEntryTypeControl').setValue(null);
    }
  }

  /**
   * Removes PreLabelClasses from the ng-select control if they were already selected
   * in the mapping. Each PreLabelClass can only be selected once!
   */
  updateAvailableClasses() {
    const selectedClasses: string[] = this.getControls()
      .filter((control: AbstractControl) => !!control.get('labelClassControl').value)
      .map((control: AbstractControl) => (control.get('labelClassControl').value).preLabelClassKey);
    this.remainingNetworkClasses = this.availableNetworkClasses
      .filter((labelClass: PreLabelClass) => !selectedClasses.includes(labelClass.preLabelClassKey));
  }

  /**
   * Adds a "create new geometry" item to the ng-select if the selected PreLabelClass
   * has no counter part in the existing LabelConfiguration
   *
   * @param i - index of the mapping table row
   */
  updateAvailableLcEntries(i: number) {
    this.availableGeometries = [...this.availableGeometries.filter(geo => !!geo.id)];

    const preLabelClass: PreLabelClass =  this.getControls()[i].get('labelClassControl').value;
    if (!!preLabelClass && !!!this.getPreLabelGeometryByEntryKey(preLabelClass.preLabelModel)) {
      const preLabelGeo = new PreLabelGeometry(null, preLabelClass.preLabelModel, preLabelClass.preLabelModel, LcEntryType.RECTANGLE);
      this.availableGeometries = [preLabelGeo, ...this.availableGeometries.filter(geo => !!geo.id)];
    }
  }

  /**
   * Updates the selected LcEntry with the LcEntryType set in the LcEntryType
   * select box
   *
   * @param i - index of the mapping table row
   */
  updateLcEntryTypeSelection(i: number) {
    const lcEntry = this.getControls()[i].get('lcEntryControl').value;
    lcEntry.type = this.getControls()[i].get('lcEntryTypeControl').value;
  }
}
