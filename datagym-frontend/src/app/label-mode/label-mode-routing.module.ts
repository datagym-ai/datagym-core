import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ToggleNavBarGuard } from './toggle-nav-bar.guard';
import { LabelModeComponent } from './component/label-mode/label-mode.component';
import { PreviewModeUri } from './model/PreviewModeUri';

const labelModeRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/tasks' },
  {
    path: PreviewModeUri.PATH,
    component: LabelModeComponent,
    canActivate: [ToggleNavBarGuard],
    canDeactivate: [ToggleNavBarGuard]
  },
  {
    path: 'task/:id',
    component: LabelModeComponent,
    canActivate: [ToggleNavBarGuard],
    canDeactivate: [ToggleNavBarGuard]
  },
  {
    path: 'task/:id/:frame',
    component: LabelModeComponent,
    canActivate: [ToggleNavBarGuard],
    canDeactivate: [ToggleNavBarGuard]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(labelModeRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class LabelModeRoutingModule {
}
