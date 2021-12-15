import {Injectable} from '@angular/core';
import {WorkspaceInternalService} from './workspace-internal.service';
import {Observable, Subject} from 'rxjs';
import {WorkspaceEvent} from '../messaging/WorkspaceEvent';
import {BaseGeometryData} from '../geometries/geometry-data/BaseGeometryData';
import {filter} from 'rxjs/operators';
import {BaseGeometry} from '../geometries/BaseGeometry';
import {WorkspaceEventFilter} from './WorkspaceEventFilter';
import {Point} from '../../label-mode/model/geometry/Point';
import {GeometryProperties} from '../geometries/GeometryProperties';
import {LcEntryGeometry} from '../../label-config/model/geometry/LcEntryGeometry';
import {GeometryConfiguration} from '../model/GeometryConfiguration';
import {LcEntryType} from '../../label-config/model/LcEntryType';
import {GeometryType} from '../geometries/GeometryType';
import {LabelModeType} from '../../label-mode/model/import';

/**
 * This is a wrapper for the workspace
 */
@Injectable({
  providedIn: 'root'
})
export class WorkspaceControlService {

  private workspaceEventBus = new Subject<WorkspaceEvent>();

  public get configuration(): GeometryConfiguration[] {
    return this.workspace.configuration;
  }

  public get numberOfSelectedGeometries(): number {
    return this.workspace.selectedGeometries.length;
  }

  public get selectedGeometries(): BaseGeometry[] {
    return this.workspace.selectedGeometries;
  }

  public get resizeOffsetHeight(): number {
    return this.workspace.resizeOffsetHeight;
  }

  public get resizeOffsetWidth(): number {
    return this.workspace.resizeOffsetWidth;
  }

  public get currentMediaWidth(): number {
    return this.workspace.currentMediaWidth;
  }

  public get currentMediaHeight(): number {
    return this.workspace.currentMediaHeight;
  }

  public get selectedGeometryIds(): string[] {
    return this.workspace.selectedGeometries.map(geo => geo.geometryProperties.identifier);
  }

  public get aiSegActive(): boolean {
    return this.workspace.aiSegActive;
  }

  /**
   * Open means the nested geometries are toggled within the value list of the label mode.
   */
  public get openRootGeometryIds(): string[] {
    const selectedGeometries = this.selectedGeometries;
    const selectedRootIds = selectedGeometries
      .map(geo => geo.geometryProperties)
      .map(geo => !!geo.lcEntryValueParentId ? geo.lcEntryValueParentId : geo.identifier);

    return [...new Set(selectedRootIds)];
  }

  constructor(private workspace: WorkspaceInternalService) {
    // Redirect event from workspace
    this.workspace.internalWorkspaceEventBus.subscribe(event => {
      this.workspaceEventBus.next(event);
    });
  }

  public onDeleteRequest(): void {
    this.workspace.onDeleteRequest();
  }

  public sendWorkSpaceEvent(event: WorkspaceEvent){
    this.workspaceEventBus.next(event);
  }

  public eventFilter(eventFilter: WorkspaceEventFilter): Observable<WorkspaceEvent> {
    return this.workspaceEventBus.pipe(
      filter((e: WorkspaceEvent) => eventFilter.match(e.eventType))
    );
  }

  public grayOutGeometry(identifier: string|string[]): void {
    this.workspace.grayOutGeometry(identifier);
  }

  public hideGeometry(identifier: string|string[]) {
    this.workspace.hideGeometryByIdentifier(identifier);
  }

  public showGeometry(identifier: string|string[]) {
    this.workspace.showGeometryByIdentifier(identifier);
  }

  public deleteGeometry(identifier: string|string[]) {
    this.workspace.deleteGeometryByIdentifier(identifier);
  }

  /**
   * Select only a specific geometry (unselect all others)
   * @param identifier
   */
  public selectSingleGeometry(identifier: string) {
    if (this.workspace.selectedGeometries.length === 0) {
      // If none are selected, select by identifier
      this.workspace.selectGeometryByIdentifier(identifier);
    } else if (this.workspace.selectedGeometries.length > 1) {
      // If multiple are selected, unselect without event (can't edit multiple values only delete)
      this.workspace.unselectAllGeometries(false);
      this.workspace.selectGeometryByIdentifier(identifier);
    } else if (this.workspace.selectedGeometries.length === 1) {
      // If given identifier is not already selected, unselect current and select given
      const alreadySelected: boolean = this.workspace.selectedGeometries
        .filter(geometry => geometry.geometryProperties.identifier === identifier).length > 0;
      if (!alreadySelected) {
        this.workspace.unselectAllGeometries();
        this.workspace.selectGeometryByIdentifier(identifier);
      }
    }
  }

  /**
   * delete all geometries in workspace
   */
  public deleteAllGeometries(): void {
    this.workspace.deleteAllGeometries();
  }

  /**
   * Unselect all geometries in workspace
   */
  public unselectAllGeometries(): void {
    this.workspace.unselectAllGeometries();
  }

  /**
   * Unselect the specific geometry/geometries
   * @param identifier
   */
  public unselectGeometry(identifier: string|string[]) {
    this.workspace.unselectGeometryByIdentifier(identifier);
  }

  /**
   * Unselect the specific geometry/geometries
   * @param identifier
   */
  public selectGeometryByIdentifier(identifier: string|string[]): void{
    this.workspace.selectGeometryByIdentifier(identifier);
  }

  /**
   * 1) cancel the current drawing of the geometry
   * 2) Deletes the geometry (with delete-event!)
   * @param identifier
   */
  public cancelAndDeleteGeometry(identifier: string) {
    const geoToCancel: BaseGeometry = this.workspace.currentGeometries.find((geo: BaseGeometry) => geo.geometryProperties.identifier === identifier);
    if (!!geoToCancel) {
      this.workspace.cancelDrawingGeometry(geoToCancel, true);
    }
  }

  public cancelAISeg(): void {
    this.workspace.cancelAISeg();
  }

  public getGeometryData(identifier: string): BaseGeometryData {
    return this.workspace.getGeometryDataByIdentifier(identifier);
  }

  public initConfiguration(configuration: LcEntryGeometry[]): void {
    this.workspace.configuration = configuration.map(
      config => new GeometryConfiguration(
        config.id,
        config.lcEntryParentId,
        config.children.map(child => child.id),
        config.color,
        config.entryValue,
        LcEntryType.getIcon(config.type),
        GeometryType.valueOf(config.type)
    ));
  }

  public createGeometry(geometryProperties: GeometryProperties, data: BaseGeometryData): void {
    this.workspace.drawGeometry(geometryProperties, true);
    this.workspace.setGeometryDataByIdentifier(geometryProperties.identifier, data);
  }

  public drawGeometry(geometryProperties: GeometryProperties) {
    this.workspace.drawGeometry(geometryProperties).startDrawing();
  }

  /**
   * Load this media in workspace.
   *
   * @param src
   * @param identifier
   * @param type
   */
  public loadMedia(src: string, identifier: string, type: LabelModeType): void {
    this.workspace.loadMedia(src, identifier, type);
  }

  /**
   * Get the scaled workspace size.
   */
  public getWorkspaceSize(): Point {
    return new Point(
      this.currentMediaWidth * this.resizeOffsetWidth,
      this.currentMediaHeight * this.resizeOffsetHeight
    );
  }

  public highlightGeometry(valueId: string|string[]) {
    const ids = Array.isArray(valueId) ? valueId : [valueId];
    this.workspace.currentGeometries
      .filter(geo => ids.includes(geo.geometryProperties.identifier))
      .forEach(geo => geo.highlight());
  }
}
