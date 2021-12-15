import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {DatasetDetail} from '../../model/DatasetDetail';
import {UploadModalService} from '../../service/upload-modal.service';
import {Subject} from 'rxjs';
import {UploadModalConfiguration} from '../../model/UploadModalConfiguration';
import {DatasetService} from '../../service/dataset.service';
import {MediaService} from '../../service/media.service';
import {MediaListComponent} from './media-list/media-list.component';
import {MediaType} from '../../../project/model/MediaType.enum';
import {ContentType} from '../../../basic/media/model/ContentType';
import {takeUntil} from 'rxjs/operators';
import {UserService} from "../../../client/service/user.service";
import {UploadMediaOptionsInterface} from "../../model/UploadMediaOptionsInterface";


@Component({
  selector: 'app-dataset-detail-home',
  templateUrl: './dataset-detail-home.component.html',
  styleUrls: ['./dataset-detail-home.component.css']
})
export class DatasetDetailHomeComponent implements OnInit, OnDestroy {

  @ViewChild(MediaListComponent)
  public imageListComponent: MediaListComponent;

  /**
   * Disable the upload buttons while uploading
   */
  public whileUploading: boolean = false;
  public isDummy: boolean = false;
  public projectCount: number = 0;
  public dataset: DatasetDetail;
  public MediaType = MediaType;

  public get isVideo(): boolean {
    return this.dataset.mediaType === MediaType.VIDEO;
  }

  public get datasetId(): string {
    return this.dataset.id;
  }

  public get datasetName(): string {
    return this.dataset.name;
  }

  public get datasetDesc(): string {
    return this.dataset.shortDescription;
  }

  public get allowPublicUrls(): boolean {
    return !!this.dataset.allowPublicUrls;
  }

  public get allowUrlUpload(): boolean {
    if (this.isDummy) {
      return false;
    }
    return this.allowPublicUrls;
  }

  constructor(
    private route: ActivatedRoute,
    private imageService: MediaService,
    private datasetService: DatasetService,
    private uploadService: UploadModalService,
    private userService: UserService
  ) {
  }

  public get syncAwsS3Title(): string {
    // prepared to use some other titles
    return this.allowPublicUrlsTitle;
  }

  public get allowPublicUrlsTitle(): string {
    return this.getFirstTrueTitle({
      'FEATURE.DATASET.DUMMY': this.isDummy,
      'FEATURE.DATASET.NOT_SUPPORTED': !this.allowPublicUrls,
      // fallback: no title to display
      '': true
    });
  }

  get isProjectAdmin(): boolean {
    const ownerId = this.dataset.owner;
    return this.userService.isAdminFor(ownerId);
  }

  // Acts as a reset without destroying the original subject
  private readonly unsubscribe: Subject<void> = new Subject<void>();

  ngOnInit() {
    this.dataset = this.route.parent.snapshot.data.dataset as DatasetDetail;
    this.isDummy = DatasetDetail.isDummy(this.dataset);
    this.projectCount = !!this.dataset.projectCount ? this.dataset.projectCount : 0;
    this.uploadService.onImageUploaded.pipe(takeUntil(this.unsubscribe)).subscribe(() => {
      this.refreshMediaComponentList();
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public refreshMediaComponentList(): void {
    if (this.imageListComponent !== undefined) {
      this.imageListComponent.getPage(this.imageListComponent.page);
    }
  }

  public onInitUploadPublicLinks(): void {

    if (this.isDummy || this.whileUploading || !this.allowUrlUpload) {
      return;
    }

    this.whileUploading = true;
    const options: UploadMediaOptionsInterface = {};
    const conf = UploadModalConfiguration.SHAREABLE_LINK(this.datasetId, options);
    this.uploadService.openDialogue(conf);
  }

  public onInitUploadMedia(): void {

    if (this.isDummy || this.whileUploading) {
      return;
    }

    this.whileUploading = true;
    let options: UploadMediaOptionsInterface = {errorI18n: 'FEATURE.DATASET.DETAILS.IMAGES.EX_IMAGE_TYPE'};
    if (this.dataset.mediaType === MediaType.IMAGE) {
      options = {...options, acceptedTypes: [ContentType.IMAGE_JPEG_VALUE, ContentType.IMAGE_PNG_VALUE]};
    } else {
      options = {
        ...options,
        acceptedTypes: [ContentType.VIDEO_MP4_VALUE],
        errorI18n: 'FEATURE.DATASET.DETAILS.IMAGES.EX_VIDEO_TYPE'
      };
    }
    const conf = UploadModalConfiguration.LOCAL_IMAGES(this.datasetId, options);
    this.uploadService.openDialogue(conf);
  }

  public onInitSyncAwsS3(): void {

    if (this.isDummy || this.whileUploading || !this.allowUrlUpload) {
      return;
    }

    this.whileUploading = true;
    const options: UploadMediaOptionsInterface = {};
    const conf = UploadModalConfiguration.AWS_S3(this.datasetId, options);
    this.uploadService.openDialogue(conf);
  }

  /**
   * Create a object of title translations and a boolean flag.
   * The first key with a value of true is returned. The order
   * matters!
   *
   * @param titles
   */
  private getFirstTrueTitle(titles: { [key: string]: boolean }) {

    // extract the keys and filter the truthfully ones.
    const title = Object.keys(titles)
      .filter(t => !!titles[t]);

    if (Object.keys(titles).length === 0) {
      return '';
    }

    return title[0];
  }

}
