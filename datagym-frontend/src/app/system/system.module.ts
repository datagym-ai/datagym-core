import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemRoutingModule } from './system-routing.module';
import { SystemComponent } from './component/system/system.component';
import { TranslateModule } from "@ngx-translate/core";


@NgModule({
  declarations: [SystemComponent],
  imports: [
    CommonModule,
    SystemRoutingModule,
    TranslateModule,
  ]
})
export class SystemModule { }
