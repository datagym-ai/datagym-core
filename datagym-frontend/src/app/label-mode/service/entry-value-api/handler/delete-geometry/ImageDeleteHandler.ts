import {EntryValueService} from '../../../entry-value.service';
import {EntryValueApiService} from '../../entry-value-api.service';
import {WorkspaceControlService} from '../../../../../svg-workspace/service/workspace-control.service';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {DeleteHandler} from './DeleteHandler';
import {take, takeUntil} from 'rxjs/operators';
import {DeleteGeometryConfig} from './DeleteGeometryConfig';
import {EventEmitter} from '@angular/core';


export class ImageDeleteHandler extends DeleteHandler {

  public constructor(
    valueService: EntryValueService,
    entryValueApiService: EntryValueApiService,
    workspaceController: WorkspaceControlService,
    private readonly onDeleteEvent: EventEmitter<string>
  ) {
    super(valueService, entryValueApiService, workspaceController);
  }

  /**
   * This is the default delete mechanism from the media labeling.
   *
   * @param value
   * @param _ ignored just to match the interface.
   */
  public deleteGeometry(value: LcEntryGeometryValue, _: DeleteGeometryConfig): void {
    value.markAsDeleted();
    this.entryValueApiService.deleteValueById(value.id).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(() => {
      // remove the value from the stack.
      this.valueService.removeGeometryFromStack(value);
      this.workspaceController.deleteGeometry(value.id);
      this.onDeleteEvent.emit(value.id);
    });
  }
}
