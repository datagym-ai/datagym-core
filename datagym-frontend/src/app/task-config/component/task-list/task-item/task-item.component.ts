import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {LabelTask} from '../../../model/LabelTask';
import {LabelTaskState} from '../../../model/LabelTaskState';
import {LabelTaskApiService} from '../../../service/label-task-api.service';
import {take, takeUntil} from 'rxjs/operators';
import {MediaService} from '../../../../dataset/service/media.service';
import {ActivatedRoute, Router} from '@angular/router';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {DialogueModel} from '../../../../shared/dialogue-modal/DialogueModel';
import {LabelTaskMode} from '../../../../label-mode/model/LabelTaskMode';
import {PreLabelState} from '../../../model/PreLabelState';
import {Subject} from 'rxjs';
import {ProjectService} from '../../../../project/service/project.service';

@Component({
  selector: 'app-task-item',
  templateUrl: './task-item.component.html',
  styleUrls: ['./task-item.component.css']
})
export class TaskItemComponent implements OnInit, OnDestroy {

  @Input('index')
  index: number;

  @Input('task')
  task: LabelTask;
  benchmarkMode: LabelTaskMode = LabelTaskMode.SET_BENCHMARK;

  public LabelTaskState = LabelTaskState;

  public get preLabelState(): string {
    if (!!this.task.preLabelState) {
      if (this.task.preLabelState === PreLabelState.WAITING) {
        return LabelTaskState.IN_PROGRESS;
      } else {
        return this.task.preLabelState;
      }
    } else {
      if (this.task.labelTaskState === LabelTaskState.WAITING) {
        return PreLabelState.WAITING;
      } else {
        return PreLabelState.BACKLOG;
      }
    }
  }

  public get state(): LabelTaskState {
    return this.task.labelTaskState;
  }

  public get icon(): string {
    return LabelTaskState.getIcon(this.task.labelTaskState);
  }

  public get preLabelIcon(): string {
    switch (this.task.preLabelState) {
      case PreLabelState.WAITING:
        return 'icon fas fa-spinner fa-pulse fa-lg';
      case PreLabelState.FINISHED:
        return 'icon fas fa-check';
      case PreLabelState.IN_PROGRESS:
        return 'icon fas fa-spinner fa-pulse fa-lg';
      case null:
        return this.icon;
      default:
        // just in case, some states were added to the enum.
        return '';
    }
  }

  public get stateName(): string {
    return (this.state as string).replace('_', ' ');
  }

  public get isFinished(): boolean {
    return this.task.preLabelState === PreLabelState.FINISHED;
  }

  public get isFailed(): boolean {
    return this.task.preLabelState === PreLabelState.FAILED;
  }

  public get hasJsonUpload(): boolean {
    return this.task.hasJsonUpload === true;
  }

  public get id(): string {
    return this.task.taskId;
  }

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(public api: LabelTaskApiService,
              private dialogueService: DialogueService,
              private route: ActivatedRoute,
              private router: Router,
              public mediaService: MediaService,
              public projectService: ProjectService) {
  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public isState(...states: LabelTaskState[]): boolean {
    return states.includes(this.state);
  }

  public moveToWaiting(): void {
    this.api.setState(this.task, LabelTaskState.WAITING).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(response => {
      // update the task object
      this.task = response;
    });
  }

  public moveToBacklog(): void {
    this.api.setState(this.task, LabelTaskState.BACKLOG).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(response => {
      // update the task object
      this.task = response;
    });
  }

  public moveToWC(): void {
    this.api.setState(this.task, LabelTaskState.WAITING_CHANGED).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(response => {
      // update the task object
      this.task = response;
    });
  }

  /**
   *  If the reset button gets clicked, set the task to “WAITING_CHANGED” and REMOVE THE LABELER.
   *  Tasks in “WAITING_CHANGED” should be moved to “BACKLOG”.
   */
  public resetState(): void {

    const newState = this.state !== LabelTaskState.WAITING_CHANGED
      ? LabelTaskState.WAITING_CHANGED
      : LabelTaskState.BACKLOG;

    this.api.setState(this.task, newState).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(response => {
      // update the task object
      this.task = response;
    });
  }

  public resetLabeler(): void {
    this.api.resetLabeler(this.task.taskId).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(response => {
      this.task = response;
    });
  }

  public onShowMedia(): void {
    this.mediaService.openMediaInNewTab(this.task.mediaId)
      .pipe(
        take(1), takeUntil(this.unsubscribe))
      .subscribe();
  }

  public triggerBenchmark(): void {
    if (this.task.benchmark) {
      this.api.deactivateBenchmark(this.task.taskId).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(() => {
        this.task.benchmark = false;
      });
    } else {
      this.api.activateBenchmark(this.task.taskId).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(() => {
        this.task.benchmark = true;
        this.benchmarkModeDialogue();
      });
    }
  }

  private benchmarkModeDialogue(): void {
    const title = 'FEATURE.LABEL_MODE.MODE.BENCHMARK.DIALOGUE.TITLE';
    const content = 'FEATURE.LABEL_MODE.MODE.BENCHMARK.DIALOGUE.CONTENT';
    const cancelBtn = 'FEATURE.LABEL_MODE.MODE.BENCHMARK.DIALOGUE.CANCEL';
    const confirmBtn = 'FEATURE.LABEL_MODE.MODE.BENCHMARK.DIALOGUE.CONFIRM';

    const dialogueContent: DialogueModel = {title, content, buttonLeft: confirmBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    this.dialogueService.closeAction.pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((choice: boolean) => {
      if (choice === true) {
        this.router.navigate(['../../../../../label-mode/task', this.task.taskId],
          {queryParams: {mode: LabelTaskMode.SET_BENCHMARK}}).then();
      }
    });
  }

  public onExportTask(taskId: string): void {
    const url = this.projectService.getVideoTaskExportUrl(taskId);
    window.open(url, '_blank');
  }
}
