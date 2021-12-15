import {EventEmitter, Injectable} from '@angular/core';
import {SingleTaskResponseModel} from '../model/SingleTaskResponseModel';
import {LabelTaskState} from '../../task-config/model/LabelTaskState';

/**
 * This service holds the internal state about for the task-control.component.
 *
 */
@Injectable({
  providedIn: 'root'
})
export class TaskControlService {

  /**
   * Inform the label-mode to load the next task or change the task state.
   */
  public onLoadNextTask: EventEmitter<string> = new EventEmitter<string>();
  public onSetToSkipped: EventEmitter<void> = new EventEmitter<void>();
  public onOpenTaskOverview: EventEmitter<void> = new EventEmitter<void>();
  public onSubmitAndExitTask: EventEmitter<void> = new EventEmitter<void>();
  public onSubmitAndNextTask: EventEmitter<void> = new EventEmitter<void>();

  public get projectId(): string {
    return !!this.task ? this.task.labelConfig.projectId : '';
  }

  public get reviewComment(): string {
    return !!this.task ? this.task.reviewComment : '';
  }

  public get labelTaskState(): LabelTaskState {
    return !!this.task ? this.task.labelTaskState : null;
  }

  // If  reviews are activated for the current project.
  public get reviewActivated() : boolean {
    return !!this.task.reviewActivated;
  }

  public get taskId() : string {
    return !!this.task ? this.task.taskId : '';
  }

  public get hideControlPanel(): boolean {
    const state = this.labelTaskState;
    if (state === LabelTaskState.REVIEWED) {
      // if reviewed, hide the review panel
      return true;
    }

    if (state === LabelTaskState.SKIPPED || state === LabelTaskState.COMPLETED) {
      // if reviews are not enabled, hide the review panel
      return !this.reviewActivated;
    }

    return false;
  }

  public get lastChangedConfig(): number {
    return this.task.lastChangedConfig;
  }

  public get labelIterationId(): string {
    return this.task.labelIteration.id;
  }

  private task: SingleTaskResponseModel;

  constructor() { }

  public init(task: SingleTaskResponseModel): void {
    this.task = task;
  }

  public reset(): void {
    this.task = null;
  }
}
