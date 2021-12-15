import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DatasetService} from '../../../../service/dataset.service';
import {UrlImageUploadViewModel} from '../../../../../basic/media/model/UrlImageUploadViewModel';
import {MediaUploadStatus} from '../../../../../basic/media/model/MediaUploadStatus';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-upload-public-links',
  templateUrl: './upload-public-links.component.html',
  styleUrls: ['./upload-public-links.component.css']
})
export class UploadPublicLinksComponent implements OnInit, OnDestroy {

  @Input()
  public datasetId: string;
  @Input()
  public description: string;
  @Input()
  public options: {} = {};
  @Input('removeData')
  public reset: EventEmitter<void> = new EventEmitter<void>();

  /**
   * Trigger this event if some or all images are successfully uploaded.
   * The boolean argument indicates if all or not all uploads succeeded.
   */
  @Output()
  public onImagesCompleted: EventEmitter<void> = new EventEmitter<void>();

  @Output()
  public setModalCloseable: EventEmitter<boolean> = new EventEmitter<boolean>();

  public selectedErrorField: MediaUploadStatus = MediaUploadStatus.SUCCESS;
  public MediaUploadStatus = MediaUploadStatus;

  public onUploading: boolean = false;
  public uploadFailed: boolean = false;

  public failedUpload: UrlImageUploadViewModel[] = [];
  public successUpload: UrlImageUploadViewModel[] = [];
  public duplicateUpload: UrlImageUploadViewModel[] = [];
  public unsupportedMimeTypeUpload: UrlImageUploadViewModel[] = [];

  public listUrlsFromResponse(response: UrlImageUploadViewModel[]): string {
    return response
      .map(r => r.imageUrl)
      .filter(r => !!r && r.length > 0)
      .join('\r\n');
  }

  public urlsForm = new FormGroup({
    'urls': new FormControl(null, Validators.required)
  });

  private resetSubscription: Subscription;

  constructor(
    private datasetService: DatasetService
  ) { }

  public onReset(): void {
    this.failedUpload = [];
    this.successUpload = [];
    this.duplicateUpload = [];
    this.unsupportedMimeTypeUpload = [];
    this.uploadFailed = false;
    this.onUploading = false;
    this.urlsForm.controls['urls'].reset();
  }

  public onUpload(): void {
    const urls = this.getUrlsFormTextArea();
    if (urls.length === 0) {
      this.onUploading = false;
      return;
    }

    this.onUploading = true;
    this.uploadFailed = false;
    this.setModalCloseable.emit(false);
    this.datasetService.createImagesByShareableLink(this.datasetId, urls).subscribe((response: UrlImageUploadViewModel[]) => {
      this.onUploading = false;
      this.setModalCloseable.emit(true);

      this.successUpload = response.filter(result => result.mediaUploadStatus === MediaUploadStatus.SUCCESS);
      this.failedUpload = response.filter(result => result.mediaUploadStatus === MediaUploadStatus.FAILED);
      this.duplicateUpload = response.filter(result => result.mediaUploadStatus === MediaUploadStatus.DUPLICATE);
      this.unsupportedMimeTypeUpload = response.filter(result => result.mediaUploadStatus === MediaUploadStatus.UNSUPPORTED_MIME_TYPE);

      if (this.successUpload.length === response.length) {
        this.onReset();
        // all uploads are successfully
        this.onImagesCompleted.emit();
        return;
      }

      if (this.successUpload.length > 0) {
        this.selectedErrorField = MediaUploadStatus.SUCCESS;
      } else {
        // no upload succeed: open another tab per default
        // this if else structure is in the same order as the template:
        if (this.unsupportedMimeTypeUpload.length > 0) {
          this.selectedErrorField = MediaUploadStatus.UNSUPPORTED_MIME_TYPE;
        } else if (this.duplicateUpload.length > 0) {
          this.selectedErrorField = MediaUploadStatus.DUPLICATE;
        } else if (this.failedUpload.length > 0) {
          this.selectedErrorField = MediaUploadStatus.FAILED;
        }
      }

      // trigger images list refresh
      this.uploadFailed = true;
      this.onImagesCompleted.emit();
      this.setModalCloseable.emit(true);
    }, () => {
      // clear the data
      this.onReset();
      // 'all' images uploaded but failed, let the parent component handle that.
      this.onImagesCompleted.emit();
      this.setModalCloseable.emit(true);
    });
  }

  ngOnInit() {
    this.resetSubscription = this.reset.subscribe(() => {
      this.onReset();
    });
  }

  ngOnDestroy(): void {
    this.onReset();
    if (this.resetSubscription) {
      this.resetSubscription.unsubscribe();
    }
  }

  /**
   * Read the input and do some basic 'pre-processing'
   */
  private getUrlsFormTextArea(): string[] {
    const value = this.urlsForm.controls['urls'].value || '';

    // split the input, remove empty lines and remove duplicates
    return value
      .split(/\r\n|\n|\r/)
      .filter(url => url.length > 0)
      ;
  }
}
