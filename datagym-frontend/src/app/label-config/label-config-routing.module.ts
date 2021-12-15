import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LabelEditorComponent } from './component/label-editor/label-editor.component';
import { EditEntryComponent } from './component/edit-entry/edit-entry.component';
import { LabelConfigResolverService } from './service/label-config-resolver.service';
import {EditModeGuardService} from './service/edit-mode-guard.service';
import {ForbiddenKeywordResolverService} from './service/forbidden-keyword-resolver.service';

const labelRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'configure' },
  {
    path: 'configure',
    component: LabelEditorComponent,
    canDeactivate: [EditModeGuardService],
    children: [
      {
        path: 'edit/:id',
        component: EditEntryComponent,
        canActivate: [EditModeGuardService],
        canDeactivate: [EditModeGuardService]
      }
    ],
    resolve: {
      labelConfig: LabelConfigResolverService,
      forbidden: ForbiddenKeywordResolverService
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
export class LabelConfigRoutingModule {
}
