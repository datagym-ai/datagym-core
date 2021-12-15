import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RequestPendingObserverService {
  public requestsPending$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private counter: number = 0;

  public inc() {
    this.counter++;
    this.requestsPending$.next(true);
  }

  public dec() {
    this.counter--;
    if (this.counter === 0) {
      this.requestsPending$.next(false);
    }
  }
}
