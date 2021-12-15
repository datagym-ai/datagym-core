import {Subject} from 'rxjs';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {EntryValueApiService} from '../../entry-value-api.service';

/**
 * Update handler to update geometries in the BE.
 */
export abstract class UpdateHandler {

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  protected constructor(
    protected labelTaskId: string,
    protected entryValueApiService: EntryValueApiService
  ) {}

  public abstract updateGeometry(value: LcEntryGeometryValue): void;

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
