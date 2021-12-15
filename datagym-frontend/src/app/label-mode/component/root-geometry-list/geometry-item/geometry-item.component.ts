import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {LcEntryGeometry} from '../../../../label-config/model/geometry/LcEntryGeometry';
import {LabelConfigService} from '../../../../label-config/service/label-config.service';
import {LabelModeUtilityService} from '../../../service/label-mode-utility.service';
import {Subject} from 'rxjs';
import {AisegService} from '../../../service/aiseg.service';
import {LcEntryType} from '../../../../label-config/model/LcEntryType';
import {LcEntry} from '../../../../label-config/model/LcEntry';
import {WorkspaceListenerFilter as Filter} from '../../../service/workspace-listener-filter';
import {WorkspaceEventType} from '../../../../svg-workspace/messaging/WorkspaceEventType';
import {filter, takeUntil} from 'rxjs/operators';
import {WorkspaceControlService} from '../../../../svg-workspace/service/workspace-control.service';
import {EntryValueService} from '../../../service/entry-value.service';


@Component({
  selector: 'app-geometry-item',
  templateUrl: './geometry-item.component.html',
  styleUrls: ['./geometry-item.component.css']
})
export class GeometryItemComponent implements OnInit, OnDestroy {
  @Input('item')
  public item: LcEntryGeometry;

  public get isRoot(): boolean {
    return !/*not*/!!this.item.lcEntryParentId;
  }

  public showNestedGeometries: boolean = this.activated;

  public get isEraser(): boolean {
    return !!this.item && this.item.type === LcEntryType.IMAGE_SEGMENTATION_ERASER;
  }

  public get isHidden(): boolean {
    return this.labelModeUtilityService.hasAnyHiddenValueByEntryId(this.item.id);
  }

  /**
   * Nested geometries are sometimes disabled.
   *
   * They must be grayed out.
   */
  public get color(): string {

    if (this.isRoot) {
      return this.item.color;
    }

    // Note: this color is also known within the global css file as .dg-gray
    const dgGray = '#555555';

    return this.available ? this.item.color : dgGray;
  }

  /**
   * Display the activated dot only if one of the following is true:
   * - the user is currently drawing a new geometry from this type
   * - only values from this geometry are selected
   */
  public get activated(): boolean {
    if (!/*not*/!!this.item) {
      // Should not be possible but sometimes with nested geometries the error `this.item is undefined` occured.
      return false;
    }
    const selectedEntryIds = this.labelModeUtilityService.selectedEntryIds;
    if (selectedEntryIds.length === 1 && selectedEntryIds[0] === this.item.id) {
      return true;
    }

    if (this.labelModeUtilityService.userIsDrawing) {
      return this.labelModeUtilityService.latestCreatedGeometryValue.lcEntryId === this.item.id;
    }

    if (this.aisegService.aiSegActive) {
      return false;
    }

    return false;
  }

  public get available(): boolean {
    const selectedEntryIds = this.labelModeUtilityService.selectedEntryIds;

    return this.labelModeUtilityService.selectedGeometries.length === 1 &&
      selectedEntryIds[0] === this.item.lcEntryParentId ||
      selectedEntryIds[0] === this.item.id;
  }

  public get hasNestedGeometry(): boolean {
    return LcEntry.hasNestedGeometries(this.item);
  }

  /**
   * Return the nested geometries only if the showNestedGeometries flag is true.
   */
  public get nestedGeometries(): LcEntryGeometry[] {
    if (!this.showNestedGeometries) {
      return [];
    }
    if (!this.hasNestedGeometry) {
      return [];
    }

    return (this.item.children || []).filter(child => LcEntryType.isGeometry(child)) as LcEntryGeometry[];
  }

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private aisegService: AisegService,
              private valueService: EntryValueService,
              private labelConfigService: LabelConfigService,
              private workspaceControl: WorkspaceControlService,
              private labelModeUtilityService: LabelModeUtilityService) {
  }

  public get typeIcon(): string {
    return LcEntryType.getIcon(this.item.type);
  }

  public get typeTitle(): string {
    return LcEntryType.getName(this.item.type);
  }

  public get valueCount(): number {
    const valueCounter = this.labelModeUtilityService.getValueCounterByEntryId(this.item.id);
    // On media segmentations, print only 0 for none or 1 for at least one existing segmentation.
    if (this.item.type === LcEntryType.IMAGE_SEGMENTATION) {
      return Math.min(valueCounter, 1);
    }
    return valueCounter;
  }

  ngOnInit(): void {
    const unselectedFilter = Filter.TYPES([WorkspaceEventType.UNSELECTED, WorkspaceEventType.DRAW_FINISHED]);
    this.workspaceControl.eventFilter(unselectedFilter)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        this.showNestedGeometries = false;
      });

    // Toggle the entry value list when a geometry is selected in the workspace.
    const selectedFilter = Filter.TYPE(WorkspaceEventType.SELECTED);
    this.workspaceControl.eventFilter(selectedFilter)
      .pipe(filter(() => this.isRoot))
      .pipe(filter(() => this.hasNestedGeometry))
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => {
        const selectedGeometries = this.workspaceControl.selectedGeometries;
        const selectedProperties = selectedGeometries.map(geo => geo.geometryProperties);

        this.showNestedGeometries = selectedProperties.find(
          geo => geo.lcEntryId === this.item.id || geo.lcEntryParentId === this.item.id
        ) !== undefined;
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onClick(): void {
    if (this.isRoot || this.available) {
      this.labelModeUtilityService.onGeometryOrGlobalClicked(this.item.id);
    }
  }

  /**
   * Emits this entries id if it should be hidden or null if it should be shown again
   */
  public toggleHideEntry(): void {
    this.labelModeUtilityService.toggleHiddenValuesByEntryId(this.item.id);
  }

  public keyListener(keyPressed: KeyboardEvent): void {
    if (keyPressed.target === document.body) {
      let pressedKey = keyPressed.key;
      // Note: shift + geometry shortcut result in special chars depending on the keyboard layout.
      // E.g. the number 0 result in '@' on us keyboards but result in '=' on default german layout.
      if (keyPressed.shiftKey && keyPressed.code.match(/Digit[0-9]/)) {
        pressedKey = keyPressed.code.substr(-1);
        this.aisegService.unselectAllGeometries();
      }
      if (pressedKey === this.item.shortcut) {
        keyPressed.preventDefault();
        this.labelModeUtilityService.onGeometryOrGlobalClicked(this.item.id, () => {
          // if shortcut + shift was pressed, start aiseg (if available)
          const startAiSeg = !!keyPressed.shiftKey && this.item.type === LcEntryType.POLYGON && this.aisegService.isAvailable;
          if (startAiSeg) {

            this.aisegService.initAiseg();
          }
        });
      }
    }
  }
}
