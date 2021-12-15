import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LabNotificationService} from '../../client/service/lab-notification.service';
import {FileUploadService} from './file-upload.service';
import {FileUploadErrorBindingModel} from './model/FileUploadErrorBindingModel';
import {Observable} from 'rxjs';
import {ContentType} from '../../basic/media/model/ContentType';
import {flatMap} from 'rxjs/operators';


@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.css']
})
export class FileUploadComponent implements OnInit {
  @Input()
  public acceptedTypes: string[] = [];
  @Input()
  public invalidFileTypesMessage: string = 'ex_validation';
  @Input()
  public multiple: boolean = false;
  @Input()
  public uploadUrl: string;
  @Input()
  public isOpen: boolean = false;
  @Input()
  public customUploadUrlFunction: (filename: string) => Observable<string>
  @Input()
  public confirmPreSignedUrlFunction: (awsKey: string) => Observable<any>
  @Output()
  public onUploadCompleted: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  // use <any> here because the response depends on the used api endpoint.
  public onFileCompleted: EventEmitter<any> = new EventEmitter<any>();
  @Output()
  public onUploadStarted: EventEmitter<void> = new EventEmitter<void>();

  public isLoading: boolean = false;
  public fileCount: number = 0;
  public errorWhileUploading: boolean = false;
  public schemaErrors: FileUploadErrorBindingModel;
  public uploadCount: number = 0;
  public errorCount: number = 0;
  @ViewChild('fileInput')
  private fileInput: ElementRef;
  private uploadQueue: File[] = [];

  constructor(
    private route: ActivatedRoute,
    private fileUploadService: FileUploadService,
    private notificationService: LabNotificationService
  ) {
  }

  ngOnInit() {
  }

  ngOnChanges() {
    if (!this.isOpen) {
      this.errorWhileUploading = false;
    }
  }

  public onZoneClick(): void {
    this.fileInput.nativeElement.click();
  }

  public onDrop(event: DragEvent): void {
    this.preventAndStopEventPropagation(event);
    this.initiateMediaUpload(event.dataTransfer.files);
  }

  public handleInputForm(files: FileList) {
    this.initiateMediaUpload(files);
    this.fileInput.nativeElement.value = '';
  }

  private initiateMediaUpload(files: FileList): void {
    if (files.length < 1) {
      return;
    }
    if (this.invalidFileTypesFound(files)) {
      this.notificationService.error_i18(this.invalidFileTypesMessage);
      return;
    }
    this.fileCount += files.length;
    this.isLoading = true;
    const startUploading = this.uploadQueue.length === 0;
    // for-of loop does not work because a iterator is missing.
    for (let i = 0; i < files.length; i++) {
      this.uploadQueue.push(files[i]);
    }
    if (startUploading) {
      this.onUploadStarted.emit();
      this.startNextUpload();
    }
  }

  private startNextUpload(): void {
    const file = this.uploadQueue.shift();
    if (file === undefined) {
      // uploadQueue was empty.
      this.isLoading = false;
      this.fileCount = 0;
      this.errorCount = 0;
      this.uploadCount = 0;
      this.onUploadCompleted.emit();
    } else {
      this.uploadFile(file);
    }
  }

  private uploadFile(file: File): void {
    const btoaFilename: string = btoa(unescape(encodeURIComponent(file.name)));
    if (this.acceptedTypes.indexOf(ContentType.VIDEO_MP4_VALUE) === -1) {
      this.fileUploadService.uploadFile(this.uploadUrl, file, btoaFilename).subscribe((response) => {
        this.schemaErrors = new FileUploadErrorBindingModel(response.errorMessages);
        this.errorWhileUploading = this.schemaErrors.errorMessages !== undefined && !(this.schemaErrors.errorMessages.length === 0);
        this.onFileCompleted.emit(response);
        this.uploadCount++;
        this.startNextUpload();
      }, () => {
        this.errorCount++;
        this.startNextUpload();
      });
    } else {
      this.customUploadUrlFunction(btoaFilename).subscribe(preSignedUrl => {
        this.fileUploadService.uploadMp4ToAwsS3(preSignedUrl, file).pipe(
          flatMap(() => this.confirmPreSignedUrlFunction(preSignedUrl))
        ).subscribe(() => {
          this.onFileCompleted.emit();
          this.uploadCount++;
          this.startNextUpload();
        }, () => {
          this.errorCount++;
          this.startNextUpload();
        })
      }, () => {
        this.errorCount++;
        this.startNextUpload();
      })
    }
  }

  private invalidFileTypesFound(files: FileList): boolean {
    if (files.length < 1) {
      return false;
    }
    // for-of loop does not work because a iterator is missing.
    for (let i = 0; i < files.length; i++) {
      if (this.acceptedTypes.indexOf(files[i].type) === -1) {
        return true;
      }
    }
    return false;
  }

  public preventAndStopEventPropagation(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }

}
