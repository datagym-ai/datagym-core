import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {PreLabelInfoViewModel} from '../model/PreLabelInfoViewModel';
import {PreLabelConfigUpdateBindingModel} from '../model/PreLabelConfigUpdateBindingModel';

@Injectable({
  providedIn: 'root'
})
export class PreLabelApiService {
  private baseUrl = '/api/prelabelconfig';

  constructor(private http: HttpClient) {}


  public getPreLabelInfoByProject(projectId: string): Observable<PreLabelInfoViewModel> {

    const url = `${this.baseUrl}/${ projectId }`;

    return this.http.get<PreLabelInfoViewModel>(url);

  }

  public updatePreLabelConfigByProject(projectId:string,
                                       config: PreLabelConfigUpdateBindingModel): Observable<PreLabelInfoViewModel> {
    const url = `${this.baseUrl}/${ projectId }`;

    return this.http.put<PreLabelInfoViewModel>(url, config);
  }

}
