import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Subject} from 'rxjs';
import {filter, take, takeUntil} from 'rxjs/operators';
import {LabelTaskService} from '../../service/label-task.service';
import {SingleTaskResponseModel} from '../../model/SingleTaskResponseModel';
import {LabelModeUtilityService} from '../../service/label-mode-utility.service';
import {Project} from '../../../project/model/Project';
import {WorkspaceListenerService} from '../../service/workspace-listener.service';
import {LabelTaskCompleteUpdateModel} from '../../model/LabelTaskCompleteUpdateModel';
import {LabelTaskCompleteResponseModel} from '../../model/LabelTaskCompleteResponseModel';
import {LabNotificationService} from '../../../client/service/lab-notification.service';
import {TaskControlService} from '../../service/task-control.service';
import {ContextMenuWorkerService} from '../../service/context-menu-worker.service';
import {PreviewModeUri} from '../../model/PreviewModeUri';
import {WorkspaceControlService} from '../../../svg-workspace/service/workspace-control.service';
import {AisegService} from '../../service/aiseg.service';
import {PreviewGuideService} from '../../service/preview-guide.service';
import {MediaControlService} from '../../service/media-control.service';
import {environment} from 'src/environments/environment';
import {VideoContextMenuService} from '../../service/video-context-menu.service';
import {ValidityObserver} from '../../service/media-controller/validity/ValidityObserver';
import {DummyValidityObserver} from '../../service/media-controller/validity/DummyValidityObserver';


@Component({
  selector: 'app-label-mode',
  templateUrl: './label-mode.component.html',
  styleUrls: ['./label-mode.component.css']
})
export class LabelModeComponent implements OnInit, OnDestroy {
  // this state is defined by the project name and required to disable aiseg.
  public isDummyProject: boolean = false;

  public get globalClassificationsValid(): boolean {
    return this.validityObserver.globalClassificationsValid;
  }

  /**
   * Display the workspace-loading.gif until this value is set to true.
   */
  public valuesLoaded: boolean = false;

  public get projectName(): string {
    return !/*not*/!!this.task ? '' : this.task.projectName;
  }

  /**
   * Disable the browser warning banner on development stage.
   */
  public get isProd(): boolean {
    return !!environment.production;
  }

  public get disableDemoMode(): boolean {
    return !this.labelTaskService.previewMode || this.labelTaskService.adminMode;
  }

  public taskId: string;
  // Label task received from backend and value-service prepared
  private labelTaskLoaded: boolean = false;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  private task: SingleTaskResponseModel;

  private validityObserver: ValidityObserver = new DummyValidityObserver();

  public get hideControlPanel(): boolean {
    return this.taskControlService.hideControlPanel;
  }

  constructor(
    public previewGuide: PreviewGuideService,
    public mediaControl: MediaControlService,
    private router: Router,
    private route: ActivatedRoute,
    private aisegService: AisegService,
    private labelTaskService: LabelTaskService,
    private contextMenu: ContextMenuWorkerService,
    private taskControlService: TaskControlService,
    private videoContextMenu: VideoContextMenuService,
    private labNotificationService: LabNotificationService,
    private labelModeUtilityService: LabelModeUtilityService,
    private workspaceControlService: WorkspaceControlService,
    private workspaceListenerService: WorkspaceListenerService
  ) {
  }

  get valuesSelected(): boolean {
    return this.labelModeUtilityService.valueTree.length > 0;
  }

  get isTaskLoaded(): boolean {
    return this.labelTaskLoaded;
  }

  ngOnInit() {
    this.labelModeUtilityService.init();

    /*
     * Handle control events from task-control component & service.
     */
    this.taskControlService.onLoadNextTask.pipe(takeUntil(this.unsubscribe)).subscribe((nextTaskId: string) => {
      this.loadTaskById(nextTaskId);
    });
    this.taskControlService.onSetToSkipped.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.setToSkipped();
    });
    this.taskControlService.onSubmitAndExitTask.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.onSubmitAndExitTask();
    });
    this.taskControlService.onSubmitAndNextTask.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.onSubmitAndNextTask();
    });
    this.taskControlService.onOpenTaskOverview.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.onHomeClicked();
    });

    this.mediaControl.valuesLoaded$.pipe(filter(value => value === true), take(1), takeUntil(this.unsubscribe)).subscribe(() => {
      this.valuesLoaded = true;
      this.validityObserver = this.mediaControl.getValidityObserver();
    });

    this.workspaceListenerService.initializeWorkspaceListeners();
    // Listen on param changes to detect loading of next task
    this.route.params.pipe(takeUntil(this.unsubscribe)).subscribe(params => {

      let taskId: string | undefined = params['id'];
      const inAdminPath: boolean = !!this.route.snapshot.data['SUPER_ADMIN'];
      const inPreviewPath: boolean = !!this.route.snapshot.data['PREVIEW_MODE'];

      /*
       * In admin mode we expect that the admin mode service is loaded
       * and while *not* in admin mode, the admin service must not be loaded.
       */
      if (inAdminPath && !this.labelTaskService.adminMode || !inAdminPath && this.labelTaskService.adminMode) {
        location.reload();
        return false;
      }

      /*
       * In demo mode we require the preview services and:
       * - no task id while not within the admin preview
       * - a task id while in the admin preview
       * Note: the adminMode is a sub-mode of previewMode
       */
      if (inPreviewPath) {
        if (!this.labelTaskService.previewMode) {
          location.reload();
          return false;
        }
        if (taskId === undefined && this.labelTaskService.adminMode) {
          location.reload();
          return false;
        }
        if (!!taskId && !this.labelTaskService.adminMode) {
          location.reload();
          return false;
        }
        if (!this.labelTaskService.adminMode) {
          // On demo mode, set the 'undefined' id to any not empty string
          taskId = 'demo';
        }
      }

      const frame: number = Object.keys(params).includes('frame') && !isNaN(+params['frame'])
        ? +params['frame']
        : undefined;

      if (taskId !== undefined) {
        this.loadLabelTask(taskId, frame);
      }
    });
  }

  ngOnDestroy(): void {
    this.mediaControl.teardown();
    this.labelModeUtilityService.reset();
    this.workspaceListenerService.reset();
    this.taskControlService.reset();
    this.contextMenu.reset();
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  /**
   * On home 'button' clicked, go back to the tasks overview page
   * or in demo mode return to datagyms home page.
   */
  onHomeClicked() {
    if (this.labelModeUtilityService.userIsDrawing) {
      this.workspaceControlService.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
    }
    if (this.labelTaskService.adminMode) {
      // /admin/projects/details/4c3fe28f-b8e7-4256-a2d0-744e940d663c/home
      this.router.navigate(['/admin', 'projects', 'details', this.task.labelIteration.projectId, 'tasks', 'configure']).then();
    } else if (this.labelTaskService.previewMode) {
      PreviewModeUri.leavePreviewMode();
    } else {
      this.router.navigate(['/']).then();
    }
  }

  /**
   * Handle a click on global Classifications, cancels drawing if necessary, unselect geometry, reset state and get
   * the globalClassifications from the service to show them in selectedValueTree.
   */
  public onGlobalClicked() {
    this.labelModeUtilityService.onGeometryOrGlobalClicked(null);
  }

  @HostListener('body:keydown', ['$event'])
  private handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.labelModeUtilityService.cancelAISeg();
      this.labelModeUtilityService.unselectAllGeometries();
      if (this.labelModeUtilityService.userIsDrawing) {
        this.workspaceControlService.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
      }
    }
    // Arrow keys left & right *with* ctrlKey are used to iterate through the entry value list and select the next one.
    if (event.ctrlKey && this.mediaControl.getValidityObserver().hasGeometries() && ['ArrowLeft', 'ArrowRight'].includes(event.key)) {
      this.iterateSelectedValues(event.key === 'ArrowRight');
    }
  }

  @HostListener('body:keyup', ['$event'])
  private handleKeyUp(event: KeyboardEvent): void {
    /*
     * Shortcuts with target:
     * - input for comments
     * - textarea for text classifications
     * are ignored.
     */
    const nodeName = (event.target as Element).nodeName.toLowerCase();
    if (nodeName === 'textarea') {
      return;
    }
    if ('input' === nodeName) {
      /*
       * Exclude range sliders (like in 'DgSliderComponent').
       *
       * Using the time slider within  video labeling mode
       * the slider will stop/pause the video. While that slider has the focus, the
       * shortcuts would not work so exclude here input#slider of type 'range' as
       * used within `DgSliderComponent`.
       *
       * Using `input.blur()` method on that slider is also no option. That would
       * remove the focus and the mouse (second+) events would not work.
       */
      if (!((event.target as HTMLInputElement).type === 'range' && (event.target as Element).id === 'slider')) {
        return;
      }
    }

    const shortcuts = this.mediaControl.getShortCuts();
    shortcuts.forEach(shortcut => {
      const keys = Array.isArray(shortcut.key) ? shortcut.key : [shortcut.key];
      if (keys.includes(event.key)) {
        const shiftRequired = !!shortcut.shiftKey;
        if (!shiftRequired || !!shiftRequired && event.shiftKey) {
          shortcut.callback();
        }
      }
    });
  }

  public iterateSelectedValues(next: boolean): void {
    this.labelModeUtilityService.iterateSelectedValues(next);
  }

  /**
   * Event fires when user reloads page. Cancels and deletes current geometry
   * if user is drawing.
   */
  @HostListener('window:beforeunload', ['$event']) unloadHandler() {
    if (this.labelModeUtilityService.userIsDrawing) {
      this.workspaceControlService.cancelAndDeleteGeometry(this.labelModeUtilityService.latestCreatedGeometryValue.id);
    }
  }

  public onLabelModeContainerClicked(): void {
    this.videoContextMenu.close();
    this.contextMenu.close();
  }

  /**
   * Check for required globalClassifications and entry-values validity and return it.
   * Used to prevent user from completing an invalid task.
   */
  public taskValid(): boolean {
    if (this.mediaControl.hasRequiredGlobalClassifications === true && !this.validityObserver.globalClassificationsValid) {
      return false;
    }
    if (!this.validityObserver.hasGeometries()) {
      return true;
    }
    return !this.validityObserver.hasInvalidGeometries();
  }

  /**
   * Validate task and submit, go to next task on success
   */
  public onSubmitAndNextTask(): void {
    const body = new LabelTaskCompleteUpdateModel(this.taskControlService.lastChangedConfig);
    this.labelTaskService.setToComplete(this.taskId, body)
      .subscribe((response: LabelTaskCompleteResponseModel) => {
        if (response.hasLabelConfigChanged === true) {
          const translateKey = 'FEATURE.LABEL_MODE.CONFIGURATION_CHANGED';
          this.labNotificationService.info_i18(translateKey);
          // reload the config ? // or reload the full workspace a second time ?
          this.loadTaskById(response.currentTaskId);
          return;
        }
        this.loadNextTask();
      }, () => {
        this.router.navigate(['../', this.taskId], {relativeTo: this.route}).then();
      });
  }

  /**
   * Validate task and submit, navigate away on success
   */
  public onSubmitAndExitTask(): void {
    const body = new LabelTaskCompleteUpdateModel(this.taskControlService.lastChangedConfig);
    this.labelTaskService.setToComplete(this.taskId, body).subscribe((response: LabelTaskCompleteResponseModel) => {
      if (response.hasLabelConfigChanged === true) {
        const translateKey = 'FEATURE.LABEL_MODE.CONFIGURATION_CHANGED';
        this.labNotificationService.info_i18(translateKey);

        // reload the config ? // or reload the full workspace a second time ?
        this.loadTaskById(response.currentTaskId);
        return;
      }
      this.router.navigate(['/tasks']).then();
    }, () => {
      this.router.navigate(['../', this.taskId], {relativeTo: this.route}).then();
    });
  }

  private setToSkipped(): void {
    this.labelTaskService.setToSkipped(this.taskId).subscribe(() => {
      this.loadNextTask();
    }, () => {
      this.router.navigate(['/tasks']).then();
    });
  }

  /**
   * Get the next taskId by projectId and navigate there
   */
  public loadNextTask(): void {
    this.labelTaskService.getNextLabelTaskIdByProjectId(this.mediaControl.projectId).subscribe((taskId: string) => {
      this.loadTaskById(taskId);
    }, () => {
      this.router.navigate(['/tasks']).then();
    });
  }

  private loadTaskById(taskId: string | undefined): void {
    // no task id means, no further tasks available.
    if (taskId === undefined) {
      this.router.navigate(['/tasks']).then();
      return;
    }

    // double navigation to trigger full component lifecycle
    this.router.navigate(['/label-mode']).then(() => {
      this.router.navigate(['/label-mode/task', taskId]).then();
    });
  }

  /**
   * Load the label task via id and initialise the config service and the media.
   * @param labelTaskId: string the id to load.
   * @param frameNumber: optional load this frame number in the video labeling mode.
   */
  private loadLabelTask(labelTaskId: string, frameNumber: number|undefined): void {
    this.taskId = labelTaskId;
    this.labelTaskService.loadTaskById(labelTaskId).subscribe((response: SingleTaskResponseModel) => {
      this.task = response;
      this.isDummyProject = Project.isDummy(response.projectName);
      this.mediaControl.init(response, frameNumber);
      this.taskControlService.init(response);
      this.aisegService.init(this.isDummyProject, this.labelTaskService.previewMode, this.task.aiSegLimitReached);
      this.contextMenu.init();
      this.labelTaskLoaded = true;
    }, () => {
      this.router.navigate(['/tasks']).then();
    });
  }

  get rootEntryName(): string {
    return this.labelModeUtilityService.rootEntryName;
  }

  public get selectedMediaClassifications(): boolean {
    return this.labelModeUtilityService.selectedMediaClassifications;
  }
}
