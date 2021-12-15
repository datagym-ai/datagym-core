import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {AppButtonInput} from '../../../../shared/button/button.component';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {Subject, Subscription} from 'rxjs';
import {LcEntryValueStates} from '../../../model/LcEntryValueStates';
import {DialogueModel} from '../../../../shared/dialogue-modal/DialogueModel';
import {LabelModeUtilityService} from '../../../service/label-mode-utility.service';
import {LcEntryGeometryValue} from '../../../model/geometry/LcEntryGeometryValue';
import {ContextMenuService} from '../../../../svg-workspace/service/context-menu.service';
import {WorkspacePoint} from '../../../../svg-workspace/model/WorkspacePoint';
import {ContextMenuWorkerService} from '../../../service/context-menu-worker.service';
import {takeUntil} from 'rxjs/operators';
import {WorkspaceControlService} from '../../../../svg-workspace/service/workspace-control.service';
import {WorkspaceListenerFilter as Filter} from '../../../service/workspace-listener-filter';
import {WorkspaceEventType} from '../../../../svg-workspace/messaging/WorkspaceEventType';
import {LcEntryType} from '../../../../label-config/model/LcEntryType';
import {EntryValueService} from '../../../service/entry-value.service';
import {VideoContextMenuService} from '../../../service/video-context-menu.service';


@Component({
  selector: 'app-entry-value-item',
  templateUrl: './entry-value-item.component.html',
  styleUrls: ['./entry-value-item.component.css']
})
export class EntryValueItemComponent implements OnInit, OnDestroy {
  @Input('value')
  public value: LcEntryGeometryValue;

  public get isRoot(): boolean {
    return !/*not*/!!this.value.lcEntryValueParentId;
  }

  @Input('children')
  public children: LcEntryGeometryValue[] = [];

  private dialogSub: Subscription;

  private unsubscribe: Subject<void> = new Subject<void>();

  public showNestedGeometries: boolean = false;

  public hasNestedGeometries: boolean = false;

  public get hasComment(): boolean {
    return !!this.value && !!this.value.comment;
  }

  get state(): LcEntryValueStates {
    return this.labelModeUtilityService.checkValueEntryState(this.value);
  }

  get isHidden(): boolean {
    return this.state === LcEntryValueStates.HIDDEN;
  }

  get name(): string {
    return this.value.lcEntry.entryValue;
  }

  get colorClass(): string {
    return LcEntryValueStates.getColorClass(this.state);
  }

  get isInvalidFlag(): boolean {
    return !this.value.valid;
  }

  constructor(
    private contextMenu: ContextMenuService,
    private valueService: EntryValueService,
    private dialogueService: DialogueService,
    private workspaceControl: WorkspaceControlService,
    private videoContextMenu: VideoContextMenuService,
    private contextMenuWorker: ContextMenuWorkerService,
    private labelModeUtilityService: LabelModeUtilityService,
  ) { }

  ngOnInit(): void {

    this.hasNestedGeometries = this.valueService.geometries.find(
      geo => geo.lcEntryValueParentId === this.value.id
    ) !== undefined;

    this.showNestedGeometries = this.workspaceControl.openRootGeometryIds.includes(this.value.id);

    // Refresh state if geometry gets selected-/unselected
    const eventFilter = Filter.TYPES([WorkspaceEventType.SELECTED, WorkspaceEventType.UNSELECTED]);
    this.workspaceControl.eventFilter(eventFilter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        this.showNestedGeometries = this.workspaceControl.openRootGeometryIds.includes(this.value.id);
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Opens the Context Menu when the comment symbol in the entry value list is
   * clicked. Selects only the Geometry the comment belongs to, deselects all others.
   * Directly opens edit comment Menu. As location for the context menu, the first
   * corner point of the geometry is chosen.
   */
  public onEditComment(event): void {
    this.contextMenu.close();
    this.videoContextMenu.close();
    this.workspaceControl.selectSingleGeometry(this.value.id);
    const point = this.workspaceControl.selectedGeometries[0].getCornerPoints()[0];
    this.contextMenu.openCommentMenu();
    this.contextMenu.onShow.emit(new WorkspacePoint(point.x, point.y));
    event.stopPropagation();
  }

  /**
   * Checks if already this value is already selected and if not selects it
   */
  public onClick(): void {
    if (this.state !== LcEntryValueStates.HIDDEN && !this.labelModeUtilityService.selectedEntryValueIds.includes(this.value.id)) {
      this.labelModeUtilityService.selectGeometryToEditById(this.value.id);
    }
  }

  public onDeleteValue(): void {
    const title = 'FEATURE.LABEL_MODE.DIALOGUE.TITLE.DELETE_LABEL';
    const content = 'FEATURE.LABEL_MODE.DIALOGUE.CONTENT.DELETE_LABEL';
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = {label: 'GLOBAL.DELETE', styling: 'warn'};
    const dialogueContent: DialogueModel = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    this.dialogSub = this.dialogueService.closeAction.pipe(takeUntil(this.unsubscribe)).subscribe((choice: boolean) => {
      this.dialogSub.unsubscribe();
      if (choice !== true) {
        return;
      }

      /*
       * For media segmentations delete all of them.
       */
      if (this.value.lcEntry.type === LcEntryType.IMAGE_SEGMENTATION) {
        this.labelModeUtilityService.deleteAllGeometriesByEntry(this.value);
      } else {
        this.labelModeUtilityService.deleteGeometryValue(this.value);
      }
    });
  }

  public toggleHideEntry(): void {
    this.labelModeUtilityService.toggleValueVisibility(this.value);
  }
}
