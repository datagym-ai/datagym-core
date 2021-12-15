import {LcEntryGeometryValue} from '../../../../model/geometry/LcEntryGeometryValue';
import {LcEntryChange} from '../../../../model/change/LcEntryChange';
import {LcEntryType} from '../../../../../label-config/model/LcEntryType';
import {Subject} from 'rxjs';

/**
 * Delete handler to manage the change objects for a geometry.
 *
 * This delete handler is called when a keyframe is deleted via the videoValueLines.
 */
export abstract class DeleteHandler {
  /**
   * IMAGE_SEGMENTATION_ERASER is not supported within the BE.
   * Override this property in child classes to set different types.
   */
  public readonly unsupportedKinds: LcEntryType[] = [LcEntryType.IMAGE_SEGMENTATION_ERASER];

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  /**
   * Delete all change objects from the value with the frame number.
   *
   * @param value
   * @param frameNumber
   */
  public abstract deleteChange(value: LcEntryGeometryValue, frameNumber: number): void;

  /**
   * Delete the given change object from the given value.
   *
   * @param value
   * @param change
   */
  public abstract deleteChange(value: LcEntryGeometryValue, change: LcEntryChange): void;

  /**
   * Implementation of the above definitions.
   *
   * @param value
   * @param arg
   */
  public abstract deleteChange(value: LcEntryGeometryValue, arg: number|LcEntryChange): void;

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
