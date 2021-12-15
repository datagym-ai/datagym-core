import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { TaskViewModel } from "../model/TaskViewModel";
import { map } from "rxjs/operators";
import { LabelTask } from "../../task-config/model/LabelTask";

@Injectable({
  providedIn: 'root'
})
export class TaskOverviewService {

  private baseUrl = '/api/user';

  constructor(private http: HttpClient) { }

  public getTaskOverview(): Observable<TaskViewModel[]> {
    const url = `${ this.baseUrl }/taskList`;
    return this.http.get<TaskViewModel[]>(url);
  }

  public getNextTaskByProjectId(projectId: string): Observable<LabelTask> {
    const url = `${ this.baseUrl }/nextTask/${ projectId }`;
    return this.http.get<LabelTask>(url);
  }

  public getNextTaskIdByProjectId(projectId: string): Observable<string> {
    return this.getNextTaskByProjectId(projectId).pipe(map(response => response.taskId));
  }

  /**
   * This method is a copy from LabelTaskApiService within the label mode.
   * Copied to not have some dependencies.
   * @param projectId
   */
  public getNextReview(projectId?: string): Observable<LabelTask> {
    const baseUrl = '/api/user';
    const endpoint = 'nextReview';

    const url = typeof projectId === 'string' && !!projectId
      ? `${ baseUrl }/${ endpoint }/${projectId}`
      : `${ baseUrl }/${ endpoint }`;

    return this.http.get<LabelTask>(url);
  }

  /**
   * This method is a copy from LabelTaskApiService within the label mode.
   * Copied to not have some dependencies.
   * @param projectId
   */
  public getNextReviewId(projectId?: string): Observable<string> {
    return this.getNextReview(projectId).pipe(map((task: LabelTask) => task.taskId));
  }
}
