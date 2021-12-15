import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  {path: 'projects', data: {SUPER_ADMIN: true}, loadChildren: () => import('./../project/project.module').then(mod => mod.ProjectModule)},
  {path: 'datasets', data: {SUPER_ADMIN: true}, loadChildren: () => import('./../dataset/dataset.module').then(mod => mod.DatasetModule)},
  {path: 'label-mode', data: {SUPER_ADMIN: true}, loadChildren: () => import('./../label-mode/label-mode.module').then(mod => mod.LabelModeModule)}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
