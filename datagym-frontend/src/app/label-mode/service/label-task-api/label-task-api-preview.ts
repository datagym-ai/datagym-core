import {Observable, of} from 'rxjs';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelTaskReviewModel} from '../../model/LabelTaskReviewModel';
import {LabelTask} from '../../../task-config/model/LabelTask';
import {LabelTaskCompleteUpdateModel} from '../../model/LabelTaskCompleteUpdateModel';
import {LabelTaskCompleteResponseModel} from '../../model/LabelTaskCompleteResponseModel';
import {PreviewModeUri} from '../../model/PreviewModeUri';
import {LabelTaskState} from '../../../task-config/model/LabelTaskState';
// do not use brackets here around 'demoData' else the object 'demoData' would
// be searched within the json object. That does not exists.
import demoData from '../../../../assets/json/demo.json';
import {AbstractLabelTaskApiService} from './abstract-label-task-api.service';

/**
 * Within the preview mode, mock/fake the api requests.
 *
 * This is a implementation detail to support the preview mode.
 *
 * Don't use this class directly. Instead use the labelTaskApiServiceFactory
 * method to receive the right version of LabelTaskApiInterface.
 */
export class LabelTaskApiPreview extends AbstractLabelTaskApiService {

  public readonly previewMode: boolean = true;
  public readonly adminMode: boolean = false;

  public getTaskDataById(taskId: string): Observable<SingleTaskResponseModel> {
    // convert the 'string' into the expected SingleTaskResponseModel

    const taskData = demoData as unknown as SingleTaskResponseModel;
    // force the LabelTaskState.PREVIEW state.
    taskData.labelTaskState = LabelTaskState.PREVIEW;

    return of(taskData);
  }

  public setReviewSucceed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    // Reviews are not supported in the demo mode.
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public setReviewFailed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void> {
    // Reviews are not supported in the demo mode.
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public getNextReview(projectId?: string): Observable<LabelTask> {
    // Reviews are not supported in the demo mode.
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public getNextReviewId(projectId?: string): Observable<string> {
    // Reviews are not supported in the demo mode.
    return of('not_supported');
  }

  /**
   * Set a task to a new status, body is ignored
   * @param taskId
   * @param status skipTask, completeTask
   */
  public setTaskStatus(taskId: string, status: string): Observable<unknown> {
    // Task status is not supported in the demo mode.
    // Do not remove the null return value else the observable would not emit next.
    return of(null);
  }

  public setTaskStatus2Complete(taskId: string, body: LabelTaskCompleteUpdateModel): Observable<LabelTaskCompleteResponseModel> {

    const ret = {
      currentTaskId: (demoData as unknown as SingleTaskResponseModel).taskId,
      hasLabelConfigChanged: false
    };

    return of(ret);
  }

  public getNextLabelTaskIdByProjectId(projectId: string): Observable<string> {
    // Task status is not supported in the demo mode, return the same
    return of(PreviewModeUri.PATH);
  }

  public getNextLabelTaskByProjectId(projectId: string): Observable<LabelTask> {
    const singleTask = (demoData as unknown as SingleTaskResponseModel);

    return of({
      taskId: singleTask.taskId,
      projectId: singleTask.labelIteration.projectId,
      projectName: singleTask.projectName,
      labelTaskState: singleTask.labelTaskState,
      preLabelState: singleTask.preLabelState,
      mediaId: singleTask.media.id,
      mediaName: singleTask.media.mediaName,
      labeler: 'demoLabeler',
      iterationId: singleTask.labelIteration.id,
      iterationRun: singleTask.labelIteration.run
    });
  }
}
