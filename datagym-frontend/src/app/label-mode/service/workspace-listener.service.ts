import {Injectable} from '@angular/core';
import {WorkspaceEventType} from '../../svg-workspace/messaging/WorkspaceEventType';
import {takeUntil} from 'rxjs/operators';
import {WorkspaceEvent} from '../../svg-workspace/messaging/WorkspaceEvent';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {PolygonGeometryData} from '../../svg-workspace/geometries/geometry-data/PolygonGeometryData';
import {LineGeometryData} from '../../svg-workspace/geometries/geometry-data/LineGeometryData';
import {WorkspaceListEvent} from '../../svg-workspace/messaging/WorkspaceListEvent';
import {AppButtonInput} from '../../shared/button/button.component';
import {DialogueModel} from '../../shared/dialogue-modal/DialogueModel';
import {Subject, Subscription} from 'rxjs';
import {WorkspaceControlService} from '../../svg-workspace/service/workspace-control.service';
import {EntryValueService} from './entry-value.service';
import {LabelModeUtilityService} from './label-mode-utility.service';
import {DialogueService} from '../../shared/service/dialogue.service';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {WorkspaceInternalService} from '../../svg-workspace/service/workspace-internal.service';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {WorkspaceListenerFilter as Filter} from './workspace-listener-filter';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {GeometryType} from '../../svg-workspace/geometries/GeometryType';
import {GeometryProperties} from '../../svg-workspace/geometries/GeometryProperties';
import {VideoContextMenuService} from './video-context-menu.service';
import {LabelModeType} from '../model/import';

@Injectable({
  providedIn: 'root'
})
export class WorkspaceListenerService {
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private valueService: EntryValueService,
    private dialogueService: DialogueService,
    private workspace: WorkspaceInternalService,
    private videoContextMenu: VideoContextMenuService,
    private notificationService: LabNotificationService,
    private workspaceController: WorkspaceControlService,
    private labelModeUtilityService: LabelModeUtilityService,
  ) { }

  /**
   * Unsubscribe to all events und unselectAllGeometries
   * Gets called by label-mode.component onDestroy lifecycle hook
   */
  public reset(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
    this.workspaceController.unselectAllGeometries();
  }

  /**
   * Wrapper method for setting up all necessary listeners on workspace events
   */
  public initializeWorkspaceListeners(): void {
    this.unsubscribe = new Subject<void>();
    this.initializeWorkspaceInitializedListener();
    this.initializeWorkspaceReadyListener();
    this.initializeWorkspaceDrawFinishedListener();
    this.initializeWorkspaceSelectedListener();
    this.initializeWorkspaceDeleteRequestListener();
    this.initializeWorkspaceDeleteWithoutRequestListener();
    this.initializeWorkspaceDataUpdatedListener();
    this.initializeWorkspaceCanceledListener();
    this.initializeWorkspaceUnselectedListener();
    this.initializeWorkspaceCreateSegmentGeometry();
    this.initializeWorkspaceContextMenuListener();
  }

  /**
   * Listener on WORKSPACE_INITIALIZED
   * Loads media when fired
   */
  private initializeWorkspaceInitializedListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.WORKSPACE_INITIALIZED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        this.workspaceController.loadMedia(
          this.valueService.url,
          this.valueService.mediaId,
          this.valueService.labelModeType
        );
      });
  }

  /**
   * Listener on WORKSPACE_READY event
   * Calls initSavedGeometriesFromValues when fired
   */
  private initializeWorkspaceReadyListener() {
    const filter = Filter.TYPE(WorkspaceEventType.WORKSPACE_READY);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        this.valueService.workspaceReady = true;
        this.valueService.initSavedGeometriesFromValues();
      });
  }

  /**
   * Listener on SELECTED event
   * SELECTED event fires multiple times while drawing but we only care about events outside of drawing
   * Selects a valueTree if it is not already selected
   */
  private initializeWorkspaceSelectedListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.SELECTED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceEvent: WorkspaceEvent) => {
        if (!this.labelModeUtilityService.userIsDrawing) {
          const rootValue = this.valueService.geometries.find(value => value.id === workspaceEvent.internalId);
          this.labelModeUtilityService.selectValueTreeByGeometryValue(rootValue);
        }
      });
  }

  /**
   * Listener on workspace UNSELECTED event
   * Updates geometry and resets selectionState if needed
   */
  private initializeWorkspaceUnselectedListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.UNSELECTED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        if (this.labelModeUtilityService.selectedEntryValueIds.length > 0) {
          this.labelModeUtilityService.resetSelectionState();
        }
      });
  }

  /**
   * Listener on DATA_UPDATED event
   * Updates geometry when fired
   */
  private initializeWorkspaceDataUpdatedListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.DATA_UPDATED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((event: WorkspaceEvent) => {
        this.valueService.updateGeometryById(event.internalId);

        if (this.workspace.checkIfGeometryIsOutsideBoundariesByIdentifier(event.internalId)) {
          const boundaryError = 'FEATURE.LABEL_MODE.ERROR.OUT_OF_BOUNDS';
          this.notificationService.info_i18(boundaryError);
        }
      });
  }

  /**
   * Listener on workspace CANCELED event
   * Deletes the canceled value and resets drawing state
   */
  private initializeWorkspaceCanceledListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.CANCELED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceEvent: WorkspaceEvent) => {
        if (workspaceEvent.internalId === this.labelModeUtilityService.latestCreatedGeometryValue.id) {
          this.labelModeUtilityService.deleteGeometryValue(this.labelModeUtilityService.latestCreatedGeometryValue);
          this.labelModeUtilityService.userIsDrawing = false;
          this.labelModeUtilityService.latestCreatedGeometryValue = undefined;
          this.labelModeUtilityService.activateGeometry.emit('');
          this.labelModeUtilityService.resetSelectionState();
        }
      });
  }

  /**
   * Listener on DRAW_FINISHED event
   * POLYGON and LINE are special cases and get canceled if not valid
   * Updates geometry and selects its valueTree
   * Sets drawing state to false and clears activatedParent
   */
  private initializeWorkspaceDrawFinishedListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.DRAW_FINISHED);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceEvent: WorkspaceEvent) => {
        if (!/*not*/!!this.labelModeUtilityService.latestCreatedGeometryValue) {
          // reset draw state
          this.labelModeUtilityService.userIsDrawing = false;
          this.labelModeUtilityService.activateGeometry.emit('');
          return;
        }
        if (this.labelModeUtilityService.latestCreatedGeometryValue.lcEntry.type === LcEntryType.POLYGON) {
          // A POLYGON needs to have 3 points to be valid
          const polyData: PolygonGeometryData = this.workspaceController.getGeometryData(workspaceEvent.internalId) as PolygonGeometryData;
          const requiredPoints = 3;
          if (polyData.points.length < requiredPoints) {
            // If invalid POLYGON, cancel it and notify user
            this.workspaceController.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
            const polygonError = 'FEATURE.LABEL_MODE.ERROR.POLYGON_POINT_COUNT';
            this.notificationService.info_i18(polygonError);
            return;
          }
        }
        if (this.labelModeUtilityService.latestCreatedGeometryValue.lcEntry.type === LcEntryType.LINE) {
          // A LINE needs 2 points to be valid
          const lineData: LineGeometryData = this.workspaceController.getGeometryData(workspaceEvent.internalId) as LineGeometryData;
          const requiredPoints = 2;
          if (lineData.points.length < requiredPoints) {
            // If invalid LINE, cancel it and notify user
            this.workspaceController.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
            const lineError = 'FEATURE.LABEL_MODE.ERROR.LINE_POINT_COUNT';
            this.notificationService.info_i18(lineError);
            return;
          }
        }
        if (this.workspace.checkIfGeometryIsOutsideBoundariesByIdentifier(workspaceEvent.internalId)) {
          // reset draw state
          this.labelModeUtilityService.userIsDrawing = false;
          this.labelModeUtilityService.activateGeometry.emit('');
          // Error message.
          const boundaryError = 'FEATURE.LABEL_MODE.ERROR.OUT_OF_BOUNDS';
          this.notificationService.info_i18(boundaryError);
        }
        if (this.labelModeUtilityService.latestCreatedGeometryValue.lcEntry.type !== LcEntryType.IMAGE_SEGMENTATION_ERASER) {
          // add the geometry to the value stack
          this.valueService.geometries.push(this.labelModeUtilityService.latestCreatedGeometryValue);
          this.valueService.updateGeometryById(workspaceEvent.internalId, true);
          this.labelModeUtilityService.selectValueTreeByGeometryValue(this.labelModeUtilityService.latestCreatedGeometryValue);
        }
        // reset draw state
        this.labelModeUtilityService.userIsDrawing = false;
        this.labelModeUtilityService.activateGeometry.emit('');

        if (this.workspace.mediaType === LabelModeType.VIDEO) {
          // Fast labeling for geometries without classifications is not
          // supported by now. It throws some null-pointers.
          return;
        }

        // if the geometry has no nested classifications:
        const latest = this.labelModeUtilityService.latestCreatedGeometryValue;
        if (latest.children.length === 0) {
          // Payload controls if the auto-next-draw should be active
          if (workspaceEvent.payload !== undefined && workspaceEvent.payload === false) {
            return;
          }
          // select the same geometry so the user can label the next
          // area without using the shortcuts or click into the entry list.
          this.labelModeUtilityService.onGeometryOrGlobalClicked(latest.lcEntry.id);
          this.labelModeUtilityService.activateGeometry.emit(latest.lcEntry.id);
        }
      });
  }

  /**
   * Listener on DELETE_REQUEST event
   * Shows confirmation dialogue before deleting geometry
   * Deletes all given geometries if user confirms
   */
  private initializeWorkspaceDeleteRequestListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.DELETE_REQUEST);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceListEvent: WorkspaceListEvent) => {
        const dialogueModel: DialogueModel = WorkspaceListenerService.buildDialogueModelFromListEvent(workspaceListEvent);
        this.dialogueService.openDialogue(dialogueModel);
        const dialogSub: Subscription = this.dialogueService.closeAction.subscribe((choice: boolean) => {
          dialogSub.unsubscribe();
          if (choice !== true) {
            return;
          }
          this.deleteWithoutRequest(workspaceListEvent.internalIdList);
        });
      });
  }

  /**
   * Listener on DELETE_REQUEST event
   * Shows confirmation dialogue before deleting geometry
   * Deletes all given geometries if user confirms
   */
  private initializeWorkspaceDeleteWithoutRequestListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.DELETE_WITHOUT_REQUEST);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceListEvent: WorkspaceListEvent) => {
        this.deleteWithoutRequest(workspaceListEvent.internalIdList);
      });
  }

  /**
   * Listener on CREATE_SEGMENT_GEOMETRY event
   * Creates a new geometry value and then updates it's
   * geometry properties without some further user interactions.
   */
  private initializeWorkspaceCreateSegmentGeometry(): void {
    const filter = Filter.TYPE(WorkspaceEventType.CREATE_SEGMENT_GEOMETRY);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((workspaceEvent: WorkspaceEvent) => {
        const rootGeometry = this.valueService.geometries.find(geometry => geometry.id === workspaceEvent.internalId);
        if (rootGeometry === undefined) {
          // should not be possible but appeared sometimes with ImageSegmentationGeometry
          return;
        }
        const rootEntry = rootGeometry.lcEntry as LcEntryGeometry;
        this.valueService.createValuesByGeometry(rootEntry).subscribe(createdGeometry => {
          // Segment geometries cannot have a parent geometry.
          const extended = {...rootEntry, icon: LcEntryType.getIcon(rootEntry.type), lcEntryValueParentId: undefined};
          const geometryProperties = new GeometryProperties(createdGeometry.id, GeometryType.IMAGE_SEGMENTATION, extended);
          this.workspaceController.createGeometry(geometryProperties, workspaceEvent.payload);
          this.valueService.geometries.push(createdGeometry);
          this.valueService.updateGeometryById(createdGeometry.id);
        });
      });
  }

  /**
   * Listener on CONTEXT_MENU_OPEN
   * Close the video line context menu when the context menu within the
   * workspace is loaded.
   */
  private initializeWorkspaceContextMenuListener(): void {
    const filter = Filter.TYPE(WorkspaceEventType.CLOSE_VIDEO_CONTEXT_MENU);
    this.workspaceController.eventFilter(filter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        this.videoContextMenu.close();
      });
  }


  /**
   * Helper method to share the code used to delete all listed values.
   *
   * This method doesn't warn the user!
   *
   * @param ids
   */
  private deleteWithoutRequest(ids: string[]): void {
    ids.forEach((value: string) => {
      const valueToDelete: LcEntryGeometryValue = this.valueService.geometries
        .find((searchValue: LcEntryGeometryValue) => searchValue.id === value);
      if (valueToDelete !== undefined) {
        this.labelModeUtilityService.deleteGeometryValue(valueToDelete);
      }
    });
  }

  /**
   * Helper-method which builds the DialogueModel for delete-requests from a given workspaceListEvent
   * @param workspaceListEvent
   */
  private static buildDialogueModelFromListEvent(workspaceListEvent: WorkspaceListEvent): DialogueModel {
    let title: string;
    let content: string;
    let titleParams: {};
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = {label: 'GLOBAL.DELETE', styling: 'warn'};
    let dialogueContent: DialogueModel;
    if (workspaceListEvent.internalIdList.length > 1) {
      title = 'FEATURE.LABEL_MODE.DIALOGUE.TITLE.DELETE_LABEL_LIST';
      content = 'FEATURE.LABEL_MODE.DIALOGUE.CONTENT.DELETE_LABEL_LIST';
      titleParams = {labelCount: workspaceListEvent.internalIdList.length};
      dialogueContent = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn, titleParams};
    } else {
      title = 'FEATURE.LABEL_MODE.DIALOGUE.TITLE.DELETE_LABEL';
      content = 'FEATURE.LABEL_MODE.DIALOGUE.CONTENT.DELETE_LABEL';
      dialogueContent = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    }
    return dialogueContent;
  }
}
