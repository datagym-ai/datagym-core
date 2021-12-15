import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Media} from '../../../../../basic/media/model/Media';
import {ContentType} from '../../../../../basic/media/model/ContentType';
import {AwsS3MediaService} from '../../../../service/aws-s3-media.service';
import {Observable, of} from 'rxjs';
import {UploadMediaOptionsInterface} from "../../../../model/UploadMediaOptionsInterface";

@Component({
  selector: 'app-upload-media',
  templateUrl: './upload-media.component.html',
  styleUrls: ['./upload-media.component.css']
})
export class UploadMediaComponent implements OnInit {

  @Input()
  public datasetId: string;
  @Input()
  public description: string;
  @Input()
  public options: UploadMediaOptionsInterface = {errorI18n: 'FEATURE.DATASET.DETAILS.IMAGES.EX_IMAGE_TYPE'};
  @Output()
  public onUploadCompleted: EventEmitter<Media> = new EventEmitter<Media>();
  @Output()
  public loadingFinished: EventEmitter<void> = new EventEmitter<void>();
  @Output()
  public onUploadStarted: EventEmitter<void> = new EventEmitter<void>();

  // should the content be centered? Adds 'text-center' css class
  get center(): boolean {
    return this.options.center;
  }

  constructor(private awsS3MediaService: AwsS3MediaService) {
  }

  get uploadUrl(): string {
    return `/api/dataset/${this.datasetId}/file`;
  }

  get acceptedTypes(): ContentType[] {
    return this.options.acceptedTypes;
  }

  ngOnInit() {
  }

  public createAwsPreSignedUploadURI(filename: string): Observable<string|undefined> {
    if (this.options.acceptedTypes.indexOf(ContentType.VIDEO_MP4_VALUE) !== -1) {
      return this.awsS3MediaService.createAwsPreSignedUploadURI(this.datasetId, filename);
    }
    return of(undefined);
  }

  public confirmPreSignedUrlUpload(preSignedUrl: string) {
    return this.awsS3MediaService.confirmPreSignedUrlUpload(this.datasetId, preSignedUrl);
  }
}
