import { Component, OnDestroy, OnInit } from '@angular/core';
import { DatasetDetail } from '../../model/DatasetDetail';
import { DatasetService } from '../../service/dataset.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { AppButtonInput } from '../../../shared/button/button.component';
import { DialogueService } from '../../../shared/service/dialogue.service';
import { DatasetUpdateBindingModel } from '../../model/DatasetUpdateBindingModel';
import { DialogueModel } from '../../../shared/dialogue-modal/DialogueModel';
import { DatasetFormBuilder } from '../../model/DatasetFormBuilder';
import { takeUntil } from 'rxjs/operators';
import { MediaType } from '../../../project/model/MediaType.enum';


@Component({
  selector: 'app-dataset-detail-settings',
  templateUrl: './dataset-detail-settings.component.html',
  styleUrls: ['./dataset-detail-settings.component.css']
})
export class DatasetDetailSettingsComponent implements OnInit, OnDestroy {
  public editDatasetForm: FormGroup;
  public dataset: DatasetDetail;

  get isDummy(): boolean {
    return DatasetDetail.isDummy(this.dataset);
  }

  private dialogueSub: Subscription;
  private updateDatasetSub: Subscription;
  private deleteDatasetSub: Subscription;
  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  public MediaType = MediaType;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private datasetService: DatasetService,
              private dialogueService: DialogueService) {
  }

  ngOnInit() {
    this.dataset = this.route.parent.snapshot.data.dataset as DatasetDetail;
    this.editDatasetForm = DatasetFormBuilder.create(this.dataset);
  }

  ngOnDestroy(): void {
    if (this.updateDatasetSub) {
      this.updateDatasetSub.unsubscribe();
    }
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.deleteDatasetSub) {
      this.deleteDatasetSub.unsubscribe();
    }
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onDelete(): void {

    if (this.isDummy) {
      return;
    }

    const title = 'FEATURE.DATASET.DETAILS.SETTINGS.DANGER_ZONE.MODAL.TITLE';
    const content = this.dataset.projectCount && this.dataset.projectCount === 0
      ? 'FEATURE.DATASET.DETAILS.SETTINGS.DANGER_ZONE.MODAL.CONTENT'
      : 'FEATURE.DATASET.DETAILS.SETTINGS.DANGER_ZONE.MODAL.CONTENT_WITH_CONNECTIONS';
    const cancelBtn = 'GLOBAL.CANCEL';
    const deleteBtn: AppButtonInput = { label: 'GLOBAL.DELETE', styling: 'warn' };
    const dialogueContent: DialogueModel = {title, content, buttonLeft: deleteBtn, buttonRight: cancelBtn};
    this.dialogueService.openDialogue(dialogueContent);
    if (this.dialogueSub) {
      this.dialogueSub.unsubscribe();
    }
    if (this.deleteDatasetSub) {
      this.deleteDatasetSub.unsubscribe();
    }
    this.dialogueSub = this.dialogueService.closeAction.pipe(takeUntil(this.unsubscribe)).subscribe((choice: boolean) => {
      if (choice === false) {
        return;
      }
      this.deleteDatasetSub = this.datasetService.deleteDatasetById(this.dataset.id).pipe(takeUntil(this.unsubscribe)).subscribe(() => {
        this.router.navigate(['datasets']).then();
      });
    });
  }

  public onSubmit(): void {

    if (this.isDummy) {
      return;
    }

    const id = this.dataset.id;
    const name: string = this.editDatasetForm.value.datasetName;
    const shortDescription: string = this.editDatasetForm.value.datasetShortDescription;
    const updatedDataset: DatasetUpdateBindingModel = new DatasetUpdateBindingModel(name, shortDescription);
    if (this.updateDatasetSub){
      this.updateDatasetSub.unsubscribe();
    }
    this.updateDatasetSub = this.datasetService.updateDataset(id, updatedDataset).pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.router.onSameUrlNavigation = 'reload';
      this.router.navigate([], { relativeTo: this.route }).then();
    });
  }

  public onReset(): void {

    if (this.isDummy) {
      return;
    }

    this.editDatasetForm = DatasetFormBuilder.create(this.dataset);
  }
}
