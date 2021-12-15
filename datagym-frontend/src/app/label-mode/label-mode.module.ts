import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LabelModeComponent } from './component/label-mode/label-mode.component';
import { RootGeometryList } from './component/root-geometry-list/root-geometry-list.component';
import { ClassificationListComponent } from './component/classification-list/classification-list.component';
import { EntryValueListComponent } from './component/entry-value-list/entry-value-list.component';
import { ClassificationItemComponent } from './component/classification-list/classification-item/classification-item.component';
import { GeometryItemComponent } from './component/root-geometry-list/geometry-item/geometry-item.component';
import { SharedModule } from '../shared/shared.module';
import { LabelModeRoutingModule } from './label-mode-routing.module';
import { TranslateModule } from '@ngx-translate/core';
import { ReactiveFormsModule } from '@angular/forms';
import { EntryValueItemComponent } from './component/entry-value-list/entry-value-item/entry-value-item.component';
import { ProjectModule } from '../project/project.module';
import { SvgWorkspaceModule } from '../svg-workspace/svg-workspace.module';
import { NgSelectModule } from '@ng-select/ng-select';
import { ToolBarComponent } from './component/tool-bar/tool-bar.component';
import { InfoModalComponent } from './component/info-modal/info-modal.component';
import { TaskControlComponent } from './component/task-control/task-control.component';
import { ResolutionLimitComponent } from './component/resolution-limit/resolution-limit.component';
import { ActiveControlComponent } from './component/active-control/active-control.component';
import { SortGeometryItemsPipe } from './pipe/sort-geometry-items.pipe';
import { SortClassificationsPipe } from './pipe/sort-classifications.pipe';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { GroupSegmentationsPipe } from './pipe/group-segmentations.pipe';
import { KbdDirective } from './service/kbd.directive';
import { VideoControlComponent } from './component/video-control/video-control.component';
import { Seconds2timePipe } from './pipe/seconds2time.pipe';
import { VideoSettingModalComponent } from './component/video-control/video-setting-modal/video-setting-modal.component';
import { ContextMenuComponent } from './component/video-control/context-menu/context-menu.component';

@NgModule({
  declarations: [
    LabelModeComponent,
    RootGeometryList,
    ClassificationListComponent,
    EntryValueListComponent,
    ClassificationItemComponent,
    GeometryItemComponent,
    EntryValueItemComponent,
    ToolBarComponent,
    InfoModalComponent,
    TaskControlComponent,
    ResolutionLimitComponent,
    ActiveControlComponent,
    SortGeometryItemsPipe,
    SortClassificationsPipe,
    GroupSegmentationsPipe,
    KbdDirective,
    VideoControlComponent,
    Seconds2timePipe,
    VideoSettingModalComponent,
    ContextMenuComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    TranslateModule,
    LabelModeRoutingModule,
    ReactiveFormsModule,
    ProjectModule,
    SvgWorkspaceModule,
    NgSelectModule,
    NgxSmartModalModule.forChild()
  ]
})
export class LabelModeModule {
}
