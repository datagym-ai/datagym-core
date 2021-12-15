import { Component, Input, OnInit } from '@angular/core';
import { TaskViewModel } from '../../../model/TaskViewModel';
import { TaskOverviewService } from '../../../service/task-overview.service';
import { Router } from '@angular/router';
import {UserService} from '../../../../client/service/user.service';

@Component({
  selector: 'app-task-list-item',
  templateUrl: './task-list-item.component.html',
  styleUrls: ['./task-list-item.component.css']
})
export class TaskListItemComponent implements OnInit {

  @Input()
  public task: TaskViewModel;

  @Input()
  public hasAnyReviewTasks: boolean = false;

  public isAdmin: boolean = false;

  constructor(
    private router: Router,
    private userService: UserService,
    private taskOverviewService: TaskOverviewService
  ) { }

  ngOnInit() {
    this.isAdmin = this.userService.isAdminFor(this.task.owner);
  }

  public startLabeling(): void {
    this.taskOverviewService.getNextTaskIdByProjectId(this.task.projectId).subscribe(taskId => {
      this.router.navigate(['/label-mode/task', taskId]).then();
    });
  }

  public openProject(): void {
    if (!this.isAdmin) {
      return;
    }
    this.router.navigate(['projects', 'details', this.task.projectId]).then();
  }

  public startReviewing(): void {
    this.taskOverviewService.getNextReviewId(this.task.projectId).subscribe(taskId => {
      this.router.navigate(['/label-mode/task', taskId]).then();
    });
  }
}
