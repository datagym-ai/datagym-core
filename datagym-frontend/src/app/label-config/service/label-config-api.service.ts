import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import { LabelConfiguration } from '../model/LabelConfiguration';
import { LcEntry } from '../model/LcEntry';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class LabelConfigApiService {
  private baseUrl = '/api/lconfig';

  constructor(private http: HttpClient) { }

  public getLabelConfigById(id: string): Observable<LabelConfiguration> {
    return this.http.get<LabelConfiguration>(`${ this.baseUrl }/${ id }`);
  }

  public getExportUrl(configId: string): string {
    return `${ this.baseUrl }/${ configId }/export`;
  }

  public updateLabelConfig(id: string, data: LcEntry[], changeCompletedTasksStatus: boolean = false): Observable<LabelConfiguration> {

    let urlParams: HttpParams = new HttpParams();
    if (changeCompletedTasksStatus !== undefined) {
      urlParams = urlParams.append('changeStatus', `${ changeCompletedTasksStatus }`);
    }

    const url = `${ this.baseUrl }/${ id }`;
    return this.http.put<LabelConfiguration>(url, data, {params: urlParams});
  }

  public deleteLabelConfig(id: string): Observable<LabelConfiguration> {
    const url = `${ this.baseUrl }/${ id }`;
    return this.http.delete<LabelConfiguration>(url)
      .pipe(map(data => new LabelConfiguration(data['configId'])));
  }

  public getForbiddenKeywords(): Observable<string[]> {
    const url = `${ this.baseUrl }/forbiddenKeywords`;
    return this.http.get<string[]>(url);
  }
}
