import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LabelTask } from '../model/LabelTask';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LabelTaskState } from '../model/LabelTaskState';
import { MoveAllDirections, MoveAllProjectsBindingModel } from '../model/MoveAllProjectsBindingModel';

@Injectable({
  providedIn: 'root'
})
export class LabelTaskApiService {

  private baseUrl = '/api/task';

  constructor(private http: HttpClient) { }

  /**
   * The endpoint is defined as /api/project/<id>/task
   * This method stays here to give access to all task related api endpoints
   * in one place.
   */
  public getAllLabelTasksByProjectId(projectId: string, params ?: HttpParams): Observable<LabelTask[]> {
    const url = `/api/project/${projectId}/task`;

    if (params === undefined) {
      params = new HttpParams();
    }

    return this.http.get<LabelTask[]>(url, {params});
  }

  public moveAllToWaiting(projectId: string, datasetId: string): Observable<any> {
    const move = new MoveAllProjectsBindingModel(projectId, datasetId, MoveAllDirections.TO_WAITING);
    return this.moveAllTo(move);
  }

  public moveAllToBacklog(projectId: string, datasetId: string): Observable<any> {
    const move = new MoveAllProjectsBindingModel(projectId, datasetId, MoveAllDirections.TO_BACKLOG);
    return this.moveAllTo(move);
  }

  public moveAllTo(move: MoveAllProjectsBindingModel): Observable<any> {
    const url = `${this.baseUrl}/moveAll`;

    return this.http.put(url, move);
  }

  public setState(labelTask: LabelTask, newState: LabelTaskState): Observable<any> {
    const endpoint = LabelTaskState.getEndPoint(newState);
    const url = `${this.baseUrl}/${labelTask.taskId}/${ endpoint }`;

    return this.http.put(url, labelTask);
  }

  public activateBenchmark(labelTaskId: string): Observable<void> {
    const endpoint = 'activateBenchmark';
    const url = `${ this.baseUrl }/${ labelTaskId }/${endpoint}`;

    return this.http.put<void>(url, null);
  }

  public deactivateBenchmark(labelTaskId: string): Observable<void> {
    const endpoint = 'deactivateBenchmark';
    const url = `${ this.baseUrl }/${ labelTaskId }/${endpoint}`;

    return this.http.put<void>(url, null);
  }

  public resetLabeler(labelTaskId: string): Observable<LabelTask> {
    const endpoint = 'resetLabeler';
    const url = `${ this.baseUrl }/${ labelTaskId }/${endpoint}`;

    return this.http.put<LabelTask>(url, null);
  }
}
