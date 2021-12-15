import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DatasetListComponent } from './component/dataset-list/dataset-list.component';
import { DatasetDetailComponent } from './component/dataset-detail/dataset-detail.component';
import { DatasetCreateComponent } from './component/dataset-create/dataset-create.component';
import { DatasetDetailHomeComponent } from './component/dataset-detail-home/dataset-detail-home.component';
import { DatasetDetailSettingsComponent } from './component/dataset-detail-settings/dataset-detail-settings.component';
import { DatasetResolverService } from './service/dataset-resolver.service';
import {DatasetDetailAwsS3Component} from './component/dataset-detail-aws-s3/dataset-detail-aws-s3.component';
import {S3ResolverService} from './service/s3-resolver.service';


const datasetRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: DatasetListComponent
  },
  { path: 'create', component: DatasetCreateComponent },
  {
    path: 'details/:id',
    component: DatasetDetailComponent,
    resolve: { dataset: DatasetResolverService },
    runGuardsAndResolvers: 'always',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: DatasetDetailHomeComponent },
      { path: 'settings', component: DatasetDetailSettingsComponent },
      {
        path: 'aws-s3', component: DatasetDetailAwsS3Component,
        resolve: { credentials: S3ResolverService }
      }
    ]
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(datasetRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class DatasetRoutingModule {
}
