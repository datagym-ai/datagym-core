import {EventEmitter, Injectable} from '@angular/core';
import {EntryValueService} from './entry-value.service';
import {WorkspaceControlService} from '../../svg-workspace/service/workspace-control.service';
import {LcEntryValue} from '../model/LcEntryValue';
import {LcEntryValueStates} from '../model/LcEntryValueStates';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {GeometryType} from '../../svg-workspace/geometries/GeometryType';
import {EntryConfigService} from './entry-config.service';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../model/classification/LcEntryClassificationValue';
import {LcEntryImageSegmentationEraserValue} from '../model/geometry/LcEntryImageSegmentationEraserValue';
import {GeometryProperties} from '../../svg-workspace/geometries/GeometryProperties';
import {LcEntry} from '../../label-config/model/LcEntry';
import {filter} from 'rxjs/operators';
import {take, takeUntil} from 'rxjs//operators';
import {VideoControlService} from './video-control.service';
import {LabelModeType} from '../model/import';


@Injectable({
  providedIn: 'root'
})
export class LabelModeUtilityService {

  public get selectedGeometries(): LcEntryGeometryValue[] {
    const selected = this.workspaceController.selectedGeometryIds;
    return this.valueService.geometries.filter(geo => selected.includes(geo.id));
  }

  public get selectedEntryIds(): string[] {
    const entryIds = this.selectedGeometries.map(geo => geo.lcEntryId);
    // Make the list unique.
    return [...new Set(entryIds)];
  }

  public get selectedEntryValueIds(): string[] {
    return this.selectedGeometries.map(geo => geo.id);
  }

  // draw state
  public latestCreatedGeometryValue: LcEntryGeometryValue;
  public userIsDrawing: boolean = false;
  // classification-list state
  public rootEntryName: string;
  public valueTree: LcEntryClassificationValue[] = [];
  /**
   * Emits currently selected valueTree and changes to it
   */
  public valueTree$: BehaviorSubject<LcEntryClassificationValue[]> =
    new BehaviorSubject<LcEntryClassificationValue[]>(this.valueTree);
  // set activated-dot in geometry-item by geometry id, empty string clears all.
  public activateGeometry: EventEmitter<string> = new EventEmitter<string>();

  // Should be called if the hidden state gets changed
  public onHiddenStateChanged: BehaviorSubject<string> = new BehaviorSubject<string>('');

  public get aiSegActive(): boolean {
    return this.workspaceController.aiSegActive;
  }

  public get selectedMediaClassifications(): boolean {
    return this.rootEntryName === LabelModeUtilityService.IMAGE_CLASSIFICATIONS_IDENTIFIER;
  }

  /**
   * Setter which holds selectedValueTree state and emits changes
   * @param tree
   */
  public set selectedValueTree(tree: LcEntryClassificationValue[] | []) {
    this.valueTree = tree;
    this.valueTree$.next(tree);
  }

  /**
   * Replaces hiddenEntryIds and works on value ids
   * instead of config ids.
   */
  private hiddenValuesIds: string[] = [];

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  private static IMAGE_CLASSIFICATIONS_IDENTIFIER: string = 'Image Classifications';

  constructor(
    private valueService: EntryValueService,
    private configService: EntryConfigService,
    private videoControl: VideoControlService,
    private workspaceController: WorkspaceControlService,
    private labNotificationService: LabNotificationService
  ) {
  }

  public unselectAllGeometries(): void {
    this.workspaceController.unselectAllGeometries();
  }

  public cancelAISeg(): void {
    this.workspaceController.cancelAISeg();
  }

  /**
   * Translates LcEntryTypes to GeometryTypes for usage in the workspace
   * @param inType LcEntryType
   * @return undefined if type is not a geometry
   */
  public static translateTypes(inType: LcEntryType): GeometryType | undefined {
    switch (inType) {
      case LcEntryType.RECTANGLE:
        return GeometryType.RECTANGLE;
      case LcEntryType.POLYGON:
        return GeometryType.POLYGON;
      case LcEntryType.POINT:
        return GeometryType.POINT;
      case LcEntryType.LINE:
        return GeometryType.LINE;
      case LcEntryType.IMAGE_SEGMENTATION:
        return GeometryType.IMAGE_SEGMENTATION;
      case LcEntryType.IMAGE_SEGMENTATION_ERASER:
        return GeometryType.IMAGE_SEGMENTATION_ERASER;
      // should not be possible
      default:
        return undefined;
    }
  }

  /**
   * Setups the internal states.
   * Called once by label-mode.component ngOnInit
   */
  public init(): void {
    this.valueService.valuesLoaded$.pipe(
      filter(loaded => !!loaded),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      this.workspaceController.hideGeometry(this.hiddenValuesIds);
    });
    /*
     * Within the video label mode close the classifications bar
     * on every frame change.
     */
    this.videoControl.onFrameChanged.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      if (this.valueTree.length > 0) {
        this.selectedValueTree = [];
      }
    });
  }

  /**
   * Resets internal state
   * called by label-mode.component ngOnDestroy
   */
  public reset(): void {
    this.hiddenValuesIds = [];
    this.latestCreatedGeometryValue = undefined;
    this.userIsDrawing = false;
    this.resetSelectionState();
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * Helper-method to reset selection state
   */
  public resetSelectionState(): void {
    this.rootEntryName = null;
    this.selectedValueTree = [];
  }

  /**
   * Select a geometry to edit, SELECTED event will call selectValueTreeByGeometryValue
   * @param geoToEditId
   */
  public selectGeometryToEditById(geoToEditId: LcEntryGeometryValue | string) {
    if (this.userIsDrawing === true) {
      this.workspaceController.cancelAndDeleteGeometry(this.latestCreatedGeometryValue.id);
    }
    const id = typeof geoToEditId === 'string' ? geoToEditId : geoToEditId.id;
    this.workspaceController.selectSingleGeometry(id);
  }

  /**
   * Iterate through the list of geometries and select the next or previous geometry.
   * @param next
   */
  public iterateSelectedValues(next: boolean): void {
    const visibleGeometries = this.valueService.geometries.filter(geo => !this.isValueHidden(geo));
    if (visibleGeometries.length === 0) {
      // No visible geometries.
      return;
    }
    const selectedGeometries = this.selectedEntryValueIds;
    if (selectedGeometries.length === 0) {
      // Nothing is selected. Select the first geometry.
      this.selectGeometryToEditById(visibleGeometries[0]);
      return;
    }
    if (selectedGeometries.length > 1) {
      // More than one geometry is selected, do nothing.
      return;
    }

    const selectedGeometry = this.valueService.geometries.find(geo => geo.id === selectedGeometries[0]);
    const selectedGeometryIndex: number = this.valueService.geometries.indexOf(selectedGeometry);

    // On left arrow select the previous item, on right arrow select the next item.
    let geometryIdToSelect = selectedGeometryIndex + (next ? 1 : -1);
    // But don't rotate / make sure the next index is within the range of the geometries.
    geometryIdToSelect = Math.max(0, geometryIdToSelect);
    geometryIdToSelect = Math.min(geometryIdToSelect, this.valueService.geometries.length - 1);

    const geometryToSelect = this.valueService.geometries[geometryIdToSelect];
    this.selectGeometryToEditById(geometryToSelect);
  }

  /**
   * Called by entry-value-item, sets up necessary state to edit an entry-value and selects its geometry
   * @param valueToEdit
   */
  public selectValueTreeByGeometryValue(valueToEdit: LcEntryGeometryValue): void {
    const numberOfSelectedGeometries = this.workspaceController.numberOfSelectedGeometries;
    if (numberOfSelectedGeometries > 1 && this.valueTree.length > 0) {
      // fade in the classification bar
      this.selectedValueTree = [];
    }
    // Todo: extract to handler with interface.
    if (valueToEdit !== undefined && valueToEdit !== null && numberOfSelectedGeometries === 1) {
      let classifications = valueToEdit.getNestedClassifications();
      if (this.configService.mediaType === LabelModeType.VIDEO) {
        const currentFrameNumber = this.videoControl.currentFrameNumber;

        const handler = (child: LcEntryClassificationValue): LcEntryClassificationValue => {
          child.children = child.children.map(handler);
          const usedFrame = Math.max(...child.frameNumbers.filter(n => n <= currentFrameNumber));
          return child.applyChange(usedFrame) as LcEntryClassificationValue;
        }

        classifications = classifications.map(handler);
      }
      this.selectedValueTree = classifications;

      this.rootEntryName = valueToEdit.lcEntry.entryValue;
    }
  }

  /**
   * Reset selected-state and delete geometryValue
   * @param value: geometryValue to delete
   */
  public deleteGeometryValue(value: LcEntryGeometryValue): void {

    if (LcEntryType.isClassification(value.lcEntry)) {
      return;
    }
    if (!!value.lcEntryValueParentId) {
      const parentValue = this.valueService.geometries.find(geo => geo.id === value.lcEntryValueParentId);
      if (!/*not*/!!parentValue) {
        // Just a precaution, should not be possible.
        return;
      }
      if (!LcEntryType.isGeometry(parentValue.lcEntry)) {
        // Something unexpected. That should not be possible.
        return;
      }
    }
    if (this.selectedEntryValueIds.includes(value.id)) {
      this.resetSelectionState();
    }
    this.valueService.deleteGeometryValue(value);

    const childGeometries = this.valueService.geometries.filter(geo => geo.lcEntryValueParentId === value.id);
    this.workspaceController.deleteGeometry(childGeometries.map(geo => geo.id));
  }

  /**
   * Reset selected-state and delete geometryValue
   *
   * Deletes *all* instances of an LcEntry.
   *
   * @param selector: geometryValue to delete
   */
  public deleteAllGeometriesByEntry(selector: LcEntryGeometryValue | LcEntry): void {

    const lcEntry = Object.keys(selector).includes('lcEntry')
      ? (selector as LcEntryGeometryValue).lcEntry
      : selector as LcEntry;

    const values2delete = this.valueService.geometries.filter(
      geo => geo.lcEntry.id === lcEntry.id
    );

    values2delete.forEach(value2delete => this.deleteGeometryValue(value2delete));
  }

  /**
   * Gets called by geometry-item on click. Resets current state, sets new state, calls value-service to create
   * the new value-tree and creates the geometry in the workspace.
   * @param rootGeoIdOrNull: Id of LcEntry from which to create geometry, null if called by global classifications
   * @param aisegCallback optional callback method to avoid circular dependencies with AisegService that depends on this service.
   */
  public onGeometryOrGlobalClicked(rootGeoIdOrNull: string | null, aisegCallback ?: () => void): void {
    if (rootGeoIdOrNull === null && this.rootEntryName === LabelModeUtilityService.IMAGE_CLASSIFICATIONS_IDENTIFIER) {
      this.resetSelectionState();
      return;
    }
    this.resetSelectionState();
    // If drawing: cancel drawing
    if (this.userIsDrawing === true) {
      this.workspaceController.cancelAndDeleteGeometry(this.latestCreatedGeometryValue.id);
    }
    if (rootGeoIdOrNull === null) {
      // Has no effect in video labeling mode.
      this.updateValueTree();
      return;
    }
    // get config-entry
    const geo2create: LcEntryGeometry = this.configService.findRecursiveById(rootGeoIdOrNull, true) as LcEntryGeometry;
    const lcEntryValueParentId = this.getLcEntryValueParentId(geo2create.lcEntryParentId);

    this.configService.hasConfigChanged().subscribe((hasChanged: boolean) => {

      // if true => show alert message else update without notification.
      if (hasChanged) {
        const translateKey = 'FEATURE.LABEL_MODE.PLEASE_RELOAD';
        this.labNotificationService.warn_i18(translateKey);
        return;
      }
      // call backend to create value-tree and create it in the workspace

      const creator = geo2create.type !== LcEntryType.IMAGE_SEGMENTATION_ERASER
        ? this.valueService.createValuesByGeometry(geo2create, lcEntryValueParentId)
        : new Observable<LcEntryGeometryValue>((observer) => {
          observer.next(new LcEntryImageSegmentationEraserValue(geo2create, this.valueService.mediaId, this.valueService.labelIterationId));
          observer.complete();
        });

      creator.subscribe((createdValue: LcEntryGeometryValue) => {

        if (this.userIsDrawing === true) {
          /*
           * In some error cases two requests to 'createValuesByRootGeometry' may run simultaneously.
           * In this case, the later responding request must cancel and delete the previous one.
           *
           * This can be triggered
           * - by finishing a geometry without nested classifications
           * - and quickly clicking the shortcut for another geometry.
           *
           * Finishing the geometry will start drawing the next one of the same type, resulting in
           * the first call of 'createValuesByRootGeometry'. Then, the shortcut cames in, resulting
           * in the second call of 'createValuesByRootGeometry'.
           */
          this.workspaceController.cancelAndDeleteGeometry(this.latestCreatedGeometryValue.id);
        }

        if (!!lcEntryValueParentId) {
          const parentGeometry = this.valueService.geometries
            .find(geo => geo.id === createdValue.lcEntryValueParentId);
          if (!!parentGeometry) {
            const otherSameTypedGeometries = this.valueService.geometries
              .filter(geo => geo.lcEntryId === parentGeometry.lcEntryId)
              .filter(geo => geo.id !== createdValue.lcEntryValueParentId);
            const ids2suppress = otherSameTypedGeometries.map(geo => geo.id);
            this.workspaceController.grayOutGeometry(ids2suppress);
          }
        }

        this.latestCreatedGeometryValue = createdValue;
        this.createGeometryInWorkspace(createdValue.id, createdValue.lcEntryValueParentId, geo2create);
        this.userIsDrawing = true;
        if (!!aisegCallback) {
          aisegCallback();
        }
      });
    });
  }

  /**
   * This method handles only global media classifications. They are not supported
   * in video labeling mode.
   *
   * @private
   */
  private updateValueTree() {
    if (this.rootEntryName !== LabelModeUtilityService.IMAGE_CLASSIFICATIONS_IDENTIFIER) {
      this.valueService.getMediaClassificationValues().pipe(
        take(1),
        takeUntil(this.unsubscribe)
      ).subscribe((mediaClassifications: LcEntryClassificationValue[]) => {
        this.selectedValueTree = mediaClassifications;
      });
      this.rootEntryName = LabelModeUtilityService.IMAGE_CLASSIFICATIONS_IDENTIFIER;
    }
  }

  /**
   * Creates a new Geometry in the workspace with the given valueId and LcEntry
   * @param valueId the LcEntryValueId for reference
   * @param parentValue to reference the parent value
   * @param lcEntry the LcEntry to base this geometry on
   */
  public createGeometryInWorkspace(valueId: string, parentValue: string, lcEntry: LcEntryGeometry): void {
    const geometryType = LabelModeUtilityService.translateTypes(lcEntry.type);
    const entryWithIcon = {...lcEntry, icon: LcEntryType.getIcon(lcEntry.type), lcEntryValueParentId: parentValue};
    const geometryProperties = new GeometryProperties(valueId, geometryType, entryWithIcon);
    this.workspaceController.drawGeometry(geometryProperties);
  }

  /**
   * Get the number of values created by the entry with the given id.
   * @param entryId: LcEntryId to check
   */
  public getValueCounterByEntryId(entryId: string): number {
    return this.valueService.geometries.filter(v => v.lcEntryId === entryId).length;
  }

  public isValueHidden(value: LcEntryValue | string): boolean {
    const id = typeof value === 'string' ? value : value.id;
    return this.hiddenValuesIds.includes(id);
  }

  public isValueActive(value: LcEntryValue | string): boolean {
    const id = typeof value === 'string' ? value : value.id;
    return this.selectedEntryValueIds.includes(id);
  }

  /**
   * Check the current LcEntryValueState of the given LcEntryValue and return it
   * @param value: LcEntryValue to check
   */
  public checkValueEntryState(value: LcEntryValue): LcEntryValueStates {
    if (this.isValueHidden(value)) {
      return LcEntryValueStates.HIDDEN;
    } else if (this.isValueActive(value.id)) {
      return LcEntryValueStates.ACTIVE;
    } else if (!value.valid) {
      return LcEntryValueStates.ERROR;
    } else {
      return LcEntryValueStates.DEFAULT;
    }
  }

  /**
   * Is any value based on the given LcEntryId hidden?
   *
   * @param lcEntryId
   */
  public hasAnyHiddenValueByEntryId(lcEntryId: string): boolean {
    return this.valueService.geometries
      .filter(value => value.lcEntry.id === lcEntryId)
      .filter(value => this.hiddenValuesIds.includes(value.id))
      .length > 0;
  }

  /**
   * Toggle the visibility state of one lc entry value by its id.
   *
   * If the value is a root geometry and has nested geometries the
   * visibility of that children is also toggled. To determine the
   * new visibility state the full list of geometries is used. If
   * *all of them* are visible, hide all. Otherwise show all of them.
   *
   * @param valueId
   */
  public toggleValueVisibility(valueId: string): void;
  public toggleValueVisibility(value: LcEntryValue): void;
  public toggleValueVisibility(value: string | LcEntryValue): void {

    const valueId = typeof value === 'string' ? value : value.id;

    /*
     * If the value is a IMAGE_SEGMENTATION hide *all* of them.
     */
    const geometryValue = this.valueService.geometries.find(geo => geo.id === valueId);
    if (!!geometryValue && geometryValue.lcEntry.type === LcEntryType.IMAGE_SEGMENTATION) {
      this.toggleHiddenValuesByEntryId(geometryValue.lcEntryId);
      return;
    }

    const childGeometryValuesIDs = this.valueService.geometries
      .filter(geo => geo.lcEntryValueParentId === valueId)
      .filter(geo => LcEntryType.isGeometry(geo.lcEntry))
      .map(geo => geo.id);

    const toggleIds = [valueId, ...childGeometryValuesIDs];

    const hasHiddenValue = this.hiddenValuesIds.find(id => toggleIds.includes(id)) !== undefined;
    this.toggleVisibilityById(toggleIds, hasHiddenValue);
  }

  /**
   * Looks through hiddenEntries array to determine whether to show or hide the geometries with the given id
   * @param lcEntryId: LcEntryId to hide or show
   */
  public toggleHiddenValuesByEntryId(lcEntryId: string): void;
  public toggleHiddenValuesByEntryId(lcEntry: LcEntryValue): void;
  public toggleHiddenValuesByEntryId(lcEntry: LcEntryValue | string): void {

    const lcEntryId = typeof lcEntry === 'string' ? lcEntry : lcEntry.id;
    const entry = this.configService.findRecursiveById(lcEntryId, true);
    const childGeometryIds = entry.children.filter(child => LcEntryType.isGeometry(child));
    const entries = [entry, ...childGeometryIds];
    const entryIds = entries.map(geo => geo.id);

    const toggleIds = this.valueService.geometries
      .filter(value => entryIds.includes(value.lcEntry.id))
      .map(value => value.id);

    const hasHiddenValue = this.hiddenValuesIds.find(id => toggleIds.includes(id)) !== undefined;
    this.toggleVisibilityById(toggleIds, hasHiddenValue);
  }

  public highlightGeometry(valueId: string|string[]) {
    this.workspaceController.highlightGeometry(valueId);
  }

  /**
   * Hide or show a bunch of geometries defined by their id and the show flag.
   *
   * @param ids2toggle a single id or a list of ids to toggle.
   * @param show boolean flag to set visibility
   * @private
   */
  private toggleVisibilityById(ids2toggle: string | string[], show: boolean): void {
    ids2toggle = Array.isArray(ids2toggle) ? ids2toggle : [ids2toggle];
    ids2toggle.forEach(id2toggle => {
      if (show) {
        this.hiddenValuesIds = this.hiddenValuesIds.filter(id => id !== id2toggle);
        this.workspaceController.showGeometry(id2toggle);
      } else {
        this.hiddenValuesIds.push(id2toggle);
        this.workspaceController.unselectGeometry(id2toggle);
        this.workspaceController.hideGeometry(id2toggle);
      }
    });
    // Inform that the hidden-values got changed
    this.onHiddenStateChanged.next('');
  }

  /**
   * If a nested geometry should be created, we need the id from the selected geometry value
   * for the api request.
   */
  private getLcEntryValueParentId(lcEntryParentId: string | null): string {

    if (!/*not*/!!lcEntryParentId) {
      return null;
    }

    // if none ore multiple geometries are select we cannot identify the parent geometry.
    if (this.workspaceController.selectedGeometries.length !== 1) {
      return null;
    }

    const selectedValueId = this.workspaceController.selectedGeometries[0].geometryProperties.identifier;

    const parentGeometryValue = this.valueService.geometries.find(geo => geo.id === selectedValueId);
    if (!!parentGeometryValue && !!parentGeometryValue.lcEntryValueParentId) {
      return parentGeometryValue.lcEntryValueParentId;
    }

    return selectedValueId;
  }
}
