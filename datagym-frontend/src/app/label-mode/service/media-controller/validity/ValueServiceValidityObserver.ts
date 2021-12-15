import {ValidityObserver} from './ValidityObserver';
import {EntryValueService} from '../../entry-value.service';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';


export class ValueServiceValidityObserver implements ValidityObserver {

  public globalClassificationsValid: boolean = false;

  // Acts as a reset without destroying the original subject
  protected readonly unsubscribe: Subject<void> = new Subject<void>();

  public constructor(private valueService: EntryValueService) {
    this.valueService.mediaClassificationsValid$.pipe(takeUntil(this.unsubscribe)).subscribe((valid: boolean) => {
      this.globalClassificationsValid = valid;
    });
  }

  hasGeometries(): boolean {
    return this.valueService.geometries.length > 0;
  }

  hasInvalidGeometries(): boolean {
    return this.valueService.geometries.find(value => !/*not*/!!value.valid) !== undefined;
  }

  public teardown(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

}
