import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {LabelTask} from '../../../task-config/model/LabelTask';
import {LabelModeType, LcEntry, LcEntryClassification, LcEntryType} from '../../model/import';
import {LabelTaskApiInterface} from './label-task-api-interface';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelTaskReviewModel} from '../../model/LabelTaskReviewModel';
import {LabelTaskCompleteUpdateModel} from '../../model/LabelTaskCompleteUpdateModel';
import {LabelTaskCompleteResponseModel} from '../../model/LabelTaskCompleteResponseModel';
import {LcEntryContainer} from '../../model/value-response-factory/LcEntryContainer';
import {LcEntryValueFactory} from '../../model/value-response-factory/LcEntryValueFactory';
import {LcEntryValue} from '../../model/LcEntryValue';


export abstract class AbstractLabelTaskApiService implements LabelTaskApiInterface {
  readonly abstract adminMode: boolean;
  readonly abstract previewMode: boolean;

  /**
   * Get a task by its id
   * @param taskId
   */
  public getTaskById(taskId: string): Observable<SingleTaskResponseModel> {
    return this.getTaskDataById(taskId).pipe(
      tap(task => AbstractLabelTaskApiService.enforceProjectType(task)),
      tap(task => AbstractLabelTaskApiService.enforceUrlProperty(task)),
      tap(task => AbstractLabelTaskApiService.setVideoProperties(task)),
      tap(task => AbstractLabelTaskApiService.filterInvalidChangeObjects(task)),
      tap(task => AbstractLabelTaskApiService.setRequiredFlag2false4video(task)),
      map(response => AbstractLabelTaskApiService.createObjectsFromResponse(response))
    );
  }

  /**
   * Return here the abstract data
   * @param taskId
   */
  abstract getTaskDataById(taskId: string): Observable<SingleTaskResponseModel>;

  abstract getNextLabelTaskByProjectId(projectId: string): Observable<LabelTask>;

  abstract getNextLabelTaskIdByProjectId(projectId: string): Observable<string | undefined>;

  abstract getNextReview(projectId?: string): Observable<LabelTask>;

  abstract getNextReviewId(projectId?: string): Observable<string | undefined>;

  abstract setReviewFailed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void>;

  abstract setReviewSucceed(labelTaskReviewModel: LabelTaskReviewModel): Observable<void>;

  abstract setTaskStatus(taskId: string, status: string): Observable<unknown>;

  abstract setTaskStatus2Complete(taskId: string, body: LabelTaskCompleteUpdateModel): Observable<LabelTaskCompleteResponseModel>;

  /**
   * The response object is not a 'plain' SingleTaskResponseModel because the lcEntryValues
   * attributes contain 'mixed' objects cast that 'mixed objects' to lcEntryValues.
   *
   * @param response
   * @private
   */
  private static createObjectsFromResponse(response): SingleTaskResponseModel {
    const entryContainer = new LcEntryContainer(response.labelConfig.entries);
    response.labelIteration.entryValues = (response.labelIteration.entryValues as any[]).map(value => {
      return LcEntryValueFactory.castFromObject(value, entryContainer.findRecursiveById(value.lcEntryId));
    });
    return response;
  }

  /**
   * The projectType property is not set within the demo data.
   *
   * Migration helper for the video labeling task.
   *
   * @param task
   * @private
   */
  private static enforceProjectType(task: SingleTaskResponseModel): void {
    if (!/*not*/!!task.projectType) {
      task.projectType = LabelModeType.IMAGE;
    }
  }

  /**
   * On some media types the url is not set by the BE.
   *
   * Migration helper for the video labeling task.
   *
   * @param task
   * @private
   */
  private static enforceUrlProperty(task: SingleTaskResponseModel): void {
    if (!/*not*/!!task.media.url) {
      task.media.url = `/api/media/${task.media.id}`;
    }
  }

  /**
   * @param task
   * @private
   */
  private static setVideoProperties(task: SingleTaskResponseModel): void {

    if (task.projectType !== LabelModeType.VIDEO) {
      return;
    }

    // rFrameRate is a string like '24/1' that must be used to calculate the fps.
    const rFrameRate: string = {rFrameRate: '', ...task.media}.rFrameRate;
    const split = rFrameRate.split('/').splice(0, 2);
    const fps = split
      .map(s => parseInt(s, 10))
      .reduce((a, b) => {
      return a / b;
    });

    const videoProperty = {fps};

    task.media = {...videoProperty, ...task.media};
  }

  /**
   * Ignore invalid change objects.
   *
   * Change objects without a valid frame number are not supported.
   *
   * @param task
   * @private
   */
  private static filterInvalidChangeObjects(task: SingleTaskResponseModel) {
    task.labelIteration.entryValues.forEach(entryValue => {
      entryValue.change = (entryValue.change || []).filter(change =>
        change.frameNumber !== undefined && change.frameNumber !== null && typeof change.frameNumber === 'number'
      );
    });
  }

  /**
   * @deprecated Remove when the video labeling mode supports required flags.
   *
   * @param task
   * @private
   */
  private static setRequiredFlag2false4video(task: SingleTaskResponseModel) {

    // Set the configuration for classifications to 'not required'
    // so new created values are flagged as 'not required'.
    const requiredCallback = (entry: LcEntry): void => {
      if (LcEntryType.isClassification(entry)) {
        (entry as LcEntryClassification).required = false;
      }
      entry.children.forEach(requiredCallback);
    };

    // Set all values to 'valid' until we support that flag.
    const validCallback = (value: LcEntryValue): void => {
      value.valid = false;
      value.children.forEach(validCallback);
    }

    if (task.projectType === LabelModeType.VIDEO) {
      task.labelConfig.entries.forEach(requiredCallback);
      task.labelIteration.entryValues.forEach(validCallback);
    }
  }
}
