import {AfterViewInit, Component, EventEmitter, OnDestroy, Output} from '@angular/core';
import {NgxSmartModalComponent, NgxSmartModalService} from 'ngx-smart-modal';
import {Subscription} from 'rxjs';
import {Media} from '../../../../basic/media/model/Media';
import {UploadModalService} from '../../../service/upload-modal.service';
import {UploadModalConfiguration} from '../../../model/UploadModalConfiguration';
import {MediaSourceType} from '../../../../basic/media/model/MediaSourceType';

/**
 * Component description:
 *
 * The template uses ngSwitch depending on the MediaSourceType to display the upload dialogue.
 *
 * Depending on that type some subcomponents are loaded. That component receive the uploadUrl
 * as argument and must handle the upload. After uploading, they must call (via ./upload-modal.component.html)
 * - onMediaUploaded() method for every single media to 'register' the new media and close the modal.
 * or
 * - onMediaUploaded method to 'register every single media' and
 * - loadingFinished method to close the modal
 */

@Component({
  selector: 'app-upload-modal',
  templateUrl: './upload-modal.component.html',
  styleUrls: ['./upload-modal.component.css']
})
export class UploadModalComponent implements AfterViewInit, OnDestroy {

  /**
   * This id should not be changed.
   * It's also hard coded in the template file.
   * It's also used in the taskListComponent to select this modal component.
   */
  public modalId: string = 'UploadModal';
  /**
   * Trigger this event to inform the child components,
   * to clear their 'local' storage.
   */
  public removeData: EventEmitter<void> = new EventEmitter<void>();

  // make the upload types available in template file.
  public readonly MediaType = MediaSourceType;

  // should the modal be closeable?
  public isCloseable: boolean = true;

  @Output()
  public refreshList: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  public onFinished: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output()
  public onStart: EventEmitter<void> = new EventEmitter<void>();

  private dataSub: Subscription;
  private onFinishSub: Subscription;
  private modal: NgxSmartModalComponent;
  private configuration: UploadModalConfiguration;

  constructor(
    private modalService: NgxSmartModalService,
    private uploadService: UploadModalService
  ) { }

  get hasConfiguration(): boolean {
    return !!this.configuration;
  }
  get options(): {} {
    return this.configuration.options;
  }
  get content(): string {
    return `FEATURE.DATASET.MEDIA_LIST.UPLOAD_DIALOGUE.${ this.uploadType }.CONTENT`;
  }

  get datasetId(): string {
    return this.configuration.datasetId;
  }

  get uploadType(): MediaSourceType {
    return this.configuration.uploadType;
  }

  /**
   * Beside 'allImagesCompleted' with this
   * method the 'refreshImageComponentList' event is emitted
   * to reload the full images list.
   */
  public refrehMediaList(): void {
    this.refreshList.emit();
  }

  /**
   * Call onMediaUploaded to register a new single media.
   */
  public onMediaUploaded(image: Media): void {
    this.uploadService.onImageUploaded.emit(image);
  }

  /**
   * Just clear the local cache instead of closing
   * the modal so multiple uploads can be started
   * in a row.
   */
  public clearData(): void {
    this.removeData.emit();
  }

  ngAfterViewInit(): void {
    this.modal = this.modalService.get(this.modalId);

    if (this.onFinishSub) {
      this.onFinishSub.unsubscribe();
    }
    this.onFinishSub = this.modal.onAnyCloseEvent.subscribe(() => {
      this.onFinished.emit(true);
    });

    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    this.dataSub = this.modal.onDataAdded.subscribe((configuration: UploadModalConfiguration) => {
      if (Object.keys(configuration.options).includes('closeable')) {
        this.isCloseable = !!configuration.options.closeable;
      }
      this.configuration = configuration;
      if (this.uploadType === MediaSourceType.AWS_S3) {
        this.onStart.emit();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.dataSub) {
      this.dataSub.unsubscribe();
    }
    if (this.modal) {
      this.modal.close();
    }
    if (this.onFinishSub) {
      this.onFinishSub.unsubscribe();
    }
  }

  public onClose(): void {
    this.removeData.emit();
    this.modal.removeData();
  }
}
