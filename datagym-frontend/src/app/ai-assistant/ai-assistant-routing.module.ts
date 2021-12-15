import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AiAssistantComponent} from './component/ai-assistant.component';
import {PreLabelConfigResolverService} from './service/pre-label-config-resolver.service';
import {LabelConfigResolverService} from '../label-config/service/label-config-resolver.service';


const labelRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'configure' },
  {
    path: 'configure', component: AiAssistantComponent,
    resolve: {
      preLabelConfig: PreLabelConfigResolverService,
      labelConfig: LabelConfigResolverService
    }
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(labelRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AiAssistantRoutingModule {
}
