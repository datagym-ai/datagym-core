import {Project} from './Project';
import {LabelTaskState} from '../../task-config/model/LabelTaskState';
import {MediaSourceType} from '../../basic/media/model/MediaSourceType';
import {DatasetList} from '../../dataset/model/DatasetList';
import {DataGymPlan} from '../../basic/account-settings/model/DataGymPlan';
import {DatasetMediaStatus} from './DatasetMediaStatus';
import {GeometryCount} from './GeometryCount';
import {LabelCountByDay} from './LabelCountByDay';

export interface ChartEntry {
  name: string;
  value: number;
}


export class ProjectDashboard extends Project {

  public countTasks: number;
  public countDatasets: number;
  public approvedReviewPerformance: number;
  public declinedReviewPerformance: number;
  public hasLabelConfiguration: boolean;
  public currentPlan: DataGymPlan;
  public taskStatus: Record<LabelTaskState, number>;
  public taskMediaDetail: Record<MediaSourceType, number>;
  public datasetMediaStatuses: DatasetMediaStatus[];
  public countInvalidImages: number;


  public static createDatasetsChartResults(datasets: DatasetList[]): ChartEntry[] {
    const dataSetResults: ChartEntry[] = [];
    datasets.forEach(dataset => {
      dataSetResults.push({
        'name': ProjectDashboard.trimLength(dataset.name),
        'value': dataset.mediaCount
      });
    });
    return dataSetResults;
  }

  public static createReviewPerformanceChartResults(projectDashboard: ProjectDashboard,
                                                    approvedLabel: string): ChartEntry[] {
    return [{
      'name': approvedLabel,
      'value': projectDashboard.approvedReviewPerformance
    }];
  }

  public static createTasksChartResults(taskStatus: Record<LabelTaskState, number>): ChartEntry[] {
    const labelStateOrder = [
      LabelTaskState.BACKLOG,
      LabelTaskState.WAITING,
      LabelTaskState.WAITING_CHANGED,
      LabelTaskState.IN_PROGRESS,
      LabelTaskState.SKIPPED,
      LabelTaskState.COMPLETED,
      LabelTaskState.REVIEWED,
      LabelTaskState.REVIEWED_SKIP,
    ];

    const taskStateResults: ChartEntry[] = [];
    for (const taskState of labelStateOrder) {
      if (!!taskStatus[taskState] && taskStatus[taskState] > 0) {
        taskStateResults.push({
          'name': taskState,
          'value': taskStatus[taskState]
        });
      }
    }
    return taskStateResults;
  }

  public static getTotalMediaCount(taskImageDetail: Record<MediaSourceType, number>): number {
    const localImageCount = !!taskImageDetail[MediaSourceType.LOCAL] ? taskImageDetail[MediaSourceType.LOCAL] : 0;
    const sharableImageCount = !!taskImageDetail[MediaSourceType.SHAREABLE_LINK] ? taskImageDetail[MediaSourceType.SHAREABLE_LINK] : 0;
    const awsImgCount = !!taskImageDetail[MediaSourceType.AWS_S3] ? taskImageDetail[MediaSourceType.AWS_S3] : 0;

    return localImageCount + sharableImageCount + awsImgCount;
  }

  public static getTotalReviewCount(projectDashboard: ProjectDashboard): number {
    return projectDashboard.approvedReviewPerformance + projectDashboard.declinedReviewPerformance;
  }

  public static createMediaSourceTypeChartResults(taskImageDetail: Record<MediaSourceType, number>,
                                                  localImgLabel: string, urlImgLabel: string, awsS3Label: string): ChartEntry[] {

    const localImageCount = !!taskImageDetail[MediaSourceType.LOCAL] ? taskImageDetail[MediaSourceType.LOCAL] : 0;
    const sharableImageCount = !!taskImageDetail[MediaSourceType.SHAREABLE_LINK] ? taskImageDetail[MediaSourceType.SHAREABLE_LINK] : 0;
    const awsImgCount = !!taskImageDetail[MediaSourceType.AWS_S3] ? taskImageDetail[MediaSourceType.AWS_S3] : 0;

    return [
      {
        'name': localImgLabel,
        'value': localImageCount
      },
      {
        'name': urlImgLabel,
        'value': sharableImageCount
      },
      {
        'name': awsS3Label,
        'value': awsImgCount
      }
    ];
  }

  public static createDatasetImageStatusChartResults(datasets: DatasetMediaStatus[]): ChartEntry[] {
    const dataSetResults: ChartEntry[] = [];
    datasets.forEach(dataset => {
      if (dataset.invalidMediaCount > 0) {
        dataSetResults.push({
          'name': ProjectDashboard.trimLength(dataset.datasetName),
          'value': dataset.invalidMediaCount
        });
      }
    });
    return dataSetResults;
  }

  public static createGeometryCountsChartResults(geometryCounts: GeometryCount[]): ChartEntry[] {
    const chartResults: ChartEntry[] = [];
    geometryCounts.forEach(geometryCount => {
      if (geometryCount.lcEntryValueCount > 0) {
        chartResults.push({
          'name': ProjectDashboard.trimLength(geometryCount.lcEntryValue as string),
          'value': geometryCount.lcEntryValueCount
        });
      }
    });
    return chartResults;
  }

  public static createGeometryCountsByDayChartResults(geometryCountByDay: LabelCountByDay[]): ChartEntry[] {

    return geometryCountByDay.map(geometryCount => {
      return {
        'name': geometryCount.date,
        'value':  geometryCount.geometryCount as unknown as number
      };
    });
  }

  /**
   * Trim the value string by the maxLength attribute and append three dots `...` if necessary.
   *
   * @param value the value to trim.
   * @param maxLength positive number, default is 20.
   * @private
   */
  private static trimLength(value: string, maxLength: number = undefined): string {
    const dotCount = 3;
    const defaultMaxLength = 20;
    maxLength = typeof maxLength === 'number' && maxLength > 0 ? maxLength : defaultMaxLength;

    return value.length <= maxLength ? value : `${value.substring(0, maxLength - dotCount)}...`;
  }
}
