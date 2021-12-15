import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { TranslateModule } from '@ngx-translate/core';
import {NgSelectModule} from '@ng-select/ng-select';
import {AiAssistantRoutingModule} from './ai-assistant-routing.module';
import {AiAssistantComponent} from './component/ai-assistant.component';
import {LabelMappingComponent} from './component/label-mapping/label-mapping.component';

@NgModule({
  declarations: [
    AiAssistantComponent,
    LabelMappingComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ReactiveFormsModule,
    AiAssistantRoutingModule,
    NgxSmartModalModule,
    TranslateModule,
    NgSelectModule
  ]
})
export class AiAssistantModule { }
