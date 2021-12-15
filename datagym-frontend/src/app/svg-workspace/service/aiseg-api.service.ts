import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {AiSegCalculate} from '../model/AiSegCalculate';
import {AiSegResponse} from '../model/AiSegResponse';
import {finalize, tap} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AisegApiService {

  get isCalculating(): boolean {
    return this.aisegCalculating;
  }

  get isPrepared(): boolean {
    return this.aiSegPrepared;
  }

  public get aiSegActive(): boolean {
    return this.isAiSegActive;
  }

  public set aiSegActive(newState: boolean) {
    this.isAiSegActive = newState;
    this.onActivationEvent.emit(newState);
  }

  /**
   * Required to 'listen' to the escape event that canceled the aiseg drawing.
   */
  public onActivationEvent: EventEmitter<boolean> = new EventEmitter<boolean>();

  /**
   * This flag is set from 'outside' before the aiseg rectangle is setup and
   * reset after replacing the rectangle with the polygon geometry.
   */
  private isAiSegActive: boolean = false;

  /**
   * The internal state of aiseg api.
   */
  private aisegImageId: string = '';
  private aiSegPrepared: boolean = false;
  private aisegCalculating: boolean = false;

  constructor(private http: HttpClient) { }

  public prepareMedia(imageId: string, frameNumber: number = undefined, dataUri: string = null): Observable<void> {
    this.aiSegPrepared = false;
    this.aisegImageId = imageId;
    let url = `/api/aiseg/prepare/${imageId}`;
    if (frameNumber !== undefined) {
      url = `/api/aiseg/prepare/${imageId}/${frameNumber}`;
    }
    return this.http.post<void>(url, dataUri).pipe(
      tap(() => {
        this.aiSegPrepared = true;
      })
    );
  }

  public calculate(calcObj: AiSegCalculate): Observable<AiSegResponse> {
    this.aisegCalculating = true;
    // make sure we only use the image id we had prepared.
    calcObj.imageId = this.aisegImageId;
    return this.http.post<AiSegResponse>('/api/aiseg/calculate', calcObj).pipe(
      finalize(() => {this.aisegCalculating = false;})
    );
  }

  public finishImage(): Observable<void> {
    const finish = this.aiSegPrepared || this.aiSegActive;
    const imageId = this.aisegImageId;
    this.aisegCalculating = false;
    this.aiSegPrepared = false;
    this.aisegImageId = '';

    if (!finish) {
      return of(null);
    }

    const url = `/api/aiseg/finish/${imageId}`;
    return this.http.delete<void>(url);
  }

  public finishUserSession(userSessionUUID: string): Observable<void> {
    const url = `/api/aiseg/finishUserSession/${userSessionUUID}`;
    return this.http.delete<void>(url);
  }

  public finishFrameImage(mediaId: string, frameNumber: number): Observable<void> {
    const url = `/api/aiseg/finishFrame/${mediaId}/${frameNumber}`;
    return this.http.delete<void>(url);
  }
}
