import {Subject} from 'rxjs';
import {AbstractControl} from '@angular/forms';
import {filter, take, takeUntil} from 'rxjs/operators';
import {LabelConfigForm} from '../model/LabelConfigForm';

/**
 * This class is used to 'connect' two Form Controls that the
 * value of the 'leader' is mirrored into the 'follower' until
 * the follower's value gets changed with another input.
 *
 * In addition, a optional transform method can be passed to the ctr.
 * This transformer is called with the leader's value and it's return
 * value is set as new follower's value. The default is a
 * 'none-transformer'
 */
export class ConnectControls {

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    leader: AbstractControl,
    follower: AbstractControl,
    transform?: (value: string) => string
  ) {
    /**
     * If no transform method was set, don't filter.
     */
    if (transform === undefined) {
      transform = (value: string) => value;
    }

    leader.valueChanges
      .pipe(
        filter(() => leader.valid),
        filter(value => !!value),
        takeUntil(this.unsubscribe)
      ).subscribe((newValue: string) => {

      const newFollowerValue = transform(newValue);

      // set the value on the follower but don't emit the changed event.
      follower.setValue(newFollowerValue, {emitEvent: false});
      follower.markAsDirty();
      follower.markAsTouched();
    });

    /**
     * If the value was changed directly,
     * break the connection.
     */
    follower.valueChanges
      .pipe(take(1), takeUntil(this.unsubscribe))
      .subscribe(() => {

        this.unsubscribe.next();
        this.unsubscribe.complete();
      });
  }

  /**
   * This set's the connector with the default behaviour of the label config.
   * The callback is defined as ConnectControls.defaultTransformer.
   *
   * @param leader
   * @param follower
   * @constructor
   */
  public static LABEL_CONFIG(leader: AbstractControl, follower: AbstractControl): ConnectControls {
    return new ConnectControls(leader, follower, ConnectControls.defaultTransformer);
  }

  /**
   * Transform the exportName value into the exportKey value. Do these jobs:
   * - to lowercase
   * - space to underscore
   * - cut by exportKey.maxLength chars
   *
   * @param value
   */
  private static defaultTransformer(value: string): string {
    const exportKey = LabelConfigForm.exportKey;

    return value
      .toLowerCase()
      .split('')
      // Replace all spaces with underscores
      .map(c => c === ' ' ? '_' : c)
      .filter(s => s.match(exportKey.pattern))
      .join('')
      // Replace also all multiple underscores with just one.
      .replace(/_+/g, '_')
      .substring(0, exportKey.maxLength);
  }

}
