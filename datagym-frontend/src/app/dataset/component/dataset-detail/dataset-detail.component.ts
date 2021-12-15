import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {DatasetService} from '../../service/dataset.service';
import {DatasetDetail} from '../../model/DatasetDetail';
import {TabGroup} from '../../../shared/tab-group/models/TabGroup';
import {Tab} from '../../../shared/tab-group/models/Tab';
import {Project} from '../../../project/model/Project';
import {ProjectService} from '../../../project/service/project.service';
import {LabNotificationService} from '../../../client/service/lab-notification.service';
import {DgSelectModalService} from '../../../shared/dg-select-modal/dg-select-modal.service';
import {DgSelectOptionModel} from '../../../shared/dg-select-modal/DgSelectOptionModel';
import {Subscription} from 'rxjs';
import {MediaType} from '../../../project/model/MediaType.enum';
import {UserService} from "../../../client/service/user.service";


@Component({
  selector: 'app-dataset-detail',
  templateUrl: './dataset-detail.component.html',
  styleUrls: ['./dataset-detail.component.css']
})
export class DatasetDetailComponent implements OnInit, OnDestroy {
  public tabs: TabGroup;
  public dataset: DatasetDetail;

  get isDummy(): boolean {
    return DatasetDetail.isDummy(this.dataset);
  }

  private routeSub: Subscription;

  private static initTabGroup(excludeAwsS3: boolean = false): TabGroup {
    const tabGroup = new TabGroup([new Tab('FEATURE.DATASET.DETAILS.TABS.HOME', 'home', [])]);

    if (!excludeAwsS3) {
      tabGroup.tabs.push(new Tab('FEATURE.DATASET.DETAILS.TABS.AWS_S3', 'aws-s3', ['ADMIN']));
    }

    tabGroup.tabs.push(new Tab('FEATURE.DATASET.DETAILS.TABS.SETTINGS', 'settings', ['ADMIN']));
    return tabGroup;
  }

  constructor(
    private router: Router,
    private datasetService: DatasetService,
    private projectService: ProjectService,
    private labNotificationService: LabNotificationService,
    private dgSelectModalService: DgSelectModalService,
    private route: ActivatedRoute,
    private userService: UserService
  ) {
  }

  get isProjectAdmin(): boolean {
    const ownerId = this.dataset.owner;
    return this.userService.isAdminFor(ownerId);
  }

  ngOnInit() {
    // subscribe here to route.data instead of using snapshot.data to react on changes
    // to the dataset within the child/tab components.
    this.routeSub = this.route.data.subscribe((data: { dataset: DatasetDetail }) => {
      this.dataset = data.dataset;
      const excludeAwsS3Tab = this.dataset.mediaType === MediaType.VIDEO;
      this.tabs = DatasetDetailComponent.initTabGroup(excludeAwsS3Tab);
    });

  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
  }

  /**
   * Show a modal with all available projects to select one and connect with this dataset.
   */
  public onAddToProject() {
    this.projectService.getSuitableProjectsForDataset(this.dataset.id).subscribe((projects: Project[]) => {
      if (projects.length === 0) {
        this.labNotificationService.error_i18('FEATURE.DATASET.DETAILS.MODAL.NO_SUPPORTED_PROJECT_FOUND');
        return;
      }

      const options: DgSelectOptionModel[] = projects.map(project => {
        return {id: project.id, label: project.name};
      });
      this.dgSelectModalService.openDialogue(options);
    });
  }

  public connectProject(projectID: string) {
    const datasetID = this.dataset.id;

    const sub = this.projectService.connectWithDataset(projectID, datasetID).subscribe(() => {
      sub.unsubscribe();

      const translateKey = 'FEATURE.DATASET.DETAILS.MODAL.CONNECTED_WITH_PROJECT';
      this.labNotificationService.success_i18(translateKey);
      this.reloadDataset();
    });
  }

  private reloadDataset() {
    const sub = this.datasetService.getDatasetById(this.dataset.id).subscribe(dataset => {
      sub.unsubscribe();
      this.dataset = dataset;
    });
  }
}
