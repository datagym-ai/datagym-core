import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LcEntryValueCreateBindingModel} from '../../model/LcEntryValueCreateBindingModel';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {LcEntryValueFactory} from '../../model/value-response-factory/LcEntryValueFactory';
import {EntryConfigService} from '../entry-config.service';
import {LcEntryValueUpdateBindingModel} from '../../model/LcEntryValueUpdateBindingModel';
import {EntryValueApiInterface} from './entry-value-api-interface';
import {Router} from '@angular/router';
import {PreviewModeUri} from '../../model/PreviewModeUri';
import {EntryValueApiPreview} from './entry-value-api-preview';
import {EntryValueService} from '../entry-value.service';
import {AdminModeUri} from '../../model/AdminModeUri';
import {LcEntryValue} from '../../model/LcEntryValue';
import {LcEntryGeometryValue} from '../../model/geometry/LcEntryGeometryValue';
import {LcEntryClassificationValue} from '../../model/classification/LcEntryClassificationValue';


/**
 * Factory to decide, which implementation of 'EntryValueApiInterface' should be used.
 *
 * This method stays here to not raise some circular dependencies.
 *
 * @param router
 * @param http
 * @param entryConfigService
 */
export function entryValueApiInterfaceFactory(
  router: Router,
  http: HttpClient,
  entryConfigService: EntryConfigService
): EntryValueApiInterface {

  const url = router.routerState.snapshot.url;
  if (PreviewModeUri.equals(url) || AdminModeUri.isAdminUrl(url)) {
    return new EntryValueApiPreview(entryConfigService);
  }
  return new EntryValueApiService(http, entryConfigService);
}


/**
 * The default EntryValueApi implementation using the HttpClient &
 * EntryConfigService to communicate with the backend.
 */
@Injectable({
  providedIn: PreviewModeUri.PROVIDED_IN,
  useFactory: entryValueApiInterfaceFactory,
  deps: [Router, HttpClient, EntryConfigService],
})
export class EntryValueApiService implements EntryValueApiInterface {

  public valueService: EntryValueService;

  private baseURL: string = '/api/lcvalues';

  constructor(private http: HttpClient, private entryConfigService: EntryConfigService) {
  }

  public getMediaClassificationValues(labelConfigurationId: string, lcEntryValueCreateBindingModel: LcEntryValueCreateBindingModel): Observable<LcEntryClassificationValue[]> {
    const url = `${ this.baseURL }/${ labelConfigurationId }/classification`;
    const globalClassifications = this.entryConfigService.globalClassifications;
    return this.http.post<LcEntryClassificationValue[]>(url, lcEntryValueCreateBindingModel).pipe(map(response => {
      return response.map(val => LcEntryValueFactory.castFromObject(val,
        globalClassifications.find(lcEntry => lcEntry.id === val.lcEntryId)) as LcEntryClassificationValue);
    }));
  }

  public createValuesByGeometry(lcEntryId: string, body: LcEntryValueCreateBindingModel): Observable<LcEntryGeometryValue> {
    const url = `${ this.baseURL }/${ lcEntryId }`;
    const lcEntry = this.entryConfigService.findRecursiveById(lcEntryId, true);
    return this.http.post<LcEntryGeometryValue[]>(url, body).pipe(map(response => {
      return LcEntryValueFactory.castFromObject(response, lcEntry) as LcEntryGeometryValue;
    }));
  }

  public deleteValueById(id: string): Observable<void> {
    const url = `${ this.baseURL }/${ id }`;
    return this.http.delete<void>(url);
  }

  public updateSingleValue(id: string, data: LcEntryValueUpdateBindingModel): Observable<LcEntryValue> {
    const url = `${ this.baseURL }/${ id }`;
    const lcEntry = this.entryConfigService.findRecursiveById(data.lcEntryId);

    return this.http.put<LcEntryClassificationValue>(url, data).pipe(
      map((response: LcEntryClassificationValue) => {
        return LcEntryValueFactory.castFromObject(response, lcEntry);
      })
    );
  }

  /**
   * Change the geometry type of an existing geometry.
   *
   * Note: this will delete all classifications without warning!
   *
   * @param id
   * @param newLcEntryId
   */
  public changeGeometryType(id: string, newLcEntryId: string): Observable<LcEntryGeometryValue> {
    const url = `${ this.baseURL }/${ id }/changeType`;
    const payload = {newLcEntryId};
    const lcEntry = this.entryConfigService.findRecursiveById(newLcEntryId, true);
    return this.http.put<LcEntryGeometryValue>(url, payload).pipe(map(
      (newGeometry: LcEntryGeometryValue) => {
        return LcEntryValueFactory.castFromObject(newGeometry, lcEntry) as LcEntryGeometryValue;
      }
    ));
  }
}
