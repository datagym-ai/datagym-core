import {Injectable} from '@angular/core';
import {DatasetDetail} from '../model/DatasetDetail';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {DatasetCreateBindingModel} from '../model/DatasetCreateBindingModel';
import {Media} from '../../basic/media/model/Media';
import {DatasetUpdateBindingModel} from '../model/DatasetUpdateBindingModel';
import {UrlImageUploadViewModel} from '../../basic/media/model/UrlImageUploadViewModel';
import {DatasetList} from '../model/DatasetList';
import {PageReturn} from '../model/PageReturn';
import {DatasetFilterAndPageParam} from '../model/DatasetFilterAndPageParam';


@Injectable({
  providedIn: 'root'
})
export class DatasetService {
  private baseURL: string = '/api/dataset';

  constructor(private http: HttpClient) {
  }

  public getAllDatasetsAsAccountAdmin(): Observable<DatasetList[]> {
    const url = '/api/superadmin/dataset';

    return this.http.get<DatasetList[]>(url);
  }

  /**
   * Use an optional project owner id to filter the result.
   */
  public getDatasets(org ?: string): Observable<DatasetList[]> {
    let urlParams: HttpParams = new HttpParams();
    if (!!org) {
      urlParams = urlParams.append('org', org);
    }
    return this.http.get<DatasetList[]>(this.baseURL, {params: urlParams});
  }

  public getProjectSuitableDatasets(projectId: string): Observable<DatasetList[]> {
    let urlParams: HttpParams = new HttpParams();
    urlParams = urlParams.append('projectId', projectId);
    return this.http.get<DatasetList[]>(`${this.baseURL}/projectSuitable`, {params: urlParams});
  }

  public getDatasetById(id: string): Observable<DatasetDetail> {
    return this.http.get<DatasetDetail>(`${this.baseURL}/${id}`);
  }

  public getDatasetMedia(id: string, datasetFilterAndPageParam: DatasetFilterAndPageParam): Observable<PageReturn<Media>> {
    let params = new HttpParams();
    params = params.append('pageIndex', datasetFilterAndPageParam.pageIndex.toString());
    params = params.append('numberOfElementsPerPage', datasetFilterAndPageParam.numberOfElementsPerPage.toString());
    if (datasetFilterAndPageParam.mediaName != null) {
      params = params.append('mediaName', datasetFilterAndPageParam.mediaName.toString());
    }
    if (datasetFilterAndPageParam.mediaSourceType != null) {
      params = params.append('mediaSourceType', datasetFilterAndPageParam.mediaSourceType.toString());
    }

    return this.http.get<PageReturn<Media>>(`${this.baseURL}/${id}/media`, {params});
  }

  public createDataset(newDataset: DatasetCreateBindingModel): Observable<DatasetDetail> {
    return this.http.post<DatasetDetail>(`${this.baseURL}`, newDataset);
  }

  public updateDataset(idToUpdate: string, newDataset: DatasetUpdateBindingModel): Observable<DatasetDetail> {
    return this.http.put<DatasetDetail>(`${this.baseURL}/${idToUpdate}`, newDataset);
  }

  public deleteDatasetById(idToDelete: string): Observable<DatasetDetail> {
    return this.http.delete<DatasetDetail>(`${ this.baseURL }/${ idToDelete }`);
  }

  public restoreDataset(idToRestore: string): Observable<DatasetDetail> {
    return this.http.delete<DatasetDetail>(`${ this.baseURL }/${ idToRestore }/restore`);
  }

  public createImagesByShareableLink(datasetId: string, urls: string[]): Observable<UrlImageUploadViewModel[]> {

    const url = `${this.baseURL}/${datasetId}/url`;

    return this.http.post<UrlImageUploadViewModel[]>(url, urls);
  }
}
