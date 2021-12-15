import {Component, Input, OnDestroy} from '@angular/core';
import {LcEntry} from '../../../model/LcEntry';
import {ActivatedRoute, Router} from '@angular/router';
import {CreateLabelDialogueService} from '../../../service/create-label-dialogue.service';
import {LabelConfigService} from '../../../service/label-config.service';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {Subject} from 'rxjs';
import {AppButtonInput} from '../../../../shared/button/button.component';
import {EditModeGuardService} from '../../../service/edit-mode-guard.service';
import {LcEntryGeometry} from '../../../model/geometry/LcEntryGeometry';
import {DialogueModel} from '../../../../shared/dialogue-modal/DialogueModel';
import {CreateLabelModalConfiguration, LabelChoice} from '../../../model/CreateLabelModalConfiguration';
import {LcEntryType} from '../../../model/LcEntryType';
import {LcEntryFactory} from '../../../model/LcEntryFactory';
import {take} from 'rxjs/operators';
import {takeUntil} from 'rxjs/operators';
import {MediaType} from '../../../../project/model/MediaType.enum';


@Component({
  selector: 'app-entry-item',
  templateUrl: './entry-item.component.html',
  styleUrls: ['./entry-item.component.css']
})
export class EntryItemComponent implements OnDestroy {
  @Input()
  public readonly lcEntries: LcEntry[];
  @Input()
  public readonly entry: LcEntry;
  @Input()
  public readonly nestingLevel: number;

  /**
   * In video projects disallow nested geometries.
   */
  @Input()
  public readonly mediaType: MediaType = MediaType.UNKNOWN;

  // for displaying LcEntryType.RADIO as LcEntryType.SELECT.
  public readonly LcEntryType = LcEntryType;

  public get color(): string {
    return LcEntryType.isGeometry(this.entry.type) ? (this.entry as LcEntryGeometry).color : '';
  }

  public get hasShortcut(): boolean {
    if (!LcEntryType.isGeometry(this.entry.type)) {
      return false;
    }

    const geometry = this.entry as LcEntryGeometry;
    return geometry.shortcut !== undefined && geometry.shortcut !== null;
  }

  public get shortcut(): string {
    if (this.hasShortcut) {
      return (this.entry as LcEntryGeometry).shortcut;
    }
    // should not be possible / *ngIf is used in template file.
    return '';
  }

  public get inEditMode(): boolean {
    return this.editModeGuard.editMode;
  }

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private editModeGuard: EditModeGuardService,
    private cLDService: CreateLabelDialogueService,
    private diaService: DialogueService,
    public lcService: LabelConfigService
  ) { }

  public onCreateChildOfEntry(): void {
    if (this.inEditMode) {
      // if another entry is under editing, don't allow this action.
      return;
    }

    const isVideoProject = this.mediaType === MediaType.VIDEO;
    const labelChoice = isVideoProject || LcEntryType.isClassification(this.entry)
      ? LabelChoice.CLASSIFICATION
      : LabelChoice.NONE;

    const config = new CreateLabelModalConfiguration(this.entry.id, this.nestingLevel + 1, labelChoice);
    this.cLDService.openCreateDialogue(config);
  }

  public onEditEntry(): void {
    if (this.inEditMode) {
      // if another entry is under editing, don't allow this action.
      return;
    }
    this.router.navigate(['edit', this.entry.id], {relativeTo: this.route}).then();
  }

  public onDeleteEntry(): void {
    if (this.inEditMode) {
      // if another entry is under editing, don't allow this action.
      return;
    }
    const title = 'GLOBAL.DIALOGUE.TITLE.CONFIRM_DELETE';
    const content = 'FEATURE.LABEL_CONFIG.DIALOGUE.DELETE_ENTRY';
    const confirm = 'GLOBAL.CONFIRM';
    const cancel = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = {label: confirm, styling: 'warn'};
    const dialogueContent: DialogueModel = {title, content, buttonLeft, buttonRight: cancel};
    this.diaService.openDialogue(dialogueContent);
    this.diaService.closeAction.pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((choice: boolean) => {
      if (choice) {
        this.lcService.deleteEntryById(this.entry.id);
        this.router.navigate(['./'], {relativeTo: this.route}).then();
      }
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  onCopyEntry(): void {
    if (this.inEditMode) {
      return;
    }

    let newEntry = LcEntryFactory.copyEntry(this.entry);
    newEntry = this.lcService.renameEntryToCopy(newEntry);

    if (LcEntryType.isGeometry(newEntry.type)) {
      // change the color for all children with the same color as the parent as well.
      const oldColor = (newEntry as LcEntryGeometry).color;
      const newColor = this.lcService.getNextColor();
      (newEntry as LcEntryGeometry).color = newColor;
      (newEntry as LcEntryGeometry).shortcut = this.lcService.getNextShortcut();

      (newEntry.children || [])
        .filter(child => LcEntryType.isGeometry(child))
        .filter(child => (child as LcEntryGeometry).color === oldColor)
        .forEach(child => (child as LcEntryGeometry).color = newColor);
    }

    this.lcService.createEntry(newEntry);
    this.lcService.dirty = true;
    this.router.navigate(['edit', newEntry.id], {relativeTo: this.route}).then();
  }
}
