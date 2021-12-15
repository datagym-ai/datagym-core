import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Project} from '../../model/Project';
import {ProjectService} from '../../service/project.service';
import {UserService} from '../../../client/service/user.service';
import {ProjectDashboard} from '../../model/ProjectDashboard';
import {Subject} from 'rxjs';
import {UploadPredictionService} from '../../service/upload-prediction.service';
import {MediaType} from '../../model/MediaType.enum';
import {takeUntil} from 'rxjs/operators';
import {take} from 'rxjs/operators';


@Component({
  selector: 'app-project-detail-home',
  templateUrl: './project-detail-home.component.html',
  styleUrls: ['./project-detail-home.component.css']
})
export class ProjectDetailHomeComponent implements OnInit {
  public project: Project;
  public projectDashboard: ProjectDashboard;

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  /**
   * On VIDEO projects the labels import is not supported.
   */
  public disableImport: boolean = false;

  get shortDesc(): string {
    return this.project.shortDescription;
  }

  get fullDesc(): string {
    return this.project.description;
  }

  get isProjectAdmin(): boolean {
    const ownerId = this.project.owner;
    return this.userService.isAdminFor(ownerId);
  }

  get isSuperAdmin(): boolean {
    return !!this.route.parent.snapshot.data['SUPER_ADMIN']
      && this.userService.hasScope('account.admin');
  }

  get isExportable(): boolean {
    return this.project.exportable;
  }

  public get hasLabelConfiguration(): boolean {
    return !!this.projectDashboard?.hasLabelConfiguration;
  }

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private projectService: ProjectService,
    private uploadPrediction: UploadPredictionService
  ) {}

  ngOnInit() {
    this.project = this.route.parent.snapshot.data.project as Project;
    this.disableImport = this.project.mediaType === MediaType.VIDEO;

    this.projectService.getProjectDashboardById(this.project.id).pipe(
      take(1),
      takeUntil(this.unsubscribe)
    ).subscribe((dashData) => {
      this.projectDashboard = dashData;
    });
  }

  public onExport(): void {
    const url = this.projectService.getExportUrl(this.project.id);
    window.open(url, '_blank');
  }

  public onImport() {
    this.uploadPrediction.openDialogue({'id': this.project.id});
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
