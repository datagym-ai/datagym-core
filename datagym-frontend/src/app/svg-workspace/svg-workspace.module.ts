import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {WorkspaceComponent} from './components/workspace/workspace.component';
import {SvgWorkspaceRoutingModule} from './svg-workspace-routing.module';
import {NgxPanZoomModule} from 'ngx-panzoom';
import {SharedModule} from '../shared/shared.module';
import {SimpleNotificationsModule} from 'angular2-notifications';
import {AisegLoadingComponent} from './components/aiseg-loading/aiseg-loading.component';
import {TranslateModule} from '@ngx-translate/core';
import {ContextMenuComponent} from './components/context-menu/context-menu.component';
import {FormsModule} from '@angular/forms';
import {ImageMediaComponent} from './components/workspace/image-media/image-media.component';
import {VideoMediaComponent} from './components/workspace/video-media/video-media.component';

@NgModule({
  declarations: [
    WorkspaceComponent,
    AisegLoadingComponent,
    ContextMenuComponent,
    ImageMediaComponent,
    VideoMediaComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SvgWorkspaceRoutingModule,
    NgxPanZoomModule,
    SharedModule,
    TranslateModule
  ],
  exports: [
    WorkspaceComponent
  ]
})
export class SvgWorkspaceModule {
}
