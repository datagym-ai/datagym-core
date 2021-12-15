import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectListComponent } from './component/project-list/project-list.component';
import { ProjectDetailComponent } from './component/project-detail/project-detail.component';
import { ProjectCreateComponent } from './component/project-create/project-create.component';
import { ProjectRoutingModule } from './project-routing.module';
import { ProjectNameFilterPipe } from './service/project-name-filter.pipe';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '../shared/shared.module';
import { ProjectDetailHomeComponent } from './component/project-detail-home/project-detail-home.component';
import { ProjectDetailSettingsComponent } from './component/project-detail-settings/project-detail-settings.component';
import { LabelConfigModule } from '../label-config/label-config.module';
import { ProjectDatasetComponent } from './component/project-detail-settings/project-dataset/project-dataset.component';
import { ProjectListItemComponent } from './component/project-list/project-list-item/project-list-item.component';
import { ListItemComponent } from './component/project-detail-settings/project-dataset/list-item/list-item.component';
import {DatasetModule} from '../dataset/dataset.module';
import { ProjectReviewerComponent } from './component/project-detail-settings/project-reviewer/project-reviewer.component';
import { ReviewerListItemComponent } from './component/project-detail-settings/project-reviewer/reviewer-list-item/reviewer-list-item.component';
import { ProjectDashboardComponent } from './component/project-dashboard/project-dashboard.component';
import {NgxChartsModule} from '@swimlane/ngx-charts';
import { ImportPredictionComponent } from './component/project-detail-home/import-prediction/import-prediction.component';
import {NgxSmartModalModule} from 'ngx-smart-modal';

import { ProjectGuideComponent } from './component/project-guide/project-guide.component';
import {NgSelectModule} from '@ng-select/ng-select';

@NgModule({
    declarations: [
        ProjectListComponent,
        ProjectDetailComponent,
        ProjectCreateComponent,
        ProjectNameFilterPipe,
        ProjectDetailHomeComponent,
        ProjectGuideComponent,
        ProjectDetailSettingsComponent,
        ProjectDatasetComponent,
        ProjectListItemComponent,
        ListItemComponent,
        ProjectReviewerComponent,
        ReviewerListItemComponent,
        ProjectDashboardComponent,
        ImportPredictionComponent
    ],
    exports: [
        // exported temporarily to use this filter also on the workspace entry page.
        ProjectNameFilterPipe
    ],
    imports: [
        CommonModule,
        ProjectRoutingModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        SharedModule,
        LabelConfigModule,
        DatasetModule,
        NgxChartsModule,
        NgxSmartModalModule,
        NgSelectModule,
    ]
})
export class ProjectModule {
}
