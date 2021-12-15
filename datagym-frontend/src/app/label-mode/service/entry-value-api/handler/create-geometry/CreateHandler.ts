import {Subject} from 'rxjs';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';

/**
 * Create handler to initial set the geometry properties in the BE.
 */
export abstract class CreateHandler {

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  protected constructor(
    protected readonly labelTaskId: string,
  ) {}

  /**
   * `Create Geometry` within the datagym context means 'Update the geometry properties'.
   *
   * @param value
   */
  public abstract createGeometry(value: LcEntryGeometryValue): void;

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
