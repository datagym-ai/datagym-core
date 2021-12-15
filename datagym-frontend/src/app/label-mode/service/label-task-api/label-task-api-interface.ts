import {Observable} from 'rxjs';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelTaskReviewModel} from '../../model/LabelTaskReviewModel';
import {LabelTask} from '../../../task-config/model/LabelTask';
import {LabelTaskCompleteUpdateModel} from '../../model/LabelTaskCompleteUpdateModel';
import {LabelTaskCompleteResponseModel} from '../../model/LabelTaskCompleteResponseModel';

/**
 * All LabelTaskApiService implementations returned from the 'labelTaskApiServiceFactory'
 * must implement this interface. It's only here to force all implementations to implement
 * all methods.
 */
export interface LabelTaskApiInterface {

  /**
   * Are we in the demo mode?
   */
  readonly previewMode: boolean;
  readonly adminMode: boolean;

  /**
   * Get a task by its id
   * @param taskId
   */
  getTaskById(taskId: string): Observable<SingleTaskResponseModel>;

  setReviewSucceed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void>;

  setReviewFailed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void>;

  getNextReview(projectId?: string): Observable<LabelTask>;

  getNextReviewId(projectId?: string): Observable<string | undefined>;

  /**
   * Set a task to a new status, body is ignored.
   * Response may depend on the new status.
   *
   * @param taskId
   * @param status skipTask, completeTask
   */
  setTaskStatus(taskId: string, status: string): Observable<unknown>;

  setTaskStatus2Complete(taskId: string, body: LabelTaskCompleteUpdateModel): Observable<LabelTaskCompleteResponseModel>;

  getNextLabelTaskIdByProjectId(projectId: string): Observable<string | undefined>;

  getNextLabelTaskByProjectId(projectId: string): Observable<LabelTask>;
}
