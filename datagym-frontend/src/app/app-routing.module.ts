import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Devinternal } from './devinternal/devinternal';
import { AuthGuard } from './client/service/auth-guard.service';
import { AccountSettingsComponent } from './basic/account-settings/account-settings.component';
import { NotFoundComponent } from './basic/not-found/not-found.component';
import { PreviewModeUri } from './label-mode/model/PreviewModeUri';
import { AccountAdminGuard } from './admin/account-admin-guard.service';
import { AdminModeUri } from './label-mode/model/AdminModeUri';
import {GotoGuard} from './client/service/goto-guard.service';
import {LogoutGuard} from './basic/navbar/service/logout-guard.service';


const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'tasks', canActivateChild: [AuthGuard] },
  { path: 'devinternal',  canActivate: [AuthGuard],canActivateChild: [AuthGuard], pathMatch: 'full', component: Devinternal },
  { path: 'account-settings',  canActivate: [AuthGuard], canActivateChild: [AuthGuard],pathMatch: 'full', component: AccountSettingsComponent },
  { path: 'api-tokens',  canActivate: [AuthGuard], canActivateChild: [AuthGuard], loadChildren: () => import('./api-token/api-token.module').then(mod => mod.ApiTokenModule) },
  { path: 'projects', canActivate: [AuthGuard], canActivateChild: [AuthGuard], loadChildren: () => import('./project/project.module').then(mod => mod.ProjectModule) },
  { path: 'datasets',  canActivate: [AuthGuard], canActivateChild: [AuthGuard], loadChildren: () => import('./dataset/dataset.module').then(mod => mod.DatasetModule) },
  { path: 'label-mode', canActivate: [AuthGuard],canActivateChild: [AuthGuard], loadChildren: () => import('./label-mode/label-mode.module').then(mod => mod.LabelModeModule) },
  { path: 'tasks', canActivate: [AuthGuard], canActivateChild: [AuthGuard],loadChildren: () => import('./task-overview/task-overview.module').then(mod => mod.TaskOverviewModule) },
  { path: 'version', canActivate: [AuthGuard], canActivateChild: [AuthGuard], loadChildren: () => import('./system/system.module').then(mod => mod.SystemModule) },
  { path: 'workspace', canActivate: [AuthGuard], canActivateChild: [AuthGuard], loadChildren: () => import('./svg-workspace/svg-workspace.module').then(mod => mod.SvgWorkspaceModule)},
  { path: PreviewModeUri.ROOT, data: {PREVIEW_MODE: true}, loadChildren: () => import('./label-mode/label-mode.module').then(mod => mod.LabelModeModule)},
  { path: AdminModeUri.ROOT, canActivate: [AuthGuard, AccountAdminGuard], canActivateChild: [AuthGuard, AccountAdminGuard],
    loadChildren: () => import('./admin/admin.module').then(mod => mod.AdminModule)
  },
  { path: 'goto/:target', canActivate: [GotoGuard], runGuardsAndResolvers: 'always', /* could be reached if the target is unknown */ component: NotFoundComponent },
  { path: 'logout', canActivate: [LogoutGuard], runGuardsAndResolvers: 'always', pathMatch: 'full', /* would never be reached but is required to register the component */ component: NotFoundComponent },

  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
