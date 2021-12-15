import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskListComponent } from './component/task-list/task-list.component';
import { TranslateModule } from '@ngx-translate/core';
import { TaskConfigRouting } from './task-config-routing.module';
import { TaskItemComponent } from './component/task-list/task-item/task-item.component';
import { SharedModule } from '../shared/shared.module';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { InfoModalComponent } from './component/task-list/info-modal/info-modal.component';
import { ReleaseTasksModalComponent } from './component/task-list/release-tasks-modal/release-tasks-modal.component';
import { ToBacklogModalComponent } from './component/task-list/to-backlog-modal/to-backlog-modal.component';

@NgModule({
  declarations: [
    TaskListComponent,
    TaskItemComponent,
    InfoModalComponent,
    ReleaseTasksModalComponent,
    ToBacklogModalComponent,
  ],
  imports: [
    FormsModule,
    SharedModule,
    CommonModule,
    TranslateModule,
    TaskConfigRouting,
    NgxSmartModalModule,
    ReactiveFormsModule,
    NgSelectModule
  ]
})
export class TaskConfigModule { }
