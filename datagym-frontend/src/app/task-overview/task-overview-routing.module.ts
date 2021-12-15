import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { TaskListComponent } from "./component/task-list/task-list.component";

const taskOverviewRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: TaskListComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(taskOverviewRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class TaskOverviewRouting {}
