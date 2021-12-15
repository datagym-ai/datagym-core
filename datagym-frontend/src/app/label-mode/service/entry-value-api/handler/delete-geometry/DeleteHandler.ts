import {EntryValueService} from '../../../entry-value.service';
import {EntryValueApiService} from '../../entry-value-api.service';
import {WorkspaceControlService} from '../../../../../svg-workspace/service/workspace-control.service';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {Subject} from 'rxjs';
import {LcEntryType} from '../../../../../label-config/model/LcEntryType';
import {DeleteGeometryConfig} from './DeleteGeometryConfig';

/**
 * Delete handler to delete the full value or
 * just to manage the change object updates.
 *
 * This delete handler is called when a geometry is deleted within the workspace.
 */
export abstract class DeleteHandler {
  /**
   * IMAGE_SEGMENTATION_ERASER is not supported within the BE.
   * Override this property in child classes to set different types.
   */
  public readonly unsupportedKinds: LcEntryType[] = [LcEntryType.IMAGE_SEGMENTATION_ERASER];

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  protected constructor(
    protected valueService: EntryValueService,
    protected entryValueApiService: EntryValueApiService,
    protected workspaceController: WorkspaceControlService,
  ) {
  }

  /**
   * Delete a geometryValue and its geometry in the workspace.
   * Doesn't delete in workspace if called by workspace CANCELED eventListener.
   *
   * @param value: value to delete
   * @param config
   */
  public abstract deleteGeometry(value: LcEntryGeometryValue, config: DeleteGeometryConfig): void;

  /**
   * Do some cleanup if necessary.
   *
   * Note: when overriding this method, make sure
   * to complete the unsubscribe subject!
   */
  public tearDown(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
