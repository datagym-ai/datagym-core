import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DatasetListComponent} from './component/dataset-list/dataset-list.component';
import {DatasetDetailComponent} from './component/dataset-detail/dataset-detail.component';
import {DatasetCreateComponent} from './component/dataset-create/dataset-create.component';
import {DatasetRoutingModule} from './dataset-routing.module';
import {SharedModule} from '../shared/shared.module';
import {TranslateModule} from '@ngx-translate/core';
import {DatasetDetailHomeComponent} from './component/dataset-detail-home/dataset-detail-home.component';
import {DatasetDetailSettingsComponent} from './component/dataset-detail-settings/dataset-detail-settings.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DatasetNameFilterPipe} from './service/dataset-name-filter.pipe';
import {MediaListComponent} from './component/dataset-detail-home/media-list/media-list.component';
import {UploadModalComponent} from './component/dataset-detail-home/upload-modal/upload-modal.component';
import {NgxSmartModalModule} from 'ngx-smart-modal';
import {UploadPublicLinksComponent} from './component/dataset-detail-home/upload-modal/upload-public-links/upload-public-links.component';
import {UploadMediaComponent} from './component/dataset-detail-home/upload-modal/upload-media/upload-media.component';
import {NgSelectModule} from '@ng-select/ng-select';
import {MediaListItemComponent} from './component/dataset-detail-home/media-list/media-list-item/media-list-item.component';
import {DatasetDetailAwsS3Component} from './component/dataset-detail-aws-s3/dataset-detail-aws-s3.component';
import {SyncAwsS3Component} from './component/dataset-detail-home/upload-modal/sync-aws-s3/sync-aws-s3.component';
import {NgxPaginationModule} from 'ngx-pagination';


@NgModule({
  declarations: [
    DatasetListComponent,
    DatasetDetailComponent,
    DatasetCreateComponent,
    DatasetDetailHomeComponent,
    DatasetDetailSettingsComponent,
    DatasetNameFilterPipe,
    MediaListComponent,
    UploadModalComponent,
    UploadPublicLinksComponent,
    UploadMediaComponent,
    MediaListItemComponent,
    DatasetDetailAwsS3Component,
    SyncAwsS3Component
  ],
  imports: [
    FormsModule,
    CommonModule,
    DatasetRoutingModule,
    SharedModule,
    TranslateModule,
    ReactiveFormsModule,
    NgxSmartModalModule,
    NgSelectModule,
    NgxPaginationModule
  ]
})
export class DatasetModule {
}
