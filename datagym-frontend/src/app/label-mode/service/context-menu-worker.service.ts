import {Injectable} from '@angular/core';
import {ContextMenuService} from '../../svg-workspace/service/context-menu.service';
import {WorkspaceControlService} from '../../svg-workspace/service/workspace-control.service';
import {Subject} from 'rxjs';
import {ContextMenuEventType} from '../../svg-workspace/messaging/ContextMenuEventType';
import {LabelModeUtilityService} from './label-mode-utility.service';
import {ContextMenuEvent} from '../../svg-workspace/messaging/ContextMenuEvent';
import {EntryValueService} from './entry-value.service';
import {LabelConfigService} from '../../label-config/service/label-config.service';
import {EntryConfigService} from './entry-config.service';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {takeUntil} from 'rxjs/operators';
import {AisegService} from './aiseg.service';
import {AiSegType} from '../model/AiSegType';
import {BaseGeometry} from '../../svg-workspace/geometries/BaseGeometry';
import {LcEntryGeometryValue} from '../model/geometry/LcEntryGeometryValue';


@Injectable({
  providedIn: 'root'
})
export class ContextMenuWorkerService {

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private aiseg: AisegService,
    private contextMenu: ContextMenuService,
    private valueService: EntryValueService,
    private configService: EntryConfigService,
    private workspace: WorkspaceControlService,
    private labelConfigService: LabelConfigService,
    private labelModeUtility: LabelModeUtilityService,
    private labNotificationService: LabNotificationService
  ) {
  }

  public init(): void {
    this.reset();
    this.unsubscribe = new Subject<void>();
    this.initTransform();
    this.initRefinePrediction();
    this.initHideGeometry();
    this.initDuplicateGeometry();
  }

  public reset(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  close() {
    this.contextMenu.close();
  }

  private initTransform(): void {
    this.contextMenu.onAction(ContextMenuEventType.TRANSFORM).pipe(takeUntil(this.unsubscribe)).subscribe((action: ContextMenuEvent) => {
      const targetGeometryId = action.payload as string;
      const existingGeometries = this.valueService.geometries
        .filter(geo => action.ids.includes(geo.id))
        .filter((geometry: LcEntryGeometryValue) => geometry.lcEntry.id !== targetGeometryId);

      this.configService.hasConfigChanged().subscribe((hasChanged: boolean) => {
        if (hasChanged) {
          const translateKey = 'FEATURE.LABEL_MODE.PLEASE_RELOAD';
          this.labNotificationService.warn_i18(translateKey);
          return;
        }

        /*
         * Transform each geometry (by action.ids) into a
         * geometry of type defined by targetRootGeometryId
         */
        existingGeometries.forEach((existingGeometry: LcEntryGeometryValue) => {
          this.valueService.changeGeometryType(existingGeometry.id, targetGeometryId)
            .subscribe((value: LcEntryGeometryValue) => {

              /*
               * Delete the drawn geometry within the workspace and within the valueService.
               */
              this.workspace.deleteGeometry(existingGeometry.id);
              this.valueService.geometries
                .filter(geo => geo.lcEntryValueParentId === existingGeometry.id)
                .forEach(geo => this.workspace.deleteGeometry(geo.id));

              this.valueService.createGeometryInWorkSpace(value);
              this.valueService.removeGeometryFromStack(existingGeometry);

              value.updateValidity();
              this.valueService.addGeometryToStack(value);
              /*
               * When changing multiple geometries the bar
               * and should be closed, single request per entry
               */
              if (existingGeometries.length === 1) {
                this.workspace.selectSingleGeometry(value.id);
              }
            });
        });
      });
    });
  }

  private initRefinePrediction(): void {
    this.contextMenu.onAction(ContextMenuEventType.REFINE_PREDICTION).pipe(takeUntil(this.unsubscribe)).subscribe((action: ContextMenuEvent) => {
      const isRefinePrediction: boolean = true;
      this.aiseg.initAiseg(AiSegType.POINTS, isRefinePrediction);
      (action.payload as BaseGeometry).show();
    });
  }

  private initHideGeometry(): void {
    this.contextMenu.onAction(ContextMenuEventType.HIDE).pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((action: ContextMenuEvent) => {

      /*
       * If a child geometry and his parent were selected the child geometry got toggled twice.
       * The first time because all the children of the parent should be toggled and the second
       * time because it is selected. That results in undefined behaviour. (Depending on the
       * order of the ids.)
       *
       * To avoid that, filter all child geometries that are listed beside their parent geometry.
       */
      const ids = this.valueService.geometries
        // Only the listed geometries.
        .filter(geo => action.ids.includes(geo.id))
        // Only root geometries or child geometries without their parent.
        .filter(geo => !/*not*/!!geo.lcEntryValueParentId || !action.ids.includes(geo.lcEntryValueParentId))
        .map(geo => geo.id);

      ids.forEach(id => this.labelModeUtility.toggleValueVisibility(id));
    });
  }

  private initDuplicateGeometry(): void {
    this.contextMenu.onAction(ContextMenuEventType.DUPLICATE_GEOMETRY).pipe(takeUntil(this.unsubscribe)).subscribe((action: ContextMenuEvent) => {
        const ids = action.ids;
        const mousePosition = action.payload;
        if (ids.length !== 1) {
          return;
        }
        this.configService.hasConfigChanged().subscribe((hasChanged: boolean) => {
          const selectedValue = this.valueService.geometries.find(geo => ids.includes(geo.id));
          if (!/*not*/!!selectedValue) {
            // should not be possible.
            return;
          }
          if (hasChanged) {
            const translateKey = 'FEATURE.LABEL_MODE.PLEASE_RELOAD';
            this.labNotificationService.warn_i18(translateKey);
            return;
          }
          this.valueService.createValuesByGeometry(selectedValue.lcEntryId).subscribe((createdValue: LcEntryGeometryValue) => {
            this.valueService.createClonedGeometryInWorkSpace(createdValue, selectedValue, mousePosition);
            this.workspace.unselectAllGeometries();
            this.workspace.selectGeometryByIdentifier(createdValue.id);
          });
        });
      }
    );
  }
}
