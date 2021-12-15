import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { TaskListComponent } from "./component/task-list/task-list.component";


const taskConfigRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'configure' },
  {
    path: 'configure',
    component: TaskListComponent,
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(taskConfigRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class TaskConfigRouting {}
