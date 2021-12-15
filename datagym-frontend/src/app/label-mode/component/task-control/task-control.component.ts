import {AfterViewInit, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {LabelTaskState} from '../../../task-config/model/LabelTaskState';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {LabelTaskService} from '../../service/label-task.service';
import {TaskControlService} from '../../service/task-control.service';
import {LabelModeUtilityService} from '../../service/label-mode-utility.service';
import {AppButtonInput} from '../../../shared/button/button.component';
import {DialogueModel} from '../../../shared/dialogue-modal/DialogueModel';
import {Subscription} from 'rxjs';
import {DialogueService} from '../../../shared/service/dialogue.service';
import {LabelTaskReviewModel} from '../../model/LabelTaskReviewModel';
import {concatMap} from 'rxjs/operators';


@Component({
  selector: 'app-task-control',
  templateUrl: './task-control.component.html',
  styleUrls: ['./task-control.component.css']
})
export class TaskControlComponent implements OnInit, AfterViewInit, OnDestroy {

  @Input()
  public taskValid: boolean = false;

  private readonly maxCommentLength = 128;

  public get taskId(): string {
    return this.taskControlService.taskId;
  }
  public get state(): LabelTaskState {
    return this.taskControlService.labelTaskState;
  }
  public get message(): string {
    return this.taskControlService.reviewComment;
  }

  public stateIsSkipped(): boolean{
    return this.state === LabelTaskState.SKIPPED;
  }

  public toggleMessage: boolean = true;

  /**
   * used to disable the task controls if the user is in drawing mode
   * or aiseg is active
   */
  public get enableTaskControl(): boolean {
    return !this.utilityService.userIsDrawing &&
      !this.utilityService.aiSegActive;
  }

  public reviewForm: FormGroup = new FormGroup({
    'comment': new FormControl(null, [Validators.maxLength(this.maxCommentLength)]),
  });

  public LabelTaskState = LabelTaskState;

  private dialogueSkipSubscription: Subscription;


  constructor(
    private dialogueService: DialogueService,
    private labelTaskService: LabelTaskService,
    private taskControlService: TaskControlService,
    private utilityService: LabelModeUtilityService
  ) { }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    // create the FormGroup to handle the review comment.
    this.reviewForm = new FormGroup({
      'comment': new FormControl(this.message || null,[
        Validators.maxLength(this.maxCommentLength)
      ]),
    });
  }

  ngOnDestroy(): void {
    // handle skip dialogue without unsubscribe subject.
    if (this.dialogueSkipSubscription) {
      this.dialogueSkipSubscription.unsubscribe();
    }
  }

  /*
   * Handle tasks
   */

  /**
   * Set task to skipped and go to next task on success
   */
  public onSkipTask(): void {
    const title = 'FEATURE.LABEL_MODE.DIALOGUE.TITLE.SKIP_TASK';
    const content = 'FEATURE.LABEL_MODE.DIALOGUE.CONTENT.SKIP_TASK';
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = { label: 'GLOBAL.CONFIRM', styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    // the dialogueService is only used to display a warning on skipping the task.
    this.dialogueSkipSubscription = this.dialogueService.closeAction.subscribe((del: boolean) => {
      if (this.dialogueSkipSubscription) {
        this.dialogueSkipSubscription.unsubscribe();
      }
      if (!del) {
        return;
      }
      this.taskControlService.onSetToSkipped.emit();
    });
  }

  public onSubmitAndExitTask(): void {
    this.taskControlService.onSubmitAndExitTask.emit();
  }

  public onSubmitAndNextTask(): void {
    this.taskControlService.onSubmitAndNextTask.emit();
  }

  /*
   * Handle review states
   */

  public onDenyReview(): void {

    const labelTaskReviewModel: LabelTaskReviewModel = {
      taskId: this.taskId,
      reviewComment: this.reviewComment
    };

    const projectId = this.taskControlService.projectId;
    this.labelTaskService.setReviewFailed(labelTaskReviewModel).pipe(
      concatMap(() => this.labelTaskService.getNextReviewId(projectId))
    ).subscribe((nextTaskId: string | undefined) => {
      this.taskControlService.onLoadNextTask.emit(nextTaskId);
    }, () => {
      // goto tasks config page
      this.taskControlService.onOpenTaskOverview.emit();
    });
  }

  public onAcceptReview(): void {

    const labelTaskReviewModel: LabelTaskReviewModel = {
      taskId: this.taskId,
      reviewComment: this.reviewComment
    };

    const projectId = this.taskControlService.projectId;
    this.labelTaskService.setReviewSucceed(labelTaskReviewModel).pipe(
      concatMap(() => this.labelTaskService.getNextReviewId(projectId))
    ).subscribe((nextTaskId: string | undefined) => {
      this.taskControlService.onLoadNextTask.emit(nextTaskId);
    }, () => {
      // goto tasks config page
      this.taskControlService.onOpenTaskOverview.emit();
    });
  }

  public onGoHome(): void {
    this.taskControlService.onOpenTaskOverview.emit();
  }

  private get reviewComment(): string {
    return !!this.reviewForm && !!this.reviewForm.value && !!this.reviewForm.value.comment
      ? this.reviewForm.value.comment
      : '';
  }
}
