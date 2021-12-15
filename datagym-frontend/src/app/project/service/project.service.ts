import { Injectable } from '@angular/core';
import { Project } from '../model/Project';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ProjectCreateBindingModel } from '../model/ProjectCreateBindingModel';
import { ProjectUpdateBindingModel } from '../model/ProjectUpdateBindingModel';
import { ProjectDashboard } from '../model/ProjectDashboard';
import { ProjectLabelCountByDayViewModel } from '../model/ProjectLabelCountByDayViewModel';
import { ProjectGeometryCountsViewModel } from '../model/ProjectGeometryCountsViewModel';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private baseUrl = '/api/project';

  constructor(private http: HttpClient) {
  }

  public createProject(newProject: ProjectCreateBindingModel): Observable<Project> {
    return this.http.post<Project>(this.baseUrl, newProject);
  }

  public updateProject(id: string, updatedProject: ProjectUpdateBindingModel): Observable<Project> {
    return this.http.put<Project>(`${this.baseUrl}/${id}`, updatedProject);
  }

  public deleteProject(id: string): Observable<Project> {
    return this.http.delete<Project>(`${this.baseUrl}/${id}`);
  }

  public restoreProject(id: string): Observable<Project> {
    return this.http.delete<Project>(`${this.baseUrl}/${id}/restore`);
  }

  public fetchAllProjectsAsAccountAdmin(): Observable<Project[]> {
    const url = '/api/superadmin/project';

    return this.http.get<Project[]>(url);
  }

  /**
   * List all projects connected with the logged in user.
   * @param withAdminPermissionOnly optional flag to only list projects, where the user is admin
   */
  public fetchProjects(withAdminPermissionOnly: boolean = false): Observable<Project[]> {

    const url = !!withAdminPermissionOnly
      ? `${this.baseUrl}/admin`
      : this.baseUrl;

    return this.http.get<Project[]>(url);
  }

  public getAllProjectsFromOrganisation(org: string): Observable<Project[]> {
    const url = `${this.baseUrl}/${org}/org`;

    return this.http.get<Project[]>(url);
  }

  public getSuitableProjectsForDataset(datasetId: string): Observable<Project[]> {
    let urlParams: HttpParams = new HttpParams();
    urlParams = urlParams.append('datasetId', datasetId);
    const url = `${this.baseUrl}/suitableDatasetConnections`;

    return this.http.get<Project[]>(url, {params: urlParams});
  }

  public getProjectById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/${id}`);
  }

  public pinProject(id: string): Observable<Project> {
    return this.http.post<Project>(`${this.baseUrl}/${id}/pin`, {});
  }

  public unPinProject(id: string): Observable<Project> {
    return this.http.post<Project>(`${this.baseUrl}/${id}/unpin`, {});
  }

  public connectWithDataset(projectID: string, datasetID: string): Observable<void> {
    const url = `${this.baseUrl}/${projectID}/dataset/${datasetID}`;
    return this.http.post<void>(url, {});
  }

  public deleteConnectionToDataset(projectID: string, datasetID: string): Observable<void> {
    const url = `${this.baseUrl}/${projectID}/dataset/${datasetID}/remove`;
    return this.http.delete<void>(url, {});
  }

  public getExportUrl(projectId: string): string {
    return `${this.baseUrl}/${projectId}/export`;
  }

  public getVideoTaskExportUrl(taskId: string): string {
    return `${this.baseUrl}/exportVideoTask/${taskId}`;
  }

  public getProjectDashboardById(id: string): Observable<ProjectDashboard> {
    return this.http.get<ProjectDashboard>(`${this.baseUrl}/${id}/dashboard`);
  }

  public getProjectDashboardGeometryCount(projectId: string): Observable<ProjectGeometryCountsViewModel> {
    return this.http.get<ProjectGeometryCountsViewModel>(`${this.baseUrl}/${projectId}/dashboard/geometryCounts`);
  }


  public getProjectDashboardGeometryCountByDays(projectId: string): Observable<ProjectLabelCountByDayViewModel> {
    return this.http.get<ProjectLabelCountByDayViewModel>(`${this.baseUrl}/${projectId}/dashboard/geometryCountsByDay`);
  }
}
