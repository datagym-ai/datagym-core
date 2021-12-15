import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import {LcEntry} from '../../model/LcEntry';
import {LcEntryPoint} from '../../model/geometry/LcEntryPoint';
import {LabelConfigService} from '../../service/label-config.service';
import {LcEntryType} from '../../model/LcEntryType';
import {LcEntryLine} from '../../model/geometry/LcEntryLine';
import {LcEntrySelect} from '../../model/classification/LcEntrySelect';
import {LcEntryPolygon} from '../../model/geometry/LcEntryPolygon';
import {LcEntryRectangle} from '../../model/geometry/LcEntryRectangle';
import {LcEntryChecklist} from '../../model/classification/LcEntryChecklist';
import {LcEntryText} from '../../model/classification/LcEntryText';
import {ActivatedRoute, Router} from '@angular/router';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Subscription} from 'rxjs';
import {CreateLabelModalConfiguration, LabelChoice} from '../../model/CreateLabelModalConfiguration';
import {LcEntryImageSegmentation} from '../../model/geometry/LcEntryImageSegmentation';

@Component({
  selector: 'app-create-label-modal',
  templateUrl: './create-label-modal.component.html',
  styleUrls: ['./create-label-modal.component.css']
})
export class CreateLabelModalComponent implements AfterViewInit, OnDestroy{
  public entry: LcEntry;
  public LcEntryType = LcEntryType;
  public modalId: string = 'labelModal';
  public modal: NgxSmartModalComponent;
  private dataSub: Subscription;

  public get parentId(): string|null {
    return !!this.config ? this.config.id : null;
  }

  public get labelChoice(): LabelChoice {
    return !!this.config ? this.config.choice : LabelChoice.NONE;
  }

  public set labelChoice(choice: LabelChoice) {
    if (!!this.config) {
      this.config.choice = choice;
    }
  }

  public get choiceMade(): boolean {
    return this.labelChoice !== LabelChoice.NONE;
  }

  public get types(): LcEntryType[] {
    switch (this.labelChoice) {
      case LabelChoice.CLASSIFICATION:
        return this.classificationTypes;
      case LabelChoice.GEOMETRY:
        return this.geoTypes;
      case LabelChoice.GEOMETRY_WITHOUT_SEGMENTATION:
        return this.geoTypes.filter(type => type !== LcEntryType.IMAGE_SEGMENTATION);
      case LabelChoice.NONE:
      // should not be possible
      default:
      // should not be possible
    }
    return [];
  }

  // private labelChoice: LabelChoice = LabelChoice.NONE;
  private readonly geoTypes = [LcEntryType.POINT, LcEntryType.LINE, LcEntryType.RECTANGLE, LcEntryType.POLYGON, LcEntryType.IMAGE_SEGMENTATION];
  private readonly classificationTypes = [LcEntryType.CHECKLIST, LcEntryType.SELECT, LcEntryType.FREE_TEXT];
  private config: CreateLabelModalConfiguration = null;

  constructor(public lcService: LabelConfigService,
              private router: Router,
              private route: ActivatedRoute,
              private modalService: NgxSmartModalService) {
  }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);
    this.dataSub = this.modal.onDataAdded.subscribe((config: CreateLabelModalConfiguration) => {
      this.config = config;
    });
  }

  public onGeo(): void {
    this.labelChoice = LabelChoice.GEOMETRY;
  }

  public onClassification(): void {
    this.labelChoice = LabelChoice.CLASSIFICATION;
  }

  public onClose(): void {
    this.modal.removeData();
    this.config = null;
  }

  ngOnDestroy(): void {
    this.dataSub.unsubscribe();
  }

  public createEntry(choice: LcEntryType): void {
    const parent = this.lcService.findLcEntryById(this.parentId) || null;
    const parentId = parent? parent.id : null;
    this.createLcEntry(choice, parentId);
    if (parent === null) {
      this.lcService.createEntry(this.entry);
    } else {
      parent.children.push(this.entry);
      // Trigger change detection
      parent.children = [].concat(parent.children);
    }
    const entryId = this.entry.id;
    this.modal.close();
    this.router.navigate(['edit', entryId], { relativeTo: this.route }).then();
  }

  private createLcEntry(choice: LcEntryType, parentId: string|null): void {
    switch (choice) {
      case LcEntryType.POINT:
        this.entry = new LcEntryPoint(parentId);
        break;
      case LcEntryType.LINE:
        this.entry = new LcEntryLine(parentId);
        break;
      case LcEntryType.POLYGON:
        this.entry = new LcEntryPolygon(parentId);
        break;
      case LcEntryType.RECTANGLE:
        this.entry = new LcEntryRectangle(parentId);
        break;
      case LcEntryType.IMAGE_SEGMENTATION:
        this.entry = new LcEntryImageSegmentation(parentId);
        break;
      case LcEntryType.SELECT:
        this.entry = new LcEntrySelect(parentId, false);
        break;
      case LcEntryType.CHECKLIST:
        this.entry = new LcEntryChecklist(parentId, false);
        break;
      case LcEntryType.FREE_TEXT:
        this.entry = new LcEntryText(parentId, false);
    }
  }
}
