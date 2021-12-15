import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Project } from '../../../model/Project';
import { DialogueService } from '../../../../shared/service/dialogue.service';
import { ProjectService } from '../../../service/project.service';
import { DatasetService } from '../../../../dataset/service/dataset.service';
import { AppButtonInput } from '../../../../shared/button/button.component';
import { LabNotificationService } from '../../../../client/service/lab-notification.service';
import { DialogueModel } from '../../../../shared/dialogue-modal/DialogueModel';
import { DgSelectOptionModel } from '../../../../shared/dg-select-modal/DgSelectOptionModel';
import { DgSelectModalService } from '../../../../shared/dg-select-modal/dg-select-modal.service';
import { DatasetList } from '../../../../dataset/model/DatasetList';


@Component({
  selector: 'app-project-dataset',
  templateUrl: './project-dataset.component.html',
  styleUrls: ['./project-dataset.component.css']
})
export class ProjectDatasetComponent implements OnInit, OnDestroy {
  private project: Project;
  private listModalServiceSub: Subscription;
  private dialogueSub: Subscription;
  private projectConnectionSub: Subscription;
  private projectServiceSub: Subscription;
  private datasetServiceSub: Subscription;

  get isDummy(): boolean {
    return Project.isDummy(this.project);
  }

  constructor(private route: ActivatedRoute,
              private projectService: ProjectService,
              private datasetService: DatasetService,
              private dialogueService: DialogueService,
              private dgSelectModalService: DgSelectModalService,
              private labNotificationService: LabNotificationService) {
  }

  get datasets(): DatasetList[] {
    return this.project.datasets || [];
  }

  get hasDatasets(): boolean {
    return this.datasets.length > 0;
  }

  ngOnInit() {
    this.route.parent.data.subscribe((data: { project: Project }) => {
      this.project = data.project;
    });
  }

  ngOnDestroy(): void {
    if (this.listModalServiceSub) {
      this.listModalServiceSub.unsubscribe();
    }
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.projectConnectionSub) {
      this.projectConnectionSub.unsubscribe();
    }
    if (this.projectServiceSub) {
      this.projectServiceSub.unsubscribe();
    }
    if (this.datasetServiceSub) {
      this.datasetServiceSub.unsubscribe();
    }
  }

  public onInitDatasetAdding(): void {
    if (this.isDummy) {
      return;
    }
    this.datasetServiceSub = this.datasetService.getProjectSuitableDatasets(this.project.id).subscribe((datasets: DatasetList[]) => {
      this.datasetServiceSub.unsubscribe();
      if (datasets.length === 0) {
        const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.NOTIFICATION.NO_SUPPORTED_DATASET_FOUND';
        this.labNotificationService.error_i18(translateKey);
        return;
      }
      const options: DgSelectOptionModel[] = datasets.map(dataset => {
        return {id: dataset.id, label: dataset.name};
      });
      this.dgSelectModalService.openDialogue(options);
    });
  }

  public connectDataset(datasetId: string): void {

    if (this.isDummy) {
      return;
    }

    this.projectService.connectWithDataset(this.project.id, datasetId).subscribe(() => {
      const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.NOTIFICATION.CONNECTED_WITH_DATASET';
      this.labNotificationService.success_i18(translateKey);
      this.reloadProject();
    });
  }

  public onDelete(datasetId: string): void {
    if (this.isDummy) {
      return;
    }

    const title = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.MODAL.DELETE_CONNECTION_TO_DATASET_TITLE';
    const content = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.MODAL.DELETE_CONNECTION_TO_DATASET_CONTENT';
    const deleteBtn = 'GLOBAL.DELETE';
    const cancelBtn = 'GLOBAL.CANCEL';
    const buttonLeft: AppButtonInput = { label: deleteBtn, styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    this.dialogueSub = this.dialogueService.closeAction.subscribe((choice: boolean) => {
      if (this.dialogueSub) {
        this.dialogueSub.unsubscribe();
      }
      if (choice !== true) {
        return;
      }
      this.projectConnectionSub = this.projectService.deleteConnectionToDataset(this.project.id, datasetId)
        .subscribe(() => {
          const translateKey = 'FEATURE.PROJECT.DETAIL.SETTINGS.CONNECTED_DATASETS.NOTIFICATION.REMOVED_DATASET_CONNECTION';
          this.labNotificationService.success_i18(translateKey);
          this.projectConnectionSub.unsubscribe();
          this.reloadProject();
        });
    });
  }

  private reloadProject() {
    this.projectServiceSub = this.projectService.getProjectById(this.project.id).subscribe(project => {
      this.project = project;
      this.projectServiceSub.unsubscribe();
    });
  }
}
