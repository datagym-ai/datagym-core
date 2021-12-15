import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskListComponent } from './component/task-list/task-list.component';
import { TaskOverviewRouting } from "./task-overview-routing.module";
import { TaskListItemComponent } from './component/task-list/task-list-item/task-list-item.component';
import { SharedModule } from "../shared/shared.module";
import { TranslateModule } from "@ngx-translate/core";


@NgModule({
  declarations: [
    TaskListComponent,
    TaskListItemComponent
  ],
  imports: [
    CommonModule,
    TaskOverviewRouting,
    SharedModule,
    TranslateModule
  ]
})
export class TaskOverviewModule { }
