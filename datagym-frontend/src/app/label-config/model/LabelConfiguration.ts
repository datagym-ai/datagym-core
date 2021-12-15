import { LcEntry } from './LcEntry';

export class LabelConfiguration {
  public id: string;
  public entries: LcEntry[];
  public projectId ?: string;
  public numberOfCompletedTasks?: number;
  public numberOfReviewedTasks?: number;

  constructor(id: string, projectId ?: string, numberOfCompletedTasks?: number, numberOfReviewedTasks?: number) {
    this.id = id;
    this.projectId = projectId;
    this.entries = [];
    this.numberOfCompletedTasks = numberOfCompletedTasks;
    this.numberOfReviewedTasks = numberOfReviewedTasks;
  }
}
