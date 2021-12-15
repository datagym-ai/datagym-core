import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ApiTokenRoutingModule} from './api-token-routing.module';
import {ApiTokenListComponent} from './component/api-token-list/api-token-list.component';
import {ApiTokenComponent} from './component/api-token-list/api-token/api-token.component';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../shared/shared.module';
import {ApiTokenNameFilterPipe} from './service/api-token-name-filter.pipe';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ApiTokenCreateComponent} from './component/api-token-create/api-token-create.component';


@NgModule({
  declarations: [
    ApiTokenComponent,
    ApiTokenListComponent,
    ApiTokenNameFilterPipe,
    ApiTokenCreateComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    ApiTokenRoutingModule,
    TranslateModule,
    SharedModule
  ]
})
export class ApiTokenModule { }
