import {Component, Input, OnInit} from '@angular/core';
import {LcEntry} from '../../model/LcEntry';
import {LabelType} from '../../service/label-type-filter.pipe';
import {CreateLabelDialogueService} from '../../service/create-label-dialogue.service';
import {LabelConfiguration} from '../../model/LabelConfiguration';
import {LabelConfigService} from '../../service/label-config.service';
import {EditModeGuardService} from '../../service/edit-mode-guard.service';
import {ActivatedRoute, Router} from '@angular/router';
import {CreateLabelModalConfiguration, LabelChoice} from '../../model/CreateLabelModalConfiguration';
import {Project} from '../../../project/model/Project';
import {Subject} from 'rxjs';
import {MoveTasksDialogueService} from '../../service/move-tasks-dialogue.service';
import {AppButtonInput} from '../../../shared/button/button.component';
import {DialogueModel} from '../../../shared/dialogue-modal/DialogueModel';
import {DialogueService} from '../../../shared/service/dialogue.service';
import {MediaType} from '../../../project/model/MediaType.enum';
import {take} from 'rxjs/operators';
import {takeUntil} from 'rxjs/operators';


@Component({
  selector: 'app-label-list',
  templateUrl: './label-list.component.html',
  styleUrls: ['./label-list.component.css']
})
export class LabelListComponent implements OnInit{
  public lcEntries: LcEntry[];
  @Input()
  public id: string;
  /**
   * Import the interface
   */
  public LabelType = LabelType;

  /**
   * In video projects disallow nested geometries.
   */
  public mediaType: MediaType = MediaType.UNKNOWN;

  public mediaCounter: number = 0;
  /**
   * On VIDEO projects the global classifications are disabled.
   */
  public disableGlobalClassifications: boolean = false;
  /**
   * On VIDEO projects the global classifications are disabled.
   */
  public disableImageSegmentations: boolean = false;
  /**
   * On VIDEO projects the import should be adapted to not accept
   * global classifications and media segmentations.
   *
   * Todo: remove that when the BE is ready.
   */
  public disableImport: boolean = false;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    public labelConfigService: LabelConfigService,
    public editModeGuardService: EditModeGuardService,
    private router: Router,
    private route: ActivatedRoute,
    private diaService: DialogueService,
    private cLDService: CreateLabelDialogueService,
    private moveTasksDialogueService: MoveTasksDialogueService
  ) {}

  public ngOnInit(): void {
    const project = this.route.parent.parent.snapshot.data.project as Project;
    this.mediaCounter = Project.countMedia(project);

    const videoProject = project.mediaType === MediaType.VIDEO;
    this.disableGlobalClassifications = videoProject;
    this.disableImageSegmentations = videoProject;
    this.disableImport = videoProject;
    this.mediaType = project.mediaType;
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Using setter to update errorMessageGroups on changes to lcEntries
   * @param entries
   */
  @Input()
  public set entries(entries: LcEntry[]) {
    this.lcEntries = entries;
  }

  public createRootGeometry(): void {
    if (this.editModeGuardService.editMode) {
      return;
    }
    const choice = !this.disableGlobalClassifications ? LabelChoice.GEOMETRY: LabelChoice.GEOMETRY_WITHOUT_SEGMENTATION;
    const config = new CreateLabelModalConfiguration(null, 0, choice);
    this.cLDService.openCreateDialogue(config);
  }

  public createMediaClassification(): void {
    if (this.editModeGuardService.editMode) {
      return;
    }
    if (this.disableGlobalClassifications) {
      return;
    }
    const config = new CreateLabelModalConfiguration(null, 0, LabelChoice.CLASSIFICATION);
    this.cLDService.openCreateDialogue(config);
  }

  public onSaveConfig(): void {
    if (this.labelConfigService.numberOfCompletedTasks === 0
      && this.labelConfigService.numberOfReviewedTasks === 0
      || (!this.labelConfigService.hasNewLabels && !this.labelConfigService.changedRequiredToTrue)) {
      /* if no completed or reviewed task would be affected
      * or no new entries were added
      * and no classification required value was changed from false to true
      * then just save
      */
      this.saveConfigInBackend(true);
      return;
    }

    this.moveTasksDialogueService.openDialogue(this.labelConfigService.numberOfCompletedTasks + this.labelConfigService.numberOfReviewedTasks);
    this.moveTasksDialogueService.closeAction.pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((choice: boolean) => {
      this.saveConfigInBackend(choice);
    });
  }

  public onExport(): void {
    // Don't allow the export while the config is dirty / not saved.
    if (this.editModeGuardService.editMode || this.labelConfigService.dirty) {
      return;
    }
    const url = this.labelConfigService.getExportConfigUrl(this.id);
    window.open(url, '_blank');
  }

  private saveConfigInBackend(changeCompletedTasksStatus: boolean): void {
    /**
     * Prepare the entry list before sending.
     *
     * The entries contain some additional attributes like an timestamp
     * that came from the api but are not accepted in the body of the
     * update api endpoint. Such attributes are recursively removed here.
     * Also internal id's are removed.
     *
     * @param entry LcEntry (with some additional attributes)
     */
    const prepareToSend = (entry: LcEntry): LcEntry => {
      const clone = entry;
      // cleanup the id if not 'official'.
      clone.id = clone.id || '';
      if (!clone.id || clone.id.startsWith('internal_')) {
        delete clone.id;
      }
      // recursive walk
      clone.children.forEach((child: LcEntry) => prepareToSend(child));
      return clone;
    };
    const payload: LcEntry[] = this.lcEntries.map((entry: LcEntry) => prepareToSend(entry));
    this.labelConfigService.updateLabelConfig(this.id, payload, changeCompletedTasksStatus).subscribe((labelConfiguration: LabelConfiguration) => {
      this.labelConfigService.init(labelConfiguration);
    });
  }

  private deleteConfigInBackend(): void {
    this.labelConfigService.deleteLabelConfig(this.id).subscribe((labelConfiguration) => {
      this.labelConfigService.init(labelConfiguration);
      this.labelConfigService.doneEditing.emit();
    });

  }

  public onDeleteConfig() {
    const title = 'GLOBAL.DIALOGUE.TITLE.CONFIRM_DELETE';
    const content = 'FEATURE.LABEL_CONFIG.DIALOGUE.DELETE_CONFIG';
    const contentParams = {tasks: this.mediaCounter};
    const confirm = 'GLOBAL.CONFIRM';
    const cancel = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = { label: confirm, styling: 'warn' };
    const dialogueContent: DialogueModel = { title, content, buttonLeft, buttonRight: cancel, contentParams};
    this.diaService.openDialogue(dialogueContent);
    this.diaService.closeAction.pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((choice: boolean) => {
      if (choice) {
        this.deleteConfigInBackend();
        this.router.navigate(['../'], { relativeTo: this.route }).then();
      }
    });
  }
}
