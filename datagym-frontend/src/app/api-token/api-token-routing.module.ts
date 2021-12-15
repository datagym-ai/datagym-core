import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ApiTokenListComponent} from './component/api-token-list/api-token-list.component';
import {ApiTokenCreateComponent} from './component/api-token-create/api-token-create.component';


const apiTokenRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: ApiTokenListComponent,
    runGuardsAndResolvers: 'always',
    // resolve: { tokens: ApiTokenListResolverService },
  },
  {
    path: 'create',
    component: ApiTokenCreateComponent
  }
];


@NgModule({
  imports: [
    RouterModule.forChild(apiTokenRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class ApiTokenRoutingModule {}
