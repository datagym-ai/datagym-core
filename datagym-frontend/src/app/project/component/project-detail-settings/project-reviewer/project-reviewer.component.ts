import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ProjectReviewer} from '../../../model/ProjectReviewer';
import {ReviewerApiService} from '../../../service/reviewer-api.service';
import {Project} from '../../../model/Project';
import {ActivatedRoute} from '@angular/router';
import {debounceTime, map, takeUntil} from 'rxjs/operators';
import {DgSelectModalService} from '../../../../shared/dg-select-modal/dg-select-modal.service';
import {DgSelectOptionModel} from '../../../../shared/dg-select-modal/DgSelectOptionModel';
import {LabNotificationService} from '../../../../client/service/lab-notification.service';
import {AppButtonInput} from '../../../../shared/button/button.component';
import {DialogueModel} from '../../../../shared/dialogue-modal/DialogueModel';
import {DialogueService} from '../../../../shared/service/dialogue.service';
import {Subject, Subscription} from 'rxjs';
import {ReviewerConnectItem} from '../../../model/ReviewerConnectItem';
import {ProjectReviewerCreateBindingModel} from '../../../model/ProjectReviewerCreateBindingModel';


@Component({
  selector: 'app-project-reviewer',
  templateUrl: './project-reviewer.component.html',
  styleUrls: ['./project-reviewer.component.css']
})

export class ProjectReviewerComponent implements OnInit, OnDestroy {

  public reviewers: ProjectReviewer[] = [];
  public enableReviews: FormGroup = null;
  public reviewerModalId: string = 'reviewerModalId';

  public get enabled(): boolean {
    return !!this.enableReviews.get('enable').value;
  }

  public set enabled(enable: boolean) {
    this.enableReviews.get('enable').patchValue(enable, {emitEvent: false});
  }

  public get hasReviewer(): boolean {
    return this.reviewers.length > 0;
  }

  private projectId: string = '';
  private dialogueSub: Subscription;
  private valueChangeSub: Subscription;
  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();
  private stateOnLoading: boolean;

  constructor(
    private route: ActivatedRoute,
    private dialogueService: DialogueService,
    private reviewerService: ReviewerApiService,
    private dgSelectModalService: DgSelectModalService,
    private labNotificationService: LabNotificationService
  ) { }

  ngOnInit(): void {
    this.projectId = (this.route.parent.snapshot.data.project as Project).id;
    this.stateOnLoading = !!(this.route.parent.snapshot.data.project as Project).reviewActivated;

    // init the form group
    this.enableReviews = new FormGroup({
      'enable': new FormControl(this.stateOnLoading, Validators.required),
    });

    const halfSecond = 500;
    this.enableReviews.get('enable').valueChanges.pipe(
      debounceTime(halfSecond),
      takeUntil(this.unsubscribe)
    ).subscribe((enable: boolean) => {
      // Note: do not merge the pipes within that method with this pipe,
      // on error it would no longer work.
      this.updateEnableStateInBackend(enable);
    });
    this.reloadReviewers();
  }

  ngOnDestroy(): void {
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.valueChangeSub) {
      this.valueChangeSub.unsubscribe();
    }
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  onInitReviewerAdding(): void {
    if (!this.enabled) {
      return;
    }

    this.reviewerService.getAllPossibleReviewerForProject(this.projectId)
      .pipe(map((reviewers: ReviewerConnectItem[]) =>
        reviewers.map((reviewer: ReviewerConnectItem) => {
          return {id: reviewer.id, label: reviewer.name};
        })
      ))
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((options: DgSelectOptionModel[]) => {
        if (options.length === 0) {
          const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.REVIEW.NOTIFICATION.NO_SUPPORTED_REVIEWER_FOUND';
          this.labNotificationService.error_i18(translateKey);
          return;
        }
        this.dgSelectModalService.openDialogue(options, this.reviewerModalId);
      });
  }

  addReviewer($event: string): void {

    const newProjectReviewer = new ProjectReviewerCreateBindingModel();
    newProjectReviewer.userId = $event;
    newProjectReviewer.projectId = this.projectId;

    this.reviewerService.addReviewer(newProjectReviewer).subscribe((reviewer: ProjectReviewer) => {
      const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.REVIEW.NOTIFICATION.REVIEWER_ADDED';
      this.labNotificationService.success_i18(translateKey);
      this.reviewers.push(reviewer);
    });
  }

  onDelete($event: string) {
    // should the delete option also be disabled when reviewers are not supported?
    const title = 'FEATURE.PROJECT.DETAIL.SETTINGS.REVIEW.MODAL.DELETE_REVIEWER_TITLE';
    const content = 'FEATURE.PROJECT.DETAIL.SETTINGS.REVIEW.MODAL.DELETE_REVIEWER_CONTENT';
    const deleteBtn = 'GLOBAL.DELETE';
    const cancelBtn = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = { label: deleteBtn, styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);

    this.dialogueSub = this.dialogueService.closeAction.subscribe((choice: boolean) => {
      this.dialogueSub.unsubscribe();
      if (choice !== true) {
        return;
      }
      this.reviewerService.deleteReviewer($event).subscribe(() => {
        const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.REVIEW.NOTIFICATION.REMOVED_REVIEWER';
        this.labNotificationService.success_i18(translateKey);
        this.reloadReviewers();
      });
    });
  }

  private reloadReviewers(): void {
    this.reviewerService.getReviewerByProjectId(this.projectId).subscribe((reviewers: ProjectReviewer[]) => {
      this.reviewers = reviewers;
    });
  }

  /**
   * Update the previewer enabled state.
   * Do not merge this subscriptions with the calling pipe.
   *
   * @param newState
   */
  private updateEnableStateInBackend(newState: boolean): void {
    if (this.valueChangeSub) {
      this.valueChangeSub.unsubscribe();
    }
    this.valueChangeSub = this.reviewerService.setReviewEnabledState(this.projectId, newState).pipe(
      map((project: Project) => { return !!project.reviewActivated; }),
      takeUntil(this.unsubscribe),
    ).subscribe((enable: boolean) => {
      this.enabled = enable;
    }, () => {
      // on error set the enabled flag back to the previous one, set by loading this page.
      this.enabled = this.stateOnLoading;
    });
  }
}
