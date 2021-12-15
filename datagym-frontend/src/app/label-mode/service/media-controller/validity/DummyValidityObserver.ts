import {ValidityObserver} from './ValidityObserver';


export class DummyValidityObserver implements ValidityObserver {

  public readonly globalClassificationsValid: boolean = false;

  public hasGeometries(): boolean {
    // Have some break. Here is nothing to do.
    return false;
  }

  public hasInvalidGeometries(): boolean {
    // Have some break. Here is nothing to do.
    return false;
  }

  public teardown(): void {
    // Have some break. Here is nothing to do.
  }
}
