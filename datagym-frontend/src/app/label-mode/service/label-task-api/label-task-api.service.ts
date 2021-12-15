import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LabelTask} from '../../../task-config/model/LabelTask';
import {map} from 'rxjs/operators';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelTaskCompleteResponseModel} from '../../model/LabelTaskCompleteResponseModel';
import {LabelTaskCompleteUpdateModel} from '../../model/LabelTaskCompleteUpdateModel';
import {LabelTaskReviewModel} from '../../model/LabelTaskReviewModel';
import {Router} from '@angular/router';
import {PreviewModeUri} from '../../model/PreviewModeUri';
import {LabelTaskApiInterface} from './label-task-api-interface';
import {LabelTaskApiPreview} from './label-task-api-preview';
import {LabelTaskApiAdmin} from './label-task-api-admin';
import {AdminModeUri} from '../../model/AdminModeUri';
// do not use brackets here around 'demoData' else the object 'demoData' would
// be searched within the json object. That does not exists.
import {AbstractLabelTaskApiService} from './abstract-label-task-api.service';

/**
 * Factory to decide, which implementation of 'LabelTaskApiInterface' should be used.
 *
 * This method stays here to not raise some circular dependencies.
 *
 * @param router
 * @param http
 */
export function labelTaskApiServiceFactory(router: Router, http: HttpClient): LabelTaskApiInterface {

  const url = router.routerState.snapshot.url;
  if (PreviewModeUri.equals(url)) {
    return new LabelTaskApiPreview();
  } else if (AdminModeUri.isAdminUrl(url)) {
    return new LabelTaskApiAdmin(http);
  }
  return new LabelTaskApiService(http);
}

/**
 * The default LabelTaskApiService implementation using the HttpClient
 * to communicate with the backend.
 */
@Injectable({
  providedIn: PreviewModeUri.PROVIDED_IN,
  useFactory: labelTaskApiServiceFactory,
  deps: [Router, HttpClient],
})
export class LabelTaskApiService extends AbstractLabelTaskApiService {

  public readonly previewMode: boolean = false;
  public readonly adminMode: boolean = false;

  private baseUrl = '/api/task';

  constructor(private http: HttpClient) {
    super();
  }

  public getTaskDataById(taskId: string): Observable<SingleTaskResponseModel> {
    const url = `${this.baseUrl}/${taskId}`;
    return this.http.get<SingleTaskResponseModel>(url);
  }

  public setReviewSucceed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    const url = `${this.baseUrl}/reviewedSuccess`;

    return this.http.put<void>(url, labelTaskReviewModel);
  }

  public setReviewFailed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    const url = `${this.baseUrl}/reviewedFailed`;

    return this.http.put<void>(url, labelTaskReviewModel);
  }

  public getNextReview(projectId?: string): Observable<LabelTask> {
    const baseUrl = '/api/user';
    const endpoint = 'nextReview';

    const url = typeof projectId === 'string' && !!projectId
      ? `${baseUrl}/${endpoint}/${projectId}`
      : `${baseUrl}/${endpoint}`;

    return this.http.get<LabelTask>(url);
  }

  /**
   * Return the next id if available else undefined.
   *
   * @param projectId
   */
  public getNextReviewId(projectId?: string): Observable<string | undefined> {
    return this.getNextReview(projectId).pipe(map((task: LabelTask) => task.taskId));
  }

  /**
   * Set a task to a new status, body is ignored.
   * Response may depend on the new status.
   *
   * @param taskId
   * @param status skipTask, completeTask
   */
  public setTaskStatus(taskId: string, status: string): Observable<unknown> {
    const url = `${this.baseUrl}/${taskId}/${status}`;
    return this.http.put<unknown>(url, null);
  }

  public setTaskStatus2Complete(taskId: string, body: LabelTaskCompleteUpdateModel): Observable<LabelTaskCompleteResponseModel> {
    const url = `${this.baseUrl}/${taskId}/completeTask`;
    return this.http.put<LabelTaskCompleteResponseModel>(url, body);
  }

  /**
   * Return the next id if available else undefined.
   *
   * @param projectId
   */
  public getNextLabelTaskIdByProjectId(projectId: string): Observable<string | undefined> {
    return this.getNextLabelTaskByProjectId(projectId).pipe(map((labelTask: LabelTask) => {
      return labelTask.taskId;
    }));
  }

  public getNextLabelTaskByProjectId(projectId: string): Observable<LabelTask> {
    const baseUrl = '/api/user';
    const endpoint = 'nextTask';
    const url = `${baseUrl}/${endpoint}/${projectId}`;
    return this.http.get<LabelTask>(url);
  }
}
