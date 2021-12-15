import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import { AppComponent } from './app.component';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { AppRoutingModule } from './app-routing.module';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from '@angular/common/http';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { SimpleNotificationsModule } from 'angular2-notifications';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { NavBarComponent } from './basic/navbar/nav-bar.component';
import { NavbarItemComponent } from './basic/navbar/navbar-item/navbar-item.component';
import { HttpErrorInterceptor } from './client/interceptor/error/http-error-interceptor';
import { LabNotificationService } from './client/service/lab-notification.service';
import { ProjectModule } from './project/project.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SvgWorkspaceModule } from './svg-workspace/svg-workspace.module';
import { NgxPanZoomModule } from 'ngx-panzoom';
import { Devinternal } from './devinternal/devinternal';
import { LoaderInterceptor } from './client/interceptor/loader/LoaderInterceptor';
import { SharedModule } from './shared/shared.module';
import { DatasetModule } from './dataset/dataset.module';
import { LabelConfigModule } from './label-config/label-config.module';
import { LabelModeModule } from './label-mode/label-mode.module';
import { AccountSettingsComponent } from './basic/account-settings/account-settings.component';
import { NotFoundComponent } from './basic/not-found/not-found.component';
import { SystemModule } from './system/system.module';
import { LimitModalComponent } from './basic/account-settings/limit-modal/limit-modal.component';
import { HttpSuccessInterceptor } from './client/interceptor/success/http-success-interceptor';
import {UserService} from "./client/service/user.service";

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    NavbarItemComponent,
    Devinternal,
    AccountSettingsComponent,
    NotFoundComponent,
    LimitModalComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    FontAwesomeModule,
    SimpleNotificationsModule.forRoot(),
    BrowserAnimationsModule,
    SvgWorkspaceModule,
    NgxPanZoomModule,
    ProjectModule,
    DatasetModule,
    SystemModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModule,
    LabelConfigModule,
    LabelModeModule
  ],
  providers: [
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: HttpSuccessInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptor, multi: true },
    {
      provide: APP_INITIALIZER,
      useFactory: (userService: UserService) => () => userService.initialize(),
      deps: [UserService],
      multi: true
    },
    LabNotificationService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor(library: FaIconLibrary) {
    library.addIconPacks(fas);
  }
}
