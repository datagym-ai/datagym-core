import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {SingleTaskResponseModel} from '../model/SingleTaskResponseModel';
import {LabelTaskApiService} from './label-task-api/label-task-api.service';
import {LabelTaskCompleteUpdateModel} from '../model/LabelTaskCompleteUpdateModel';
import {LabelTaskCompleteResponseModel} from '../model/LabelTaskCompleteResponseModel';
import {LabelTaskReviewModel} from '../model/LabelTaskReviewModel';
import {LabelTask} from '../../task-config/model/LabelTask';
import {ActivatedRoute} from '@angular/router';
import {LabelTaskMode} from '../model/LabelTaskMode';

@Injectable({
  providedIn: 'root'
})
export class LabelTaskService {

  /**
   * Are we in preview mode?
   * Detect via readonly property 'preview'.
   */
  public get previewMode(): boolean {
    return this.taskAPIService.previewMode;
  }

  /**
   * Are we in admin mode?
   * Detect via readonly property 'preview'.
   */
  public get adminMode(): boolean {
    return this.taskAPIService.adminMode;
  }

  constructor(private taskAPIService: LabelTaskApiService,
              private route: ActivatedRoute) {
  }

  /**
   * Are we in benchmark mode?
   */
  public get benchmarkSetMode(): boolean {
    const queryParams = this.route.snapshot.queryParams;
    if (LabelTaskMode.KEY in queryParams) {
      return queryParams[LabelTaskMode.KEY] === LabelTaskMode.SET_BENCHMARK;
    }
    return false;
  }

  /**
   * Load a single task by its id
   * @param taskId
   */
  public loadTaskById(taskId: string): Observable<SingleTaskResponseModel> {
    return this.taskAPIService.getTaskById(taskId);
  }

  public setReviewSucceed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    return this.taskAPIService.setReviewSucceed(labelTaskReviewModel);
  }

  public setReviewFailed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    return this.taskAPIService.setReviewFailed(labelTaskReviewModel);
  }

  public getNextReview(projectId?: string): Observable<LabelTask> {
    return this.taskAPIService.getNextReview(projectId);
  }

  public getNextReviewId(projectId?: string): Observable<string | undefined> {
    return this.taskAPIService.getNextReviewId(projectId);
  }

  /**
   * Set a task to status SKIPPED
   * @param labelTaskId
   */
  public setToSkipped(labelTaskId: string): Observable<unknown> {
    const status = 'skipTask';
    return this.taskAPIService.setTaskStatus(labelTaskId, status);
  }

  /**
   * Set a task to status COMPLETE
   * @param labelTaskId
   * @param body: LabelTaskCompleteUpdateModel
   */
  public setToComplete(labelTaskId: string, body: LabelTaskCompleteUpdateModel): Observable<LabelTaskCompleteResponseModel> {
    return this.taskAPIService.setTaskStatus2Complete(labelTaskId, body);
  }

  /**
   * Get the next taskId for current user
   */
  public getNextLabelTaskIdByProjectId(projectId: string): Observable<string | undefined> {
    return this.taskAPIService.getNextLabelTaskIdByProjectId(projectId);
  }
}
