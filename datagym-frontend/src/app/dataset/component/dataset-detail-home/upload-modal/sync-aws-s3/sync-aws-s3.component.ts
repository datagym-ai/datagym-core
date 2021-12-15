import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AwsS3SyncStatusView} from '../../../../model/AwsS3SyncStatusView';
import {Subject} from 'rxjs';
import {AwsS3Service} from '../../../../service/aws-s3.service';
import {debounceTime, finalize, take, takeUntil} from 'rxjs/operators';
import {AwsS3Image} from '../../../../../basic/media/model/AwsS3Image';


enum SyncResponseToggle {
  ADDED,
  DELETED,
  FAILED
}

@Component({
  selector: 'app-sync-aws-s3',
  templateUrl: './sync-aws-s3.component.html',
  styleUrls: ['./sync-aws-s3.component.css']
})
export class SyncAwsS3Component implements OnInit {

  @Input()
  public datasetId: string;

  @Input('removeData')
  public reset: EventEmitter<void> = new EventEmitter<void>();

  @Input('onStart')
  public onStart: EventEmitter<void> = new EventEmitter<void>();

  @Input()
  public description: string;

  /**
   * Trigger this event if some or all images are successfully uploaded.
   * The boolean argument indicates if all or not all uploads succeeded.
   */
  @Output()
  public onImagesCompleted: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  public setModalCloseable: EventEmitter<boolean> = new EventEmitter<boolean>();

  public Toggle = SyncResponseToggle;
  public selectedToggle: SyncResponseToggle = SyncResponseToggle.ADDED;
  public syncStatus: AwsS3SyncStatusView|undefined;

  public get onUploading(): boolean {
    return this.syncStatus === undefined;
  }

  public get hasNoMedia(): boolean {
    return !!this.syncStatus &&
      this.syncStatus.addedS3Images.length === 0 &&
      this.syncStatus.deletedS3Images.length === 0 &&
      this.syncStatus.uploadFailedS3Images.length === 0;
  }

  // Acts as a reset without destroying the original subject
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private s3: AwsS3Service) { }

  ngOnInit() {
    const ms = 75;
    this.onStart.pipe(debounceTime(ms), takeUntil(this.unsubscribe)).subscribe(() => {
      this.onReset();
      this.onUpload();
    });
    // The first time we must trigger the onStart event
    // to start the sync. The following times the onStart
    // event is called from 'outside' as expected.
    this.onStart.emit();
  }

  ngOnDestroy(): void {
    this.onReset();
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  public onReset(): void {
    this.syncStatus = undefined;
  }

  private onUpload(): void {
    this.syncStatus = undefined;
    this.s3.synchronize(this.datasetId).pipe(
      take(1),
      takeUntil(this.unsubscribe),
      finalize(() => {
        this.onImagesCompleted.emit();
        this.setModalCloseable.emit(true);
      })
    ).subscribe((response: AwsS3SyncStatusView) => {
      this.syncStatus = response;

      // Open the first one with content.
      if (response.addedS3Images.length > 0) {
        this.selectedToggle = SyncResponseToggle.ADDED;
      } else if (response.deletedS3Images.length > 0) {
        this.selectedToggle = SyncResponseToggle.DELETED;
      } else if (response.uploadFailedS3Images.length > 0) {
        this.selectedToggle = SyncResponseToggle.FAILED;
      }
    }, () => {
      // clear the data
      this.onReset();
    });
  }

  public listImagesFromResponse(images: AwsS3Image[]): string {
    return images
      .map(r => r.awsKey)
      .filter(r => !!r && r.length > 0)
      .join('\r\n');
  }
}
