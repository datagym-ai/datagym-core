import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LabelEditorComponent } from './component/label-editor/label-editor.component';
import { LabelListComponent } from './component/label-list/label-list.component';
import { SharedModule } from '../shared/shared.module';
import { CreateLabelModalComponent } from './component/create-label-modal/create-label-modal.component';
import { EditEntryComponent } from './component/edit-entry/edit-entry.component';
import { ReactiveFormsModule } from '@angular/forms';
import { LabelConfigRoutingModule } from './label-config-routing.module';
import { EntryItemComponent } from './component/label-list/entry-item/entry-item.component';
import { LabelTypeFilterPipe } from './service/label-type-filter.pipe';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { TranslateModule } from '@ngx-translate/core';
import {NgSelectModule} from '@ng-select/ng-select';
import { MoveTasksModalComponent } from './component/move-tasks-modal/move-tasks-modal.component';
import { SortLabelConfigPipe } from './service/sort-label-config.pipe';
import { ImportConfigurationComponent } from './component/import-configuration/import-configuration.component';

@NgModule({
  declarations: [
    LabelEditorComponent,
    LabelListComponent,
    CreateLabelModalComponent,
    EditEntryComponent,
    EntryItemComponent,
    LabelTypeFilterPipe,
    MoveTasksModalComponent,
    SortLabelConfigPipe,
    ImportConfigurationComponent
  ],
    imports: [
        CommonModule,
        SharedModule,
        ReactiveFormsModule,
        LabelConfigRoutingModule,
        NgxSmartModalModule,
        TranslateModule,
        NgSelectModule
    ]
})
export class LabelConfigModule { }
