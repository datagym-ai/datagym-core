import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LcEntryChange} from '../../model/change/LcEntryChange';
import {EntryChangeApiInterface} from './entry-change-api-interface';
import {LcEntryChangeType} from '../../model/change/LcEntryChangeType';
import {map, tap} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class EntryChangeApiService implements EntryChangeApiInterface {

  private baseURL: string = '/api/lcvalues/change';

  constructor(private http: HttpClient) { }

  public delete(change: string|LcEntryChange): Observable<void> {
    const id = typeof change === 'string' ? change : change.id;

    const url = `${ this.baseURL }/${ id }`;
    return this.http.delete<void>(url);
  }

  public update(change: LcEntryChange): Observable<LcEntryChange> {
    const url = `${ this.baseURL }/${ change.id }`;

    const body = {
      ...change,
      kind: change.kind
    };
    delete body.handCrafted;

    return this.http.put<LcEntryChange>(url, body).pipe(
      map(response => {
        return {
          ...response,
          handCrafted: change.handCrafted
        } as LcEntryChange;
      })
    );
  }

  public create(grandParentId: string, valueId: string, change: LcEntryChange): Observable<LcEntryChange> {

    const body = {
      ...change,
      kind: change.kind,
      lcEntryValueId: valueId,
      lcEntryRootParentValueId: grandParentId
    };

    delete body.id;
    delete body.handCrafted;

    return this.http.post<LcEntryChange>(this.baseURL, body).pipe(tap(response => {
      /**
       * Todo: remove that 'cast' when the BE only returns `frameType`.
       * Workaround while the backend returns the deprecated type.
       */
      const deprecatedType = {type: '', ...response}.type;
      if (!/*not*/!!response.frameType && LcEntryChangeType.inEnum(deprecatedType)) {
        response.frameType = deprecatedType as LcEntryChangeType;
      }
    }));
  }
}
