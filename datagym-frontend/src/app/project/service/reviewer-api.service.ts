import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { ProjectReviewer } from "../model/ProjectReviewer";
import { Project } from "../model/Project";
import {ReviewerConnectItem} from "../model/ReviewerConnectItem";
import {ProjectReviewerCreateBindingModel} from "../model/ProjectReviewerCreateBindingModel";

@Injectable({
  providedIn: 'root'
})
export class ReviewerApiService {

  private baseUrl = '/api/reviewer';

  constructor(private http: HttpClient) {}

  public setReviewEnabledState(projectId: string, enabled: boolean): Observable<Project> {
    const baseUrl = '/api/project';
    const url = `${baseUrl}/${ projectId }/activate`;

    let urlParams: HttpParams = new HttpParams();
    urlParams = urlParams.append('reviewActivated', String(enabled));

    return this.http.put<Project>(url, null, {params: urlParams});
  }

  public getReviewerByProjectId(id: string): Observable<ProjectReviewer[]> {
    const url = `${this.baseUrl}/${ id }`;

    return this.http.get<ProjectReviewer[]>(url);
  }

  public getAllPossibleReviewerForProject(projectId: string) : Observable<ReviewerConnectItem[]> {
    const url = `${this.baseUrl}/${ projectId }/possible`;

    return this.http.get<ReviewerConnectItem[]>(url);
  }

  public addReviewer(reviewer: ProjectReviewerCreateBindingModel): Observable<ProjectReviewer> {
    const url = `${this.baseUrl}`;

    return this.http.post<ProjectReviewer>(url, reviewer);
  }

  public deleteReviewer(id: string): Observable<void> {
    const url = `${this.baseUrl}/${ id }`;

    return this.http.delete<void>(url);
  }
}

