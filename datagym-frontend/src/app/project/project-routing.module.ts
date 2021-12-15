import { RouterModule, Routes } from '@angular/router';
import { ProjectListComponent } from './component/project-list/project-list.component';
import { ProjectDetailComponent } from './component/project-detail/project-detail.component';
import { ProjectCreateComponent } from './component/project-create/project-create.component';
import { NgModule } from '@angular/core';
import { ProjectResolverService } from './service/project-resolver.service';
import { ProjectDetailHomeComponent } from './component/project-detail-home/project-detail-home.component';
import { ProjectDetailSettingsComponent } from './component/project-detail-settings/project-detail-settings.component';

const projectRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: ProjectListComponent
  },
  { path: 'create', component: ProjectCreateComponent },
  {
    path: 'details/:id',
    component: ProjectDetailComponent,
    resolve: { project: ProjectResolverService },
    runGuardsAndResolvers: 'always',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: ProjectDetailHomeComponent },
      {
        path: 'tasks',
        loadChildren: () => import('../task-config/task-config.module').then(mod => mod.TaskConfigModule)
      },
      {
        path: 'label-config',
        loadChildren: () => import('../label-config/label-config.module').then(mod => mod.LabelConfigModule)
      },
      {
        path: 'ai-assistant',
        loadChildren: () => import('../ai-assistant/ai-assistant.module').then(mod => mod.AiAssistantModule)
      },
      { path: 'settings', component: ProjectDetailSettingsComponent }
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(projectRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class ProjectRoutingModule {
}
