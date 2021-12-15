import { RouterModule, Routes } from '@angular/router';
import { WorkspaceComponent } from './components/workspace/workspace.component';
import { NgModule } from '@angular/core';

const workspaceRoutes: Routes = [
  {path: '', pathMatch: 'full', component: WorkspaceComponent},
  // or redirect to 'workspace':
  // {path: '', pathMatch: 'full', redirectTo: 'workspace'},
  {path: 'workspace', component: WorkspaceComponent}
];

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forChild(workspaceRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class SvgWorkspaceRoutingModule {}
