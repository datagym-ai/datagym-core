import {
  AfterContentChecked,
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {ContextMenuService} from '../../service/context-menu.service';
import {Subject} from 'rxjs';
import {debounceTime, filter, takeUntil} from 'rxjs/operators';
import {ContextMenuEventType} from '../../messaging/ContextMenuEventType';
import {ContextMenuEvent} from '../../messaging/ContextMenuEvent';
import {WorkspacePoint} from '../../model/WorkspacePoint';
import {GeometryType} from '../../geometries/GeometryType';
import {PolygonGeometry} from '../../geometries/geometry/PolygonGeometry';
import {AisegApiService} from '../../service/aiseg-api.service';
import {WorkspaceEvent} from '../../messaging/WorkspaceEvent';
import {WorkspaceEventType} from '../../messaging/WorkspaceEventType';
import {WorkspaceControlService} from '../../service/workspace-control.service';
import {BaseGeometry} from '../../geometries/BaseGeometry';
import {GeometryConfiguration} from '../../model/GeometryConfiguration';


@Component({
  selector: 'app-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.css']
})
export class ContextMenuComponent implements OnInit, AfterViewInit, AfterContentChecked, OnDestroy {
  /**
   * Respect the max. width & height of the underlying element
   * and may change the positions.
   */
  @Input('mediaWidth') private mediaWidth: number = null;
  @Input('mediaHeight') private mediaHeight: number = null;
  @ViewChild('cMenu', {static: true}) cMenu: ElementRef;

  public get xPosition(): number {
    return Math.round(this.position.x);
  }

  public get yPosition(): number {
    return Math.round(this.position.y);
  }

  public hasComment: boolean = false;
  public singleGeometrySelected: boolean = true;
  public comment: string = '';

  public contextGeometries: GeometryConfiguration[] = [];
  public aisegPolygon: PolygonGeometry = undefined;

  public get numberOfSelectedGeometries(): number {
    return this.workspace.selectedGeometries.length || 0;
  }

  public get canUseAISeg(): boolean {
    return this.aiseg.isPrepared && !this.aiseg.aiSegActive;
  }

  /**
   * Context menu height.
   *
   * During creation of the context menu the number of items may changes.
   * The height of the context menu depends on the number of menu items.
   * to prevent useless and unlimited repositions of the context menu store
   * the height to compare if the height had changed until the last view.
   *
   * @private
   */
  private height: number = 0;

  /**
   * Position x & y of current context menu.
   */
  private position: WorkspacePoint;
  private wishPosition: WorkspacePoint;
  private readonly startPoint: WorkspacePoint = new WorkspacePoint(-800, -800);

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  /**
   * This event is internally used to trigger an position change.
   * This way was chosen to use some filter on the event instead of
   * direct changing the position property.
   */
  private recalculatePosition: EventEmitter<void> = new EventEmitter<void>();

  constructor(
    private aiseg: AisegApiService,
    public contextMenu: ContextMenuService,
    private workspace: WorkspaceControlService
  ) {
  }


  ngOnInit(): void {
    this.position = this.startPoint;
    this.wishPosition = this.startPoint;
  }

  ngAfterViewInit(): void {
    this.contextMenu.onShow.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((position: WorkspacePoint) => {
      this.contextMenu.changeContextMenuToOpen();
      this.hasComment = false;
      const selectedGeometries = this.workspace.selectedGeometries;
      this.singleGeometrySelected = selectedGeometries.length === 1;
      this.comment = selectedGeometries[0].geometryProperties.comment;
      if (this.comment != null && this.comment.length > 0) {
        this.hasComment = true;
      }
      this.wishPosition = position;


      this.contextGeometries = this.listPossibleTransformations(selectedGeometries);

      this.aisegPolygon = undefined;
      if (this.workspace.selectedGeometries.length === 1) {
        const selectedGeometry = this.workspace.selectedGeometries[0];
        if (selectedGeometry.geometryType === GeometryType.POLYGON && this.canUseAISeg) {

          // Detect if the polygon was created via aiseg
          const poly = selectedGeometry as PolygonGeometry;
          if (poly.aiSegCalculation !== undefined) {
            this.aisegPolygon = poly;
          }
        }
      }

    });

    const dueTime = 25;
    this.recalculatePosition.pipe(
      debounceTime(dueTime),
      filter(() => !!this.cMenu),
      // recalculate only if the height had changed
      filter(() => this.cMenu.nativeElement.offsetHeight !== this.height),
      // recalculate also only if the wish position is not reached.
      // Note: the wish position may never be reached when it would touch some corner.
      filter(() => !this.position.equals(this.wishPosition)),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.position = this.calculatePosition(this.wishPosition);
      this.height = this.cMenu.nativeElement.offsetHeight;
    });

    this.contextMenu.onClose.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      // To reset the position, reset also the height.
      this.height = 0;
      this.wishPosition = this.startPoint;
    });
  }

  /**
   * The 'real' height of the context menu depends on the
   * context it was called. So it could be necessary to
   * recalculate the position of the context menu once after
   * the content was fully loaded to respect parents border.
   */
  ngAfterContentChecked() {
    this.recalculatePosition.emit();
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

  /**
   * Called to change the geometry.
   *
   * @param target
   */
  public transformGeometry(target: GeometryConfiguration): void {
    const ids = this.workspace.selectedGeometries.map(geo => geo.geometryProperties.identifier);
    this.contextMenu.contextMenuEventBus.next(new ContextMenuEvent(ids, ContextMenuEventType.TRANSFORM, target.lcEntryId));
    this.contextMenu.close();
  }

  public onRefineAISegPrediction(polygon: PolygonGeometry): void {
    const id = polygon.geometryProperties.identifier;
    this.contextMenu.contextMenuEventBus.next(new ContextMenuEvent(id, ContextMenuEventType.REFINE_PREDICTION, polygon));
    this.contextMenu.close();
  }

  /**
   * Handle the hide geometries request.
   */
  public onHide(): void {
    const ids = this.workspace.selectedGeometries.map(geo => geo.geometryProperties.identifier);
    this.contextMenu.contextMenuEventBus.next(new ContextMenuEvent(ids, ContextMenuEventType.HIDE));
    this.contextMenu.close();
  }

  /**
   * Handle the delete request by using the
   * workspace message bus. It's not necessary
   * to write here a new ContextMenuEventType.
   */
  public onDelete(): void {
    this.workspace.onDeleteRequest();
    this.contextMenu.close();
  }

  /**
   * Show the Comment context menu, on close
   * switch back to the original menu.
   */
  public onAddComment(): void {
    this.contextMenu.openCommentMenu();
  }

  /**
   * Handle the save comment request by using the
   * internalWorkspaceEventBus.
   */
  public onSaveComment(): void {
    const geometry = this.workspace.selectedGeometries[0];
    geometry.addCommentDecorator(this.comment);
    this.workspace.sendWorkSpaceEvent(new WorkspaceEvent(geometry.geometryProperties.identifier, WorkspaceEventType.DATA_UPDATED));
    this.contextMenu.close();
  }

  /**
   * Handle the comment Textfield. Prevent numbers from
   * triggering drawing mode
   */
  public onInputChange(event) {
    if (event.code === 'Enter') {
      this.onSaveComment();
    }
    event.stopPropagation();
  }

  /**
   * Handle the delete comment request by using the
   * internalWorkspaceEventBus.
   */
  public onDeleteComment(): void {
    const geometry = this.workspace.selectedGeometries[0];
    geometry.addCommentDecorator('');
    this.workspace.sendWorkSpaceEvent(new WorkspaceEvent(geometry.geometryProperties.identifier, WorkspaceEventType.DATA_UPDATED));
    this.contextMenu.close();
  }

  /**
   * Handle the duplicate request
   */
  public onDuplicate(): void {
    const ids = this.workspace.selectedGeometries.map(geo => geo.geometryProperties.identifier);
    this.contextMenu.contextMenuEventBus.next(new ContextMenuEvent(ids, ContextMenuEventType.DUPLICATE_GEOMETRY, null));
    this.contextMenu.close();
  }

  private listPossibleTransformations(selectedGeometries: BaseGeometry[]): GeometryConfiguration[] {

    const properties = selectedGeometries.map(geo => geo.geometryProperties);

    // Set makes the array unique.
    const types = [...new Set(properties.map(geo => geo.geometryType))];

    /**
     * If different geometry types are selected, we cannot transform them.
     * E.g. a rectangle and a point cannot be converted.
     */
    if (types.length !== 1) {
      return [];
    }

    const selectedType = types[0]; // GeometryType to filter

    /**
     * If geometries from different nesting levels are selected, we cannot transform them.
     * E.g. a root geometry car and a nested geometry car-light cannot converted. Also a
     * child of car and a child of street sign cannot be converted.
     */
    const typedGeometries = properties.filter(geometry => geometry.geometryType === selectedType);
    const parentIds = [...new Set(typedGeometries.map(geo => geo.lcEntryParentId))];
    // parentIds contains either null or one id as string.
    if (parentIds.length !== 1) {
      return [];
    }

    const parentId = parentIds[0]; // lcEntryParentId to filter

    /**
     * All geometries with the same parent (root's parent is `null`)
     * and with the same type as the selected geometries.
     */
    const possibleCandidates = this.workspace.configuration
      .filter(config => config.type === selectedType)
      .filter(config => config.parent === parentId);

    /**
     * If only geometries of the same lcEntry type are selected,
     * filter them out of the possible candidates. E.g. a car cannot converted to itself.
     * If a car and a bus are selected, both options must be available.
     */
    const lcEntryIds = [...new Set(typedGeometries.map(geo => geo.lcEntryId))];
    if (lcEntryIds.length !== 1) {
      return possibleCandidates;
    }

    return possibleCandidates.filter(candidate => candidate.lcEntryId !== lcEntryIds[0]);
  }

  /**
   *
   * @param requestedPosition
   * @param offset
   */
  private calculatePosition(requestedPosition: WorkspacePoint, offset: number = 10): WorkspacePoint {
    // no calculation required.
    if (!this.contextMenu.visible || requestedPosition.equals({x:0, y:0}) || requestedPosition.equals(this.startPoint)) {
      return requestedPosition;
    }

    const defaultWidth = 250;
    const x: number = requestedPosition.x;
    const y: number = requestedPosition.y;
    const w: number = this.cMenu.nativeElement.offsetWidth || defaultWidth;
    const h: number = this.cMenu.nativeElement.offsetHeight || 0;

    if (!/*not*/!!this.mediaWidth || !/*not*/!!this.mediaHeight) {
      return new WorkspacePoint(x, y);
    }

    // calculate the x position: flip on right border if the menu would stick out the parent
    const newX = x + w + offset < this.mediaWidth ? x : x - w;
    // calculate the y position: flip on bottom if the menu would stick out the parent
    const newY = y + h + offset < this.mediaHeight ? y : y - h;

    // Use the y position if the new y would raise the upper corner.
    return new WorkspacePoint(newX, newY < 0 ? y : newY);
  }
}
