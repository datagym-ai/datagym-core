import { Component, OnDestroy, OnInit } from '@angular/core';
import { LabelTask } from '../../model/LabelTask';
import { Subject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { LabelTaskService } from '../../service/label-task.service';
import { LabelTaskApiService } from '../../service/label-task-api.service';
import { Project } from '../../../project/model/Project';
import { FilterBase } from '../../../shared/dynamic-filter/FilterBase';
import { FormControl, FormGroup } from '@angular/forms';
import { FilterTextbox } from '../../../shared/dynamic-filter/FilterTextbox';
import { FilterDropdown } from '../../../shared/dynamic-filter/FilterDropdown';
import { TranslateService } from '@ngx-translate/core';
import { HttpParams } from '@angular/common/http';
import { FilterControlService } from '../../../shared/dynamic-filter/FilterControlService';
import { LabelTaskState } from '../../model/LabelTaskState';
import { MoveAllDirections, MoveAllProjectsBindingModel } from '../../model/MoveAllProjectsBindingModel';
import { DgSelectOptionModel } from '../../../shared/dg-select-modal/DgSelectOptionModel';
import { DgSelectModalService } from '../../../shared/dg-select-modal/dg-select-modal.service';
import { take, takeUntil } from 'rxjs/operators';
import { DatasetService } from '../../../dataset/service/dataset.service';
import { DatasetList } from '../../../dataset/model/DatasetList';
import { LabNotificationService } from '../../../client/service/lab-notification.service';
import { ProjectService } from '../../../project/service/project.service';


@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit, OnDestroy {

  /**
   * A stack of (filtered) tasks to display.
   */
  public labelTasks: LabelTask[] = [];

  public isDummy: boolean = false;

  /**
   * The filter configuration required by the DynamicFilterComponent passed via template file.
   */
  public filterConfiguration: FilterBase<string>[] = [];

  public lastFilterHttpParams: HttpParams;

  public get datasets(): DatasetList[] {
    return this.project.datasets || [];
  }

  private project: Project;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();
  private labelerFilter: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: LabelTaskApiService,
    private translate: TranslateService,
    private projectService: ProjectService,
    private datasetService: DatasetService,
    private labelTaskService: LabelTaskService,
    private filterService: FilterControlService,
    private dgSelectModalService: DgSelectModalService,
    private labNotificationService: LabNotificationService
  ) {}

  ngOnInit() {
    this.project = this.route.parent.parent.snapshot.data.project as Project;
    this.isDummy = Project.isDummy(this.project);

    const defaultLimit = 200;
    // load the tasks list by using the existing api method and limit the result by default to 200.
    this.dynamicFilterChanged(new FormGroup({'limit': new FormControl(defaultLimit)}));

    // move all tasks when the modal is submitted.
    this.labelTaskService.onMoveLabelTasks.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      // update the tasks.
      this.api.getAllLabelTasksByProjectId(this.project.id, this.lastFilterHttpParams).pipe(
        take(1),
        takeUntil(this.unsubscribe)
      ).subscribe((payload: LabelTask[]) => {
        this.labelTasks = payload;
      });
    });

    this.buildFilterConfiguration();
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public toBacklog(datasetId: string): void {
    const direction = MoveAllDirections.TO_BACKLOG;
    const payload = new MoveAllProjectsBindingModel(this.project.id, datasetId, direction);
    this.labelTaskService.moveAllTasks(payload);
  }

  public toWaiting(datasetId: string): void {
    const direction = MoveAllDirections.TO_WAITING;
    const payload = new MoveAllProjectsBindingModel(this.project.id, datasetId, direction);
    this.labelTaskService.moveAllTasks(payload);
  }

  public performLabelerFilter(labelerFilter: string, taskList: LabelTask[]): LabelTask[] {
    labelerFilter = labelerFilter.toLowerCase();
    return taskList.filter( task => {
        if (task.labeler != null) {
          return task.labeler.toLowerCase().indexOf(labelerFilter) !== -1;
        }
        return false;
      });
  }


  public onCreateNewDataset(): void {
    this.router.navigate(['/', 'datasets', 'create'], {relativeTo: this.route}).then();
  }

  /**
   * Load the connect panel.
   *
   * Todo: Refactor: This code is mostly identically to `ProjectDatasetComponent::onInitDatasetAdding()`;
   */
  public onAttachDataset(): void {

    if (this.isDummy) {
      return;
    }

    const connectedDatasetIds: string[] = this.project.datasets.map(dataset => dataset.id);

    this.datasetService.getProjectSuitableDatasets(this.project.id).pipe(take(1), takeUntil(this.unsubscribe)).subscribe((datasets: DatasetList[]) => {
      const filtered = datasets.filter(dataset => (-1 === connectedDatasetIds.indexOf(dataset.id)));
      if (filtered.length === 0) {
        const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.NOTIFICATION.NO_SUPPORTED_DATASET_FOUND';
        this.labNotificationService.error_i18(translateKey);
        return;
      }
      const options: DgSelectOptionModel[] = filtered.map(dataset => {
        return {id: dataset.id, label: dataset.name};
      });
      this.dgSelectModalService.openDialogue(options);
    });
  }

  /**
   * The 'real' connection stays here.
   *
   * Todo: Refactor: This code is mostly identically to `ProjectDatasetComponent::connectDataset()`;
   *
   * @param datasetId
   */
  public connectDataset(datasetId: string) {
    this.projectService.connectWithDataset(this.project.id, datasetId).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(() => {
      this.projectService.getProjectById(this.project.id).pipe(take(1), takeUntil(this.unsubscribe)).subscribe(project => {
        this.api.getAllLabelTasksByProjectId(this.project.id, this.lastFilterHttpParams).pipe(take(1), takeUntil(this.unsubscribe)).subscribe((payload: LabelTask[]) => {
          this.project = project;
          this.labelTasks = payload;
          const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.NOTIFICATION.CONNECTED_WITH_DATASET';
          this.labNotificationService.success_i18(translateKey);
        });
      });
    });
  }

  public dynamicFilterChanged(event: FormGroup) {

    this.lastFilterHttpParams = this.filterService.buildHttpParams(event);

    this.labelerFilter = this.filterService.requestLabelerFilter(event);
    this.api.getAllLabelTasksByProjectId(this.project.id, this.lastFilterHttpParams).pipe(take(1), takeUntil(this.unsubscribe)).subscribe((payload: LabelTask[]) => {
      this.labelTasks = payload;
      if (this.labelerFilter) {
        this.labelTasks = this.performLabelerFilter(this.labelerFilter, payload);
      }
    });
  }

  /**
   * Create the filter configuration.
   */
  private buildFilterConfiguration(): void {

    const translateStrings: string[] = [
      'FEATURE.TASK_CONFIG.FILTER.STATE',
      'FEATURE.TASK_CONFIG.FILTER.LIMIT',
      'FEATURE.TASK_CONFIG.FILTER.ALL',
      'FEATURE.TASK_CONFIG.FILTER.SEARCH_DESCRIPTION',
      'FEATURE.TASK_CONFIG.FILTER.SEARCH_LABELER'
    ];

    this.translate.get(translateStrings).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe(translatedText => {
      const LIMIT_LABEL = translatedText['FEATURE.TASK_CONFIG.FILTER.LIMIT'];
      this.filterConfiguration = [
        new FilterTextbox({
          key: 'search',
          placeholder: translatedText['FEATURE.TASK_CONFIG.FILTER.SEARCH_DESCRIPTION'],
          colSize: 2
        }),
        new FilterTextbox({
          key: 'search_labeler',
          placeholder: translatedText['FEATURE.TASK_CONFIG.FILTER.SEARCH_LABELER'],
          colSize: 2
        }),
        new FilterDropdown({
          key: 'state',
          colSize: 2,
          placeholder: translatedText['FEATURE.TASK_CONFIG.FILTER.STATE'],
          options: [
            {key: LabelTaskState.BACKLOG, value: LabelTaskState.BACKLOG},
            {key: LabelTaskState.WAITING, value: LabelTaskState.WAITING},
            {key: LabelTaskState.IN_PROGRESS, value: LabelTaskState.IN_PROGRESS.replace('_', ' ')},
            {key: LabelTaskState.COMPLETED, value: LabelTaskState.COMPLETED},
            {key: LabelTaskState.SKIPPED, value: LabelTaskState.SKIPPED},
            {key: LabelTaskState.WAITING_CHANGED, value: LabelTaskState.WAITING_CHANGED.replace('_', ' ')},
            {key: LabelTaskState.REVIEWED, value: LabelTaskState.REVIEWED},
            {key: LabelTaskState.REVIEWED_SKIP, value: LabelTaskState.REVIEWED_SKIP}
          ]
        }),
        new FilterDropdown({
          key: 'limit',
          colSize: 2,
          placeholder: `${ LIMIT_LABEL } 200`,
          options: [
            {key: 50, value: `${ LIMIT_LABEL } 50`},
            {key: 100, value: `${ LIMIT_LABEL } 100`},
            {key: 200, value: `${ LIMIT_LABEL } 200`},
            {key: 500, value: `${ LIMIT_LABEL } 500`},
            {key: 1000, value: `${ LIMIT_LABEL } 1000`},
            {key: 0, value: translatedText['FEATURE.TASK_CONFIG.FILTER.ALL']}
          ]
        })
      ];
    });
  }
}
