
export class TaskViewModel {

  constructor(
    public projectId: string,
    public projectName: string,
    public owner: string,
    public countWaitingTasks: number,
    public countTasksToReview: number
  ) {}

}
