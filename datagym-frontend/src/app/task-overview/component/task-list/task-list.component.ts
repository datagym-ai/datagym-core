import { Component, OnInit } from '@angular/core';
import { TaskViewModel } from '../../model/TaskViewModel';
import { TaskOverviewService } from '../../service/task-overview.service';
import { RequestPendingObserverService } from '../../../client/service/request-pending-observer.service';
import { delay } from 'rxjs/operators';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  public tasks: TaskViewModel[] = [];
  public hasAnyReviewTasks: boolean = false;
  public requestsPending: boolean = false;

  constructor(private taskService: TaskOverviewService, private requestPendingObserverService: RequestPendingObserverService) {
  }

  ngOnInit() {
    this.taskService.getTaskOverview().subscribe((response: TaskViewModel[]) => {

      this.hasAnyReviewTasks = response.filter((task: TaskViewModel) => task.countTasksToReview > 0).length > 0;
      response = response.sort(TaskListComponent.sort);
      this.tasks = response;
    });
    this.requestPendingObserverService.requestsPending$.pipe(delay(0)).subscribe(loading => this.requestsPending = loading);
  }

  openDocumentation() {
    const url = `https://docs.datagym.ai/documentation/getting-started`;
    window.open(url, '_blank');
  }

  private static sort(a: TaskViewModel, b: TaskViewModel): -1|0|1 {
    const owners = [a.owner, b.owner];
    if (owners[0] === owners[1]) {
      return 0;
    }

    owners.sort(); // in alphabetical and ascending order.
    return a.owner === owners[0] ? -1 : 1;
  }
}
