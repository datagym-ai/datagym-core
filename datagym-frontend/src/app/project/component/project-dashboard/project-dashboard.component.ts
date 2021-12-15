import {Component, ElementRef, HostListener, Input, OnInit, ViewChild} from '@angular/core';
import {ChartEntry, ProjectDashboard} from '../../model/ProjectDashboard';
import {TranslateService} from '@ngx-translate/core';
import {DatasetMediaStatus} from '../../model/DatasetMediaStatus';
import {ProjectService} from '../../service/project.service';
import * as shape from 'd3-shape';
import {ProjectLabelCountByDayViewModel} from '../../model/ProjectLabelCountByDayViewModel';
import {ProjectGeometryCountsViewModel} from '../../model/ProjectGeometryCountsViewModel';
import {MediaType} from "../../model/MediaType.enum";


@Component({
  selector: 'app-project-dashboard',
  templateUrl: './project-dashboard.component.html',
  styleUrls: ['./project-dashboard.component.css']
})
export class ProjectDashboardComponent implements OnInit {
  @Input()
  public projectDashboard: ProjectDashboard;

  @ViewChild('datasetChart', {static: false, read: ElementRef})
  public datasetChartRef: ElementRef;

  @ViewChild('labelsByDayChart', {static: false, read: ElementRef})
  public labelsByDayChartRef: ElementRef;

  @ViewChild('taskChart', {static: true, read: ElementRef})
  public taskChartRef: ElementRef;

  selectedDataSetImageError: DatasetMediaStatus = null;

  datasetChartView: number[] = [800, 200];
  taskChartView: number[] = [800, 200];
  labelsByDayChartView: number[] = [1300, 400];

  public curve: any = shape.curveNatural;

  public readonly localMediaLabel: string = this.translate.instant('FEATURE.PROJECT.DASHBOARD.CHARTS.MEDIA.LOCAL');
  public readonly urlMediaLabel: string = this.translate.instant('FEATURE.PROJECT.DASHBOARD.CHARTS.MEDIA.SHARABLE_LINK');
  public readonly awsS3MediaLabel: string = this.translate.instant('FEATURE.PROJECT.DASHBOARD.CHARTS.MEDIA.AWS_S3');

  public labelCountByDayData: ProjectLabelCountByDayViewModel;
  public geometryCountData: ProjectGeometryCountsViewModel;

  public geometryCountsChartResults: ChartEntry[];
  public reviewChartResults: ChartEntry[];
  public datasetChartResults: ChartEntry[];
  public datasetImageStatusChartResults: ChartEntry[];
  public selectedDatasetImageStatusChartResults: Object[];
  public activeEntries = [];
  public taskChartResults: ChartEntry[];
  public mediaSourceTypeChartResults: ChartEntry[];
  public labelCountsByDayChartResults: ChartEntry[];

  public MediaType = MediaType;

  mediaCount: number;
  reviewCount: number;

  public readonly colorScheme = {
    domain: [
      '#6300ED',
      '#72E2E8',
      '#C499FF',
      '#ffe299',
      '#ed008a',
      '#d900ed',
      '#7672e8',
      '#CCFF90',
      '#FFFF8D',
      '#FF9E80'
    ]
  };

  constructor(private translate: TranslateService,
              private projectService: ProjectService) {

  }


  ngOnInit(): void {
    this.datasetChartResults = ProjectDashboard.createDatasetsChartResults(this.projectDashboard.datasets);
    this.taskChartResults = ProjectDashboard.createTasksChartResults(this.projectDashboard.taskStatus);

    const approvedLabel: string = this.translate.instant('FEATURE.PROJECT.DASHBOARD.CHARTS.REVIEWS.APPROVED');
    this.reviewChartResults = ProjectDashboard.createReviewPerformanceChartResults(this.projectDashboard, approvedLabel);
    this.reviewCount = ProjectDashboard.getTotalReviewCount(this.projectDashboard);

    this.mediaSourceTypeChartResults = ProjectDashboard.createMediaSourceTypeChartResults(this.projectDashboard.taskMediaDetail, this.localMediaLabel, this.urlMediaLabel, this.awsS3MediaLabel);
    this.mediaCount = ProjectDashboard.getTotalMediaCount(this.projectDashboard.taskMediaDetail);

    this.datasetImageStatusChartResults = ProjectDashboard.createDatasetImageStatusChartResults(this.projectDashboard.datasetMediaStatuses);

    this.updateViewBoxes();

    this.projectService.getProjectDashboardGeometryCount(this.projectDashboard.id).subscribe((dashData) => {
      this.geometryCountsChartResults = ProjectDashboard.createGeometryCountsChartResults(dashData.geometryCounts);
      this.geometryCountData = dashData;
      this.updateViewBoxes();
    });

    /* DISABLED THROUGH PERFORMANCE ISSUES - TICKET CREATED: LAB-854
    this.projectService.getProjectDashboardGeometryCountByDays(this.projectDashboard.id).subscribe((dashData) => {
      this.labelCountsByDayChartResults = ProjectDashboard.createGeometryCountsByDayChartResults(dashData.labelCounts);
      this.labelCountByDayData = dashData;
      this.updateViewBoxes();
    });*/
  }

  @HostListener('window:resize')
  onResize() {
    this.updateViewBoxes();
  }

  private updateViewBoxes(): void {
    if (!!this.datasetChartRef && !!this.projectDashboard.datasets.length) {
      this.datasetChartView = this.getAdvancedPieChartView(this.datasetChartRef, this.calcDatasetChartWidth(), 200);
    }
    if (!!this.projectDashboard.countTasks) {
      this.taskChartView = this.getAdvancedPieChartView(this.taskChartRef, this.calcTaskChartWidth(), 200);
    }
    if (!!this.labelsByDayChartRef && !!this.labelCountByDayData && !!this.labelCountByDayData && this.labelCountByDayData.labelCountTotal > 0) {
      this.labelsByDayChartView = this.getAdvancedPieChartView(this.labelsByDayChartRef, 1300, 400);
    }

    if (!!this.projectDashboard.datasets.length && !!this.projectDashboard.countTasks) {
      if (this.datasetChartView[0] > this.taskChartView[0]) {
        this.taskChartView = this.datasetChartView;
      } else {
        this.datasetChartView = this.taskChartView;
      }
    }
  }

  private calcDatasetChartWidth(): number {
    let width = 300;
    for (const entry of this.datasetChartResults) {
      width += (entry['name'].length * 7) + 25 + 6;
    }
    return width;
  }

  private calcTaskChartWidth(): number {
    let width = 300;
    for (const entry of this.taskChartResults) {
      width += (entry['name'].length * 9) + 25 + 6;
    }
    return width;
  }

  /**
   * Returns the best viewBox width for a chart, based on the size of its parent div container
   * Optimized for ngx-advanced-pie-chart. Height is fixed.
   * @param chartDivContainer
   * @param requiredWidth
   * @param requiredHeight
   */
  private getAdvancedPieChartView(chartDivContainer: ElementRef, requiredWidth: number, requiredHeight: number): number[] {
    const remainingWidth = chartDivContainer.nativeElement.offsetWidth;

    if (remainingWidth > 500) {
      if (remainingWidth < 800) {
        return [remainingWidth, requiredHeight];
      } else {
        if (requiredWidth < 800) {
          return [800, requiredHeight];
        } else {
          if (requiredWidth > remainingWidth) {
            return [remainingWidth - 20, requiredHeight];
          } else {
            return [requiredWidth, requiredHeight];
          }
        }
      }
    } else {
      return [500, requiredHeight];
    }
  }

  public selectDataSet(event: any): void {
    this.selectedDataSetImageError = this.projectDashboard.datasetMediaStatuses.find(value => value.datasetName === event.name);
    this.selectedDatasetImageStatusChartResults = ProjectDashboard.createMediaSourceTypeChartResults(
      this.selectedDataSetImageError.mediaStatus, this.localMediaLabel, this.urlMediaLabel, this.awsS3MediaLabel
    );
    this.activeEntries = [event];
  }
}
