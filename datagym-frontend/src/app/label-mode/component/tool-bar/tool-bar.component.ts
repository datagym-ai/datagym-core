import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {EntryValueService} from '../../service/entry-value.service';
import {Subject} from 'rxjs';
import {NgxSmartModalService} from 'ngx-smart-modal';
import {LabNotificationService} from '../../../client/service/lab-notification.service';
import {MediaStyleFilterService} from '../../../svg-workspace/service/media-style-filter.service';
import {filter, take, takeUntil} from 'rxjs/operators';
import {AisegNotes, AisegService} from '../../service/aiseg.service';
import {AisegLoadingComponent} from '../../../svg-workspace/components/aiseg-loading/aiseg-loading.component';
import {AiSegType} from '../../model/AiSegType';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {LabelModeType} from '../../model/import';
import {UserService} from "../../../client/service/user.service";

/**
 * @deprecated Refactor with State machine!
 */
@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.css']
})
export class ToolBarComponent implements OnInit, OnDestroy {
  private unsubscribe: Subject<void> = new Subject<void>();

  @Input()
  public isClassificationBarOpen: boolean;

  public get dummyProject(): boolean {
    return this.aiseg.dummyProject;
  }
  public get demoProject(): boolean {
    return this.aiseg.demoProject;
  }
  public get aiSegLimitReached(): boolean {
    return this.aiseg.aiSegLimitReached;
  }

  public readonly AiSegType = AiSegType;

  /**
   * Let the user select one of this tools for aiseg:
   */
  public readonly AisegTypes = [
    AiSegType.POINTS,
    AiSegType.RECTANGLE,
    AiSegType.BRUSH,
    AiSegType.POINT,
    // Removed AiSegType.EDGE_LINE from frontend, the implementation
    // is still 'alive' and could be used by uncommenting this line:
    // AiSegType.EDGE_LINE,
  ];

  public AISegTypeForm: FormGroup;

  constructor(public mediaStyleFilter: MediaStyleFilterService,
              private valueService: EntryValueService,
              private aiseg: AisegService,
              private modalService: NgxSmartModalService,
              private notification: LabNotificationService,
              private userService: UserService
  ) {
  }

  ngOnInit() {
    const preSelectType: AiSegType = this.AisegTypes[0];
    this.AISegTypeForm = new FormGroup({
      AiSegOption: new FormControl(preSelectType, [Validators.required])
    });
    this.AISegTypeForm.get('AiSegOption').disable();
    // register always the selected aiseg type within the service.
    this.AISegTypeForm.valueChanges.pipe(takeUntil(this.unsubscribe)).subscribe((selected: {AiSegOption: AiSegType}) => {
      this.aiseg.selectedType = selected.AiSegOption;
    });

    /*
     * AISeg is disabled:
     * - while video labeling
     * - in demo project / public available on the webpage
     * - in dummy project
     * - the limit is reached.
     */
    this.valueService.valuesLoaded$.pipe(filter(value => value === true), take(1), takeUntil(this.unsubscribe)).subscribe(() => {
      const imageLabeling = this.valueService.labelModeType === LabelModeType.IMAGE;
      if (this.demoProject || this.dummyProject || this.aiSegLimitReached || this.userService.userDetails.isOpenCoreEnvironment) {
        // nothing to do if aiseg limit is reached.
        this.AISegTypeForm.get('AiSegOption').disable();
        // return is here the key. All following operations are skipped. Including prepare api call.
        return;
      }
      this.AISegTypeForm.get('AiSegOption').enable();

      if (imageLabeling) {
        // Inform the cluster that a new media should be fetched
        this.aiseg.prepareCurrentMedia().subscribe(() => {
        });
      }
    });

    this.aiseg.aiSegActivationEvent.pipe(takeUntil(this.unsubscribe)).subscribe((value: boolean) => {
      const options = this.AISegTypeForm.get('AiSegOption');
      if (!!value) {
        options.disable();
      } else {
        options.enable();
      }
    });

    const LOADING = 'aiseg-loading';
    this.aiseg.notifications$.pipe(filter(value => value === AisegNotes.LOADING), takeUntil(this.unsubscribe)).subscribe(() => {
      this.modalService.create(LOADING, AisegLoadingComponent, { closable: false, refocus:false }).open();
    });
    this.aiseg.notifications$.pipe(filter(value => value === AisegNotes.ERROR_CALCULATING), takeUntil(this.unsubscribe)).subscribe(() => {
      this.modalService.close(LOADING);
      this.notification.error_i18('AISEG.ERROR_CALCULATING');
    });
    this.aiseg.notifications$.pipe(filter(value => value === AisegNotes.COMPLETED), takeUntil(this.unsubscribe)).subscribe(() => {
      this.modalService.close(LOADING);
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
    // AiSeg is not available for the dummy project
    if (!this.dummyProject && !this.aiSegLimitReached) {
      this.aiseg.reset();
    }
  }

  get allowAiSeg() : boolean {
    return this.AISegTypeForm.valid && this.aiseg.isAllowed;
  }

  get aiSegTitle(): string {

    if (this.AISegTypeForm.get('AiSegOption').enabled && !this.AISegTypeForm.valid) {
      return 'AISEG.SELECT_MODE';
    }

    return this.aiseg.title;
  }

  public onStartAiSeg(): void {
    if (!this.AISegTypeForm.valid) {
      return;
    }
    const type: AiSegType = this.AISegTypeForm.value.AiSegOption;
    this.aiseg.initAiseg(type);
  }
}
