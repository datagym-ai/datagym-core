import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {EntryConfigApiInterface} from './entry-config-api-interface';
import {Router} from '@angular/router';
import {PreviewModeUri} from '../../model/PreviewModeUri';
import {EntryConfigApiPreview} from './entry-config-api-preview';
import {AdminModeUri} from '../../model/AdminModeUri';


/**
 * Factory to decide, which implementation of 'EntryValueApiInterface' should be used.
 *
 * This method stays here to not raise some circular dependencies.
 *
 * @param router
 * @param http
 */
export function entryConfigApiFactory(router: Router, http: HttpClient): EntryConfigApiInterface {

  const url = router.routerState.snapshot.url;
  if (PreviewModeUri.equals(url) || AdminModeUri.isAdminUrl(url)) {
    return new EntryConfigApiPreview();
  }
  return new EntryConfigApiService(http);
}

/**
 * The default EntryValueApi implementation using
 * the HttpClient to communicate with the backend.
 */
@Injectable({
  providedIn: PreviewModeUri.PROVIDED_IN,
  useFactory: entryConfigApiFactory,
  deps: [Router, HttpClient],
})
export class EntryConfigApiService implements EntryConfigApiInterface {

  constructor(private http: HttpClient) { }

  /**
   * Detect if the label configuration was changed from the project admin
   * while the labeler made a great job and detected a new geometry or
   * classified the whole media.
   */
  public hasConfigChanged(labelIterationId: string, lastChangedConfig: number) : Observable<boolean> {

    const url = '/api/lconfig/hasConfigChanged';

    let urlParams: HttpParams = new HttpParams();
    urlParams = urlParams.append('lastChangedConfig', `${ lastChangedConfig }`);
    urlParams = urlParams.append('iterationId', labelIterationId);

    return this.http.get<{hasLabelConfigChanged: boolean}>(url, {params: urlParams}).pipe(
      map( (response) => !!response.hasLabelConfigChanged )
    );
  }
}
