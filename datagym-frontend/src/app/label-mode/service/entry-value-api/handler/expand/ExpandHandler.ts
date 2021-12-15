import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {Subject} from 'rxjs';


export abstract class ExpandHandler {

  public abstract expandVideoValueLine(value: LcEntryGeometryValue, change: LcEntryChange, left: boolean): void;

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

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
