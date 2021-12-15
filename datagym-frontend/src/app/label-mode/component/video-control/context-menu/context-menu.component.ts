import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Subject} from 'rxjs';
import {Point} from '../../../model/geometry/Point';
import {VideoContextMenuService} from '../../../service/video-context-menu.service';
import {takeUntil} from 'rxjs/operators';
import {LabelModeUtilityService} from '../../../service/label-mode-utility.service';
import {ContextMenuConfig} from '../../../model/video/ContextMenuConfig';
import {AppButtonInput} from '../../../../shared/button/button.component';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {take} from 'rxjs/operators';

/**
 * This is the context menu for the video value lines.
 *
 * Don't confuse with the context menu used within the
 * `svg-workspace` used to control the geometries.
 */
@Component({
  selector: 'app-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.css']
})
export class ContextMenuComponent implements OnInit {

  /**
   * Position x & y of current context menu.
   * To prevent position is undefined errors in template, set to a default one.
   */
  public get position(): Point {
    return !!this.config && !!this.config.position
      ? this.config.position
      : new Point(0, 0);
  }

  @ViewChild('cMenu', {static: true}) cMenu: ElementRef;

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  public isHidden: boolean = false;

  public get visible(): boolean {
    return this.config !== undefined;
  }

  public get valueId(): string {
    return this.config.valueId;
  }

  public get displayDeleteKeyFrameAction(): boolean {
    return !!this.config && !!this.config.displayDeleteKeyFrameAction;
  }

  public config: ContextMenuConfig = undefined;

  constructor(
    private dialogueService: DialogueService,
    private labelMode: LabelModeUtilityService,
    private contextMenu: VideoContextMenuService
  ) { }

  ngOnInit(): void {

    this.contextMenu.onOpen.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(config => {
      this.config = config;
      if (!/*not*/!!config) {
        // prevent position is undefined
        return;
      }
      this.isHidden = this.labelMode.isValueHidden(this.valueId);
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  handleClick($event: MouseEvent) {
    // Mouse clicks within the context menu should not
    // trigger the onClick event of the underlying svg element.
    // That would close the context menu.
    $event.stopPropagation();
  }

  /**
   * Handle the close icon within the context menu.
   */
  public onCloseIcon(): void {
    this.contextMenu.close();
  }


  public onHighlight(): void {
    this.labelMode.highlightGeometry(this.valueId);
    this.onCloseIcon();
  }

  /**
   * Toggle value's visibility.
   */
  public toggleVisibility(): void {
    this.labelMode.toggleValueVisibility(this.valueId);
    this.onCloseIcon();
  }

  public onDelete(): void {

    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = {label: 'GLOBAL.DELETE', styling: 'warn'};
    const title = 'FEATURE.LABEL_MODE.DIALOGUE.TITLE.DELETE_LABEL';
    const content = 'FEATURE.LABEL_MODE.DIALOGUE.CONTENT.DELETE_LABEL';
    const dialogueContent = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};

    const contextMenuConfig = this.config;
    this.dialogueService.openDialogue(dialogueContent);
    this.dialogueService.closeAction.pipe(take(1), takeUntil(this.unsubscribe)).subscribe(choice => {
      if (choice !== true) {
        return;
      }
      if (contextMenuConfig.displayDeleteKeyFrameAction) {
        // Just delete the keyframe.
        this.contextMenu.onDeleteKeyFrame.emit([contextMenuConfig.valueId, contextMenuConfig.frameNumber]);
      } else {
        // Delete the full line.
        this.contextMenu.onDeleteValue.emit(contextMenuConfig.valueId);
      }
    });
    this.onCloseIcon();
  }

  public onExpandLeft(): void {
    this.contextMenu.onExpand.emit([this.valueId, this.config.chunkOffset, true]);
    this.onCloseIcon();
  }

  public onExpandRight(): void {
    this.contextMenu.onExpand.emit([this.valueId, this.config.chunkOffset, false]);
    this.onCloseIcon();
  }
}
