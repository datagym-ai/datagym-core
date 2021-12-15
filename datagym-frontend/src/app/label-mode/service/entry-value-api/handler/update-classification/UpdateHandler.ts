import {Subject} from 'rxjs';
import {LcEntryClassificationValue} from '../../../../model/classification/LcEntryClassificationValue';

/**
 * Update handler to update classifications in the BE.
 */
export abstract class UpdateHandler {

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  protected constructor(
    protected readonly labelTaskId: string,
  ) {}

  public abstract updateClassification(value: LcEntryClassificationValue): void;

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
